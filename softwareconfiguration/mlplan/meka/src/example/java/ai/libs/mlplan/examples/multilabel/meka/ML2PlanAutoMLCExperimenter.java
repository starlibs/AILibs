package ai.libs.mlplan.examples.multilabel.meka;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

import org.aeonbits.owner.ConfigCache;
import org.api4.java.algorithm.Timeout;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.hasco.model.ComponentInstance;
import ai.libs.jaicore.db.sql.SQLAdapter;
import ai.libs.jaicore.experiments.ExperimentDBEntry;
import ai.libs.jaicore.experiments.ExperimentRunner;
import ai.libs.jaicore.experiments.IExperimentDatabaseHandle;
import ai.libs.jaicore.experiments.IExperimentIntermediateResultProcessor;
import ai.libs.jaicore.experiments.IExperimentSetEvaluator;
import ai.libs.jaicore.experiments.databasehandle.ExperimenterMySQLHandle;
import ai.libs.jaicore.experiments.exceptions.ExperimentDBInteractionFailedException;
import ai.libs.jaicore.experiments.exceptions.ExperimentEvaluationFailedException;
import ai.libs.jaicore.ml.classification.multilabel.evaluation.loss.AutoMEKAGGPFitnessMeasureLoss;
import ai.libs.jaicore.ml.classification.multilabel.evaluation.loss.F1MacroAverageL;
import ai.libs.jaicore.ml.classification.multilabel.evaluation.loss.Hamming;
import ai.libs.jaicore.ml.classification.multilabel.evaluation.loss.InstanceWiseF1;
import ai.libs.jaicore.ml.classification.multilabel.evaluation.loss.RankLoss;
import ai.libs.jaicore.ml.classification.multilabel.learner.IMekaClassifier;
import ai.libs.jaicore.ml.weka.WekaUtil;
import ai.libs.jaicore.ml.weka.dataset.IWekaInstances;
import ai.libs.jaicore.ml.weka.dataset.WekaInstances;
import ai.libs.mlplan.core.MLPlan;
import ai.libs.mlplan.multiclass.MLPlanClassifierConfig;
import ai.libs.mlplan.multilabel.mekamlplan.MLPlanMekaBuilder;
import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.core.MLUtils;
import weka.core.Instances;

/**
 * Experimenter for ML2PLan & AutoMLC
 *
 * @author helegraf, mwever
 *
 */
public class ML2PlanAutoMLCExperimenter implements IExperimentSetEvaluator {

	private static final ML2PlanAutoMLCExperimenterConfig CONFIG = ConfigCache.getOrCreate(ML2PlanAutoMLCExperimenterConfig.class);

	/* Logging */
	private Logger logger = LoggerFactory.getLogger(ML2PlanAutoMLCExperimenter.class);

	public ML2PlanAutoMLCExperimenter() {
		// nothing to do here
	}

