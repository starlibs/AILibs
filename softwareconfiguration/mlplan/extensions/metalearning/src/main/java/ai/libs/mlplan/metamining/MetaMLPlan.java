package ai.libs.mlplan.metamining;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang3.time.StopWatch;
import org.api4.java.ai.graphsearch.problem.IPathSearchInput;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.openml.webapplication.fantail.dc.GlobalCharacterizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

import ai.libs.hasco.core.HASCOUtil;
import ai.libs.hasco.metamining.MetaMinerBasedSorter;
import ai.libs.jaicore.components.api.IComponent;
import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.jaicore.logging.LoggerUtil;
import ai.libs.jaicore.ml.classification.loss.dataset.EAggregatedClassifierMetric;
import ai.libs.jaicore.ml.core.evaluation.MLEvaluationUtil;
import ai.libs.jaicore.ml.weka.classification.learner.IWekaClassifier;
import ai.libs.jaicore.ml.weka.dataset.WekaInstances;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.search.algorithms.standard.lds.BestFirstLimitedDiscrepancySearch;
import ai.libs.jaicore.search.algorithms.standard.lds.BestFirstLimitedDiscrepancySearchFactory;
import ai.libs.jaicore.search.algorithms.standard.lds.NodeOrderList;
import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.model.travesaltree.ReducedGraphGenerator;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithNodeRecommenderInput;
import ai.libs.mlplan.metamining.databaseconnection.ExperimentRepository;
import ai.libs.mlplan.multiclass.wekamlplan.MLPlan4Weka;
import ai.libs.mlplan.multiclass.wekamlplan.MLPlanWekaBuilder;
import ai.libs.mlplan.multiclass.wekamlplan.weka.MLPipelineComponentInstanceFactory;
import ai.libs.mlplan.multiclass.wekamlplan.weka.WekaPipelineFactory;
import weka.classifiers.AbstractClassifier;
import weka.core.Instance;
import weka.core.Instances;

public class MetaMLPlan extends AbstractClassifier {

	private transient Logger logger = LoggerFactory.getLogger(MetaMLPlan.class);

	// ids
	private static final long serialVersionUID = 4772178784402396834L;
	private static final File resourceFile = new File("resources/automl/searchmodels/weka/weka-all-autoweka.json");
	private String algorithmId = "MetaMLPlan";

	// Search components
	private transient BestFirstLimitedDiscrepancySearch<GraphSearchWithNodeRecommenderInput<TFDNode, String>, TFDNode, String, NodeOrderList> lds;
	private transient WEKAMetaminer metaMiner;
	private transient WekaPipelineFactory factory = new WekaPipelineFactory();

	// Search configuration
	private long timeoutInSeconds = 60;
	private long safetyInSeconds = 1;
	private int cpus = 1;
	private String metaFeatureSetName = "all";
	private String datasetSetName = "all";
	private int seed = 0;

	// Search results
	private IWekaClassifier bestModel;
	private transient Collection<IComponent> components;

	// For intermediate results
	private transient EventBus eventBus = new EventBus();

	public MetaMLPlan(final ILabeledDataset<?> data) throws IOException {
		this(resourceFile, data);
	}

	public MetaMLPlan(final File configFile, final ILabeledDataset<?> data) throws IOException {
		// Prepare mlPlan to get a graphGenerator
		MLPlanWekaBuilder builder = new MLPlanWekaBuilder();
		builder.withSearchSpaceConfigFile(configFile);
		builder.withDataset(data);
		MLPlan4Weka mlPlan = builder.build();
		mlPlan.next();

		// Set search components except lds
		this.components = builder.getComponents();
		this.metaMiner = new WEKAMetaminer(builder.getComponentParameterConfigurations());

		// Get lds
		BestFirstLimitedDiscrepancySearchFactory<GraphSearchWithNodeRecommenderInput<TFDNode, String>, TFDNode, String, NodeOrderList> ldsFactory = new BestFirstLimitedDiscrepancySearchFactory<>();
		IPathSearchInput<TFDNode, String> originalInput = mlPlan.getSearchProblemInputGenerator();
		GraphSearchWithNodeRecommenderInput<TFDNode, String> problemInput = new GraphSearchWithNodeRecommenderInput<>(new ReducedGraphGenerator<>(originalInput.getGraphGenerator()), originalInput.getGoalTester(),
				new MetaMinerBasedSorter(this.metaMiner, builder.getComponents()));
		ldsFactory.setProblemInput(problemInput);
		this.lds = ldsFactory.getAlgorithm();
	}

	public void buildMetaComponents(final String host, final String user, final String password) throws AlgorithmException, InterruptedException, SQLException, IOException {
		ExperimentRepository repo = new ExperimentRepository(host, user, password, new MLPipelineComponentInstanceFactory(this.components), this.cpus, this.metaFeatureSetName, this.datasetSetName);
		this.metaMiner.build(repo.getDistinctPipelines(), repo.getDatasetCahracterizations(), repo.getPipelineResultsOnDatasets());
	}

	public void buildMetaComponents(final String host, final String user, final String password, final int limit) throws AlgorithmException, InterruptedException, SQLException, IOException {
		this.logger.info("Get past experiment data from data base and build MetaMiner.");
		ExperimentRepository repo = new ExperimentRepository(host, user, password, new MLPipelineComponentInstanceFactory(this.components), this.cpus, this.metaFeatureSetName, this.datasetSetName);
		repo.setLimit(limit);
		this.metaMiner.build(repo.getDistinctPipelines(), repo.getDatasetCahracterizations(), repo.getPipelineResultsOnDatasets());
	}

