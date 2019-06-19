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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

import ai.libs.hasco.core.Util;
import ai.libs.hasco.metamining.MetaMinerBasedSorter;
import ai.libs.hasco.model.Component;
import ai.libs.hasco.model.ComponentInstance;
import ai.libs.jaicore.basic.algorithm.exceptions.AlgorithmException;
import ai.libs.jaicore.ml.core.evaluation.measure.singlelabel.ZeroOneLoss;
import ai.libs.jaicore.ml.evaluation.evaluators.weka.MonteCarloCrossValidationEvaluator;
import ai.libs.jaicore.ml.evaluation.evaluators.weka.splitevaluation.SimpleSLCSplitBasedClassifierEvaluator;
import ai.libs.jaicore.ml.metafeatures.GlobalCharacterizer;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.search.algorithms.standard.lds.BestFirstLimitedDiscrepancySearch;
import ai.libs.jaicore.search.algorithms.standard.lds.BestFirstLimitedDiscrepancySearchFactory;
import ai.libs.jaicore.search.algorithms.standard.lds.NodeOrderList;
import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.model.travesaltree.ReducedGraphGenerator;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithNodeRecommenderInput;
import ai.libs.mlplan.core.AbstractMLPlanBuilder;
import ai.libs.mlplan.core.MLPlan;
import ai.libs.mlplan.core.MLPlanWekaBuilder;
import ai.libs.mlplan.metamining.databaseconnection.ExperimentRepository;
import ai.libs.mlplan.multiclass.wekamlplan.weka.MLPipelineComponentInstanceFactory;
import ai.libs.mlplan.multiclass.wekamlplan.weka.WEKAPipelineFactory;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

public class MetaMLPlan extends AbstractClassifier {

	private transient Logger logger = LoggerFactory.getLogger(MetaMLPlan.class);

	// ids
	private static final long serialVersionUID = 4772178784402396834L;
	private static final File resourceFile = new File("resources/automl/searchmodels/weka/weka-all-autoweka.json");
	private String algorithmId = "MetaMLPlan";

	// Search components
	private transient BestFirstLimitedDiscrepancySearch<TFDNode, String, NodeOrderList> lds;
	private transient WEKAMetaminer metaMiner;
	private transient WEKAPipelineFactory factory = new WEKAPipelineFactory();

	// Search configuration
	private long timeoutInSeconds = 60;
	private long safetyInSeconds = 1;
	private int cpus = 1;
	private String metaFeatureSetName = "all";
	private String datasetSetName = "all";
	private int seed = 0;

	// Search results
	private Classifier bestModel;
	private transient Collection<Component> components;

	// For intermediate results
	private transient EventBus eventBus = new EventBus();

	public MetaMLPlan(final Instances data) throws IOException {
		this(resourceFile, data);
	}

	public MetaMLPlan(final File configFile, final Instances data) throws IOException {
		// Prepare mlPlan to get a graphGenerator
		MLPlanWekaBuilder builder = AbstractMLPlanBuilder.forWeka();
		builder.withSearchSpaceConfigFile(configFile);
		builder.withDataset(data);
		MLPlan mlPlan = builder.build();
		mlPlan.next();

		// Set search components except lds
		this.components = builder.getComponents();
		this.metaMiner = new WEKAMetaminer(builder.getComponentParameterConfigurations());

		// Get lds
		BestFirstLimitedDiscrepancySearchFactory<TFDNode, String, NodeOrderList> ldsFactory = new BestFirstLimitedDiscrepancySearchFactory<>();
		GraphSearchWithNodeRecommenderInput<TFDNode, String> problemInput = new GraphSearchWithNodeRecommenderInput<>(new ReducedGraphGenerator<>(mlPlan.getGraphGenerator()),
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
		SimpleSLCSplitBasedClassifierEvaluator classifierEval = new SimpleSLCSplitBasedClassifierEvaluator(new ZeroOneLoss());
		MonteCarloCrossValidationEvaluator mccv = new MonteCarloCrossValidationEvaluator(classifierEval, 5, data, .7f, this.seed);

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
				ComponentInstance ci = Util.getSolutionCompositionFromState(this.components, solution.get(solution.size() - 1).getState(), true);
				Classifier pl = this.factory.getComponentInstantiation(ci);

				// Evaluate pipeline
				trainingTimer.reset();
				trainingTimer.start();
				this.logger.info("Evaluate Pipeline: {}", pl);
				double score = mccv.evaluate(pl);
				this.logger.info("Pipeline Score: {}", score);
				trainingTimer.stop();

				this.eventBus.post(new IntermediateSolutionEvent(this.algorithmId, pl, score));

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
				this.logger.warn("Continuing search despite error: {}", e);
			}
		}

		Thread finalEval = new Thread() {

			@Override
			public void run() {
				MetaMLPlan.this.logger.info("Evaluating best model on whole training data ({})", MetaMLPlan.this.bestModel);
				try {
					MetaMLPlan.this.bestModel.buildClassifier(data);
				} catch (Exception e) {
					MetaMLPlan.this.bestModel = null;
					MetaMLPlan.this.logger.error("Evaluation of best model failed with an exception: {}", e);
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
		return this.bestModel.classifyInstance(instance);
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
