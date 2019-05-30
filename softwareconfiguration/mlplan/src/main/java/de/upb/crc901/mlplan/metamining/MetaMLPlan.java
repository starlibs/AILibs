package de.upb.crc901.mlplan.metamining;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

import de.upb.crc901.mlplan.core.AbstractMLPlanBuilder;
import de.upb.crc901.mlplan.core.MLPlan;
import de.upb.crc901.mlplan.core.MLPlanWekaBuilder;
import de.upb.crc901.mlplan.metamining.databaseconnection.ExperimentRepository;
import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.MLPipelineComponentInstanceFactory;
import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.WEKAPipelineFactory;
import hasco.core.Util;
import hasco.metamining.MetaMinerBasedSorter;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import jaicore.ml.core.evaluation.measure.singlelabel.ZeroOneLoss;
import jaicore.ml.evaluation.evaluators.weka.MonteCarloCrossValidationEvaluator;
import jaicore.ml.evaluation.evaluators.weka.splitevaluation.SimpleSLCSplitBasedClassifierEvaluator;
import jaicore.ml.metafeatures.GlobalCharacterizer;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import jaicore.search.algorithms.standard.lds.BestFirstLimitedDiscrepancySearch;
import jaicore.search.algorithms.standard.lds.BestFirstLimitedDiscrepancySearchFactory;
import jaicore.search.algorithms.standard.lds.NodeOrderList;
import jaicore.search.model.other.SearchGraphPath;
import jaicore.search.model.travesaltree.ReducedGraphGenerator;
import jaicore.search.probleminputs.GraphSearchWithNodeRecommenderInput;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

public class MetaMLPlan extends AbstractClassifier {
	
	private transient Logger logger = LoggerFactory.getLogger(MetaMLPlan.class);

	// ids
	private static final long serialVersionUID = 4772178784402396834L;	
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

	public MetaMLPlan(Instances data) throws IOException {
		this(new File("resources/automl/searchmodels/weka/weka-all-autoweka.json"), data);
	}

	public MetaMLPlan(File configFile, Instances data) throws IOException {
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
		GraphSearchWithNodeRecommenderInput<TFDNode, String> problemInput = new GraphSearchWithNodeRecommenderInput<>(
				new ReducedGraphGenerator<>(mlPlan.getGraphGenerator()),
				new MetaMinerBasedSorter(metaMiner, builder.getComponents()));
		ldsFactory.setProblemInput(problemInput);
		this.lds = ldsFactory.getAlgorithm();
	}

	public void buildMetaComponents(String host, String user, String password) throws Exception {
		ExperimentRepository repo = new ExperimentRepository(host, user, password,
				new MLPipelineComponentInstanceFactory(components), cpus, metaFeatureSetName, datasetSetName);
		metaMiner.build(repo.getDistinctPipelines(), repo.getDatasetCahracterizations(),
				repo.getPipelineResultsOnDatasets());
	}

	public void buildMetaComponents(String host, String user, String password, int limit) throws Exception {
		logger.info("Get past experiment data from data base and build MetaMiner.");
		ExperimentRepository repo = new ExperimentRepository(host, user, password,
				new MLPipelineComponentInstanceFactory(components), cpus, metaFeatureSetName, datasetSetName);
		repo.setLimit(limit);
		metaMiner.build(repo.getDistinctPipelines(), repo.getDatasetCahracterizations(),
				repo.getPipelineResultsOnDatasets());
	}

	@Override
	public void buildClassifier(Instances data) throws Exception {
		StopWatch totalTimer = new StopWatch();
		totalTimer.start();

		// Characterize data set and give to meta miner
		logger.info("Characterizing data set");
		metaMiner.setDataSetCharacterization(new GlobalCharacterizer().characterize(data));

		// Preparing the split for validating pipelines
		logger.info("Preparing validation split");
		SimpleSLCSplitBasedClassifierEvaluator classifierEval = new SimpleSLCSplitBasedClassifierEvaluator(new ZeroOneLoss());
		MonteCarloCrossValidationEvaluator mccv = new MonteCarloCrossValidationEvaluator(
				classifierEval,  5, data, .7f, seed);

		// Search for solutions
		logger.info("Searching for solutions");
		StopWatch trainingTimer = new StopWatch();
		bestModel = null;
		double bestScore = 1;
		double bestModelMaxTrainingTime = 0;
		boolean thereIsEnoughTime = true;
		boolean thereAreMoreElements = true;

		while (!lds.isCanceled() && thereIsEnoughTime && thereAreMoreElements) {
			try {
				SearchGraphPath<TFDNode, String> searchGraphPath = lds.nextSolutionCandidate();					
				List<TFDNode> solution = searchGraphPath.getNodes();

				if (solution == null) {
					logger.info("Ran out of solutions. Search is over.");
					break;
				}

				// Prepare pipeline
				ComponentInstance ci = Util.getSolutionCompositionFromState(components,
						solution.get(solution.size() - 1).getState(), true);
				Classifier pl = factory.getComponentInstantiation(ci);

				// Evaluate pipeline
				trainingTimer.reset();
				trainingTimer.start();
				logger.info("Evaluate Pipeline: {}",pl);
				double score = mccv.evaluate(pl);
				logger.info("Pipeline Score: {}",score);
				trainingTimer.stop();

				eventBus.post(new IntermediateSolutionEvent(this.algorithmId,pl, score, System.currentTimeMillis()));

				// Check if better than previous best
				if (score < bestScore) {
					bestModel = pl;
					bestScore = score;
				}
				if (trainingTimer.getTime() > bestModelMaxTrainingTime) {
					bestModelMaxTrainingTime = trainingTimer.getTime();
				}

				thereIsEnoughTime = checkTermination(totalTimer, bestModelMaxTrainingTime, thereIsEnoughTime);
			} catch(NoSuchElementException e) {
				logger.info("Finished search (Exhaustive search conducted).");
				thereAreMoreElements = false;
			} catch (Exception e) {
				logger.warn("Continuing search despite error: {}",e);
			}
		}

		Thread finalEval = new Thread() {

			@Override
			public void run() {
				logger.info("Evaluating best model on whole training data ({})",bestModel);
				try {
					bestModel.buildClassifier(data);
				} catch (Exception e) {
					bestModel = null;
					logger.error("Evaluation of best model failed with an exception: {}",e);
				}
			}
		};

		TimerTask newT = new TimerTask() {
			@Override
			public void run() {
				logger.error("MetaMLPlan: Interrupt building of final classifier because time is running out.");
				finalEval.interrupt();
			}
		};

		// Start timer that interrupts the final training
		try {
			new Timer().schedule(newT,
					(long) (timeoutInSeconds * 1000 - safetyInSeconds * 1000 - totalTimer.getTime()));
		} catch (IllegalArgumentException e) {
			logger.error("No time anymore to start evaluation of final model. Abort search.");
			return;
		}
		finalEval.start();
		finalEval.join();

		logger.info("Ready. Best solution: {}",bestModel);
	}

	private boolean checkTermination(StopWatch totalTimer, double bestModelMaxTrainingTime, boolean thereIsEnoughTime) {
		// Check if enough time remaining to re-train the current best model on the
		// whole training data
		if ((timeoutInSeconds - safetyInSeconds)
				* 1000 <= (totalTimer.getTime() + bestModelMaxTrainingTime)) {
			logger.info("Stopping search to train best model on whole training data which is expected to take {} ms",bestModelMaxTrainingTime);
			thereIsEnoughTime = false;
		}
		return thereIsEnoughTime;
	}

	@Override
	public double classifyInstance(final Instance instance) throws Exception {
		return bestModel.classifyInstance(instance);
	}

	public void registerListenerForIntermediateSolutions(Object listener) {
		eventBus.register(listener);
	}

	public void setTimeOutInSeconds(int timeOutInSeconds) {
		this.timeoutInSeconds = timeOutInSeconds;
	}

	public void setMetaFeatureSetName(String metaFeatureSetName) {
		this.metaFeatureSetName = metaFeatureSetName;
	}

	public void setDatasetSetName(String datasetSetName) {
		this.datasetSetName = datasetSetName;
	}

	public void setCPUs(int cPUs) {
		cpus = cPUs;
	}

	public WEKAMetaminer getMetaMiner() {
		return metaMiner;
	}

	public void setSeed(int seed) {
		this.seed = seed;
	}

	public String getAlgorithmId() {
		return algorithmId;
	}

	public void setAlgorithmId(String algorithmId) {
		this.algorithmId = algorithmId;
	}
}