	@Override
	public void buildClassifier(final Instances data) throws Exception {
		StopWatch totalTimer = new StopWatch();
		totalTimer.start();

		// Characterize data set and give to meta miner
		this.logger.info("Characterizing data set");
		this.metaMiner.setDataSetCharacterization(new GlobalCharacterizer().characterize(data));

		// Preparing the split for validating pipelines
		this.logger.info("Preparing validation split");

		// Search for solutions
		this.logger.info("Searching for solutions");
		StopWatch trainingTimer = new StopWatch();
		this.bestModel = null;
		double bestScore = 1;
		double bestModelMaxTrainingTime = 0;
		boolean thereIsEnoughTime = true;
		boolean thereAreMoreElements = true;

		while (!this.lds.isCanceled() && thereIsEnoughTime && thereAreMoreElements) {
			try {
				SearchGraphPath<TFDNode, String> searchGraphPath = this.lds.nextSolutionCandidate();
				List<TFDNode> solution = searchGraphPath.getNodes();

				if (solution == null) {
					this.logger.info("Ran out of solutions. Search is over.");
					break;
				}

				// Prepare pipeline
				ComponentInstance ci = HASCOUtil.getSolutionCompositionFromState(this.components, solution.get(solution.size() - 1).getState(), true);
				IWekaClassifier pl = this.factory.getComponentInstantiation(ci);

				// Evaluate pipeline
				trainingTimer.reset();
				trainingTimer.start();
				this.logger.info("Evaluate Pipeline: {}", pl);
				double score = MLEvaluationUtil.mccv(pl, new WekaInstances(data), 5, .7, this.seed, EAggregatedClassifierMetric.MEAN_ERRORRATE);
				this.logger.info("Pipeline Score: {}", score);
				trainingTimer.stop();

				this.eventBus.post(new IntermediateSolutionEvent(null, pl, score));

				// Check if better than previous best
				if (score < bestScore) {
					this.bestModel = pl;
					bestScore = score;
				}
				if (trainingTimer.getTime() > bestModelMaxTrainingTime) {
					bestModelMaxTrainingTime = trainingTimer.getTime();
				}

				thereIsEnoughTime = this.checkTermination(totalTimer, bestModelMaxTrainingTime, thereIsEnoughTime);
			} catch (NoSuchElementException e) {
				this.logger.info("Finished search (Exhaustive search conducted).");
				thereAreMoreElements = false;
			} catch (Exception e) {
				this.logger.warn("Continuing search despite error: {}", LoggerUtil.getExceptionInfo(e));
			}
		}

		Thread finalEval = new Thread() {

			@Override
			public void run() {
				MetaMLPlan.this.logger.info("Evaluating best model on whole training data ({})", MetaMLPlan.this.bestModel);
				try {
					MetaMLPlan.this.bestModel.getClassifier().buildClassifier(data);
				} catch (Exception e) {
					MetaMLPlan.this.bestModel = null;
					MetaMLPlan.this.logger.error("Evaluation of best model failed with an exception: {}", LoggerUtil.getExceptionInfo(e));
				}
			}
		};

		TimerTask newT = new TimerTask() {
			@Override
			public void run() {
				MetaMLPlan.this.logger.error("MetaMLPlan: Interrupt building of final classifier because time is running out.");
				finalEval.interrupt();
			}
		};

		// Start timer that interrupts the final training
		try {
			new Timer().schedule(newT, this.timeoutInSeconds * 1000 - this.safetyInSeconds * 1000 - totalTimer.getTime());
		} catch (IllegalArgumentException e) {
			this.logger.error("No time anymore to start evaluation of final model. Abort search.");
			return;
		}
		finalEval.start();
		finalEval.join();

		this.logger.info("Ready. Best solution: {}", this.bestModel);
	}

	private boolean checkTermination(final StopWatch totalTimer, final double bestModelMaxTrainingTime, boolean thereIsEnoughTime) {
		// Check if enough time remaining to re-train the current best model on the
		// whole training data
		if ((this.timeoutInSeconds - this.safetyInSeconds) * 1000 <= (totalTimer.getTime() + bestModelMaxTrainingTime)) {
			this.logger.info("Stopping search to train best model on whole training data which is expected to take {} ms", bestModelMaxTrainingTime);
			thereIsEnoughTime = false;
		}
		return thereIsEnoughTime;
	}

	@Override
	public double classifyInstance(final Instance instance) throws Exception {
		return this.bestModel.getClassifier().classifyInstance(instance);
	}

	public void registerListenerForIntermediateSolutions(final Object listener) {
		this.eventBus.register(listener);
	}

	public void setTimeOutInSeconds(final int timeOutInSeconds) {
		this.timeoutInSeconds = timeOutInSeconds;
	}

	public void setMetaFeatureSetName(final String metaFeatureSetName) {
		this.metaFeatureSetName = metaFeatureSetName;
	}

	public void setDatasetSetName(final String datasetSetName) {
		this.datasetSetName = datasetSetName;
	}

	public void setCPUs(final int cPUs) {
		this.cpus = cPUs;
	}

	public WEKAMetaminer getMetaMiner() {
		return this.metaMiner;
	}

	public void setSeed(final int seed) {
		this.seed = seed;
	}

	public String getAlgorithmId() {
		return this.algorithmId;
	}

	public void setAlgorithmId(final String algorithmId) {
		this.algorithmId = algorithmId;
	}
}