	@Override
	public void evaluate(final ExperimentDBEntry experimentEntry, final IExperimentIntermediateResultProcessor processor) throws ExperimentEvaluationFailedException {
		try (SQLAdapter adapter = new SQLAdapter(CONFIG.getDBHost(), CONFIG.getDBUsername(), CONFIG.getDBPassword(), CONFIG.getDBDatabaseName())) {
			this.logger.info("Experiment ID: {}", experimentEntry.getId());
			this.logger.info("Experiment Description: {}", experimentEntry.getExperiment().getValuesOfKeyFields());

			Map<String, String> experimentDescription = experimentEntry.getExperiment().getValuesOfKeyFields();

			// Load dataset and prepare the dataset to be ready for multi-label classification
			File datasetFile = new File(CONFIG.getDatasetFolder(), experimentDescription.get("dataset") + ".arff");
			Instances data;
			try {
				data = new Instances(new FileReader(datasetFile));
				MLUtils.prepareData(data);
			} catch (Exception e) {
				throw new ExperimentEvaluationFailedException(e);
			}

			// Get train / test splits
			String splitDescriptionTrainTest = experimentDescription.get("test_split_tech");
			String testFold = experimentDescription.get("test_fold");
			String testSeed = experimentDescription.get("seed");

			IWekaInstances dataset = new WekaInstances(data);
			List<IWekaInstances> trainTestSplit = WekaUtil.realizeSplit(dataset, WekaUtil.getArbitrarySplit(dataset, new Random(Integer.parseInt(testSeed)), 0.7));

			Timeout mlplanTimeOut = new Timeout(Integer.parseInt(experimentDescription.get("timeout")), TimeUnit.MINUTES);
			Timeout nodeEvalTimeOut = new Timeout(Integer.parseInt(experimentDescription.get("node_timeout")), TimeUnit.MINUTES);

			// Prepare connection
			ResultsDBConnection connection = new ResultsDBConnection("intermediate_measurements", "final_measurements", "ordered_metric", experimentEntry.getId(), "ML2Plan", adapter);

			// Evaluation: test
			this.logger.info("Now test...");

			MLPlanMekaBuilder builder = new MLPlanMekaBuilder();
			builder.withNodeEvaluationTimeOut(nodeEvalTimeOut);
			builder.withCandidateEvaluationTimeOut(nodeEvalTimeOut);

			int metricIdToOptimize = Integer.parseInt(experimentDescription.get("metric_id"));
			switch (metricIdToOptimize) {
			case 8: // rank loss
				builder.withPerformanceMeasure(new RankLoss());
				break;
			case 1: // hamming
				builder.withPerformanceMeasure(new Hamming());
				break;
			case 62: // F1Measure avgd by instances
				builder.withPerformanceMeasure(new InstanceWiseF1());
				break;
			case 74: // F1Measure avgd by labels (standard F1 measure for MLC)
				builder.withPerformanceMeasure(new F1MacroAverageL());
				break;
			case 73: // fitness
			default:
				builder.withPerformanceMeasure(new AutoMEKAGGPFitnessMeasureLoss());
				break;
			}

			MLPlanClassifierConfig algoConfig = builder.getAlgorithmConfig();
			algoConfig.setProperty(MLPlanClassifierConfig.SELECTION_PORTION, "0.3");
			algoConfig.setProperty(MLPlanClassifierConfig.K_RANDOM_COMPLETIONS_NUM, "3");

			builder.withAlgorithmConfig(algoConfig);
			builder.withTimeOut(mlplanTimeOut);
			builder.withNumCpus(CONFIG.getNumberOfCPUs());
			builder.withDataset(trainTestSplit.get(0));

			MLPlan<IMekaClassifier> mlplan = null;

			try {
				mlplan = builder.build();
				mlplan.setLoggerName("ml2plan");

				MultiLabelClassifier classifier;
				try {
					mlplan.call();
				} catch (AlgorithmTimeoutedException e) {
					this.logger.warn("MLPlan got a delayed timeout exception", e);
				} finally {
					classifier = (MultiLabelClassifier) mlplan.getSelectedClassifier();
				}

				if (classifier == null) {
					throw new NullPointerException("No classifier was found by ML2Plan");
				}

				// mlplan.getComponentInstanceOfSelectedClassifier().getParameterValue("threshold");

				this.logger.info("Evaluate classifier...");
				//				Result result = Evaluation.evaluateModel(classifier, trainTestSplit.get(0), trainTestSplit.get(1), "PCutL");
				this.logger.info("Done evaluating Classifier.");
				this.logger.info("Store results in DB...");
				// HashMap<String, Double> metrics = new HashMap<>();
				// ClassifierMetricGetter.getMultiLabelMetrics().forEach(metric -> {
				// try {
				// metrics.put(metric, ClassifierMetricGetter.getValueOfMultilabelClassifier(result, metric));
				// } catch (Exception e) {
				// this.logger.warn("Could not measure metric {} for final classifier choice.", e);
				// }
				// });
				// connection.addFinalMeasurements(metrics);
				this.logger.info("Stored results in DB.");
				this.logger.info("Done with evaluation. Send job result.");
				Map<String, Object> results = new HashMap<>();

				//				double extFitness = new AutoMekaGGPFitness().calculateMeasure(result.allPredictions(), result.allTrueValues());
				//				double extHamming = Metrics.L_Hamming(result.allTrueValues(), result.allPredictions(0.5));
				//				double extAccuracy = Metrics.P_Accuracy(result.allTrueValues(), result.allPredictions(0.5));
				//				double extRank = Metrics.L_RankLoss(result.allTrueValues(), result.allPredictions());
				//				double extJaccard = Metrics.P_JaccardIndex(result.allTrueValues(), result.allPredictions(0.5));
				//				double extInstanceF1 = Metrics.P_FmacroAvgD(result.allTrueValues(), result.allPredictions(0.5));

				Stack<ComponentInstance> classifierNameStack = new Stack<>();
				classifierNameStack.push(mlplan.getComponentInstanceOfSelectedClassifier());

				StringBuilder classifierName = new StringBuilder();
				while (!classifierNameStack.isEmpty()) {
					ComponentInstance ci = classifierNameStack.pop();
					if (!classifierName.toString().isEmpty()) {
						classifierName.append(" - ");
					}

					classifierName.append(ci.getComponent().getName());
					ci.getSatisfactionOfRequiredInterfaces().values().stream().forEach(classifierNameStack::push);
				}

				results.put("completed", true);
				results.put("classifier_name", mlplan.getComponentInstanceOfSelectedClassifier().getComponent().getName());
				results.put("classifier_string", mlplan.getComponentInstanceOfSelectedClassifier() + "");

				results.put("intValue", mlplan.getInternalValidationErrorOfSelectedClassifier());

				//				results.put("extFitness", extFitness);
				//				results.put("extHamming", extHamming);
				//				results.put("extAccuracy", extAccuracy);
				//				results.put("extInstanceF1", extInstanceF1);
				//				results.put("extJaccard", extJaccard);
				//				results.put("extRank", extRank);

				processor.processResults(results);

				this.logger.info("Evaluation task completed.");
			} catch (Exception e) {
				throw new ExperimentEvaluationFailedException(e);
			} finally {
				if (mlplan != null) {
					mlplan.cancel();
				}
			}
		} catch (IOException e) {
			throw new ExperimentEvaluationFailedException(e);
		}
	}

	public static void main(final String[] args) throws ExperimentDBInteractionFailedException, InterruptedException {
		IExperimentDatabaseHandle dbHandle = new ExperimenterMySQLHandle(CONFIG);
		ExperimentRunner runner = new ExperimentRunner(CONFIG, new ML2PlanAutoMLCExperimenter(), dbHandle);
		runner.randomlyConductExperiments(1);
		System.exit(0);
	}

}