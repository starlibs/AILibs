package de.upb.crc901.mlplan.examples.multilabel.meka;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.mlplan.core.AbstractMLPlanBuilder;
import de.upb.crc901.mlplan.core.MLPlan;
import de.upb.crc901.mlplan.core.MLPlanMekaBuilder;
import de.upb.crc901.mlplan.multiclass.MLPlanClassifierConfig;
import hasco.gui.statsplugin.HASCOModelStatisticsPlugin;
import hasco.model.ComponentInstance;
import jaicore.basic.SQLAdapter;
import jaicore.basic.TimeOut;
import jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;
import jaicore.experiments.ExperimentDBEntry;
import jaicore.experiments.ExperimentRunner;
import jaicore.experiments.IExperimentDatabaseHandle;
import jaicore.experiments.IExperimentIntermediateResultProcessor;
import jaicore.experiments.IExperimentSetEvaluator;
import jaicore.experiments.databasehandle.ExperimenterSQLHandle;
import jaicore.experiments.exceptions.ExperimentDBInteractionFailedException;
import jaicore.experiments.exceptions.ExperimentEvaluationFailedException;
import jaicore.experiments.exceptions.IllegalExperimentSetupException;
import jaicore.graphvisualizer.plugin.graphview.GraphViewPlugin;
import jaicore.graphvisualizer.plugin.nodeinfo.NodeInfoGUIPlugin;
import jaicore.graphvisualizer.plugin.solutionperformanceplotter.SolutionPerformanceTimelinePlugin;
import jaicore.graphvisualizer.window.AlgorithmVisualizationWindow;
import jaicore.ml.WekaUtil;
import jaicore.ml.core.evaluation.measure.multilabel.AutoMEKAGGPFitnessMeasureLoss;
import jaicore.ml.core.evaluation.measure.multilabel.AutoMekaGGPFitness;
import jaicore.ml.core.evaluation.measure.multilabel.F1MacroAverageLLoss;
import jaicore.ml.core.evaluation.measure.multilabel.HammingLoss;
import jaicore.ml.core.evaluation.measure.multilabel.InstanceWiseF1AsLoss;
import jaicore.ml.core.evaluation.measure.multilabel.RankLoss;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNodeInfoGenerator;
import jaicore.search.gui.plugins.rollouthistograms.SearchRolloutHistogramPlugin;
import jaicore.search.model.travesaltree.JaicoreNodeInfoGenerator;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import meka.classifiers.multilabel.Evaluation;
import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.core.MLUtils;
import meka.core.Metrics;
import meka.core.Result;
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

			List<Instances> trainTestSplit = WekaUtil.realizeSplit(data, WekaUtil.getArbitrarySplit(data, new Random(Integer.parseInt(testSeed)), 0.7));

			TimeOut mlplanTimeOut = new TimeOut(Integer.parseInt(experimentDescription.get("timeout")), TimeUnit.MINUTES);
			TimeOut nodeEvalTimeOut = new TimeOut(Integer.parseInt(experimentDescription.get("node_timeout")), TimeUnit.MINUTES);

			// Prepare connection
			ResultsDBConnection connection = new ResultsDBConnection("intermediate_measurements", "final_measurements", "ordered_metric", experimentEntry.getId(), "ML2Plan", adapter);

			// Evaluation: test
			this.logger.info("Now test...");

			MLPlanMekaBuilder builder = AbstractMLPlanBuilder.forMeka();
			builder.withNodeEvaluationTimeOut(nodeEvalTimeOut);
			builder.withCandidateEvaluationTimeOut(nodeEvalTimeOut);

			int metricIdToOptimize = Integer.parseInt(experimentDescription.get("metric_id"));
			switch (metricIdToOptimize) {
			case 8: // rank loss
				builder.withPerformanceMeasure(new RankLoss());
				break;
			case 1: // hamming
				builder.withPerformanceMeasure(new HammingLoss());
				break;
			case 62: // F1Measure avgd by instances
				builder.withPerformanceMeasure(new InstanceWiseF1AsLoss());
				break;
			case 74: // F1Measure avgd by labels (standard F1 measure for MLC)
				builder.withPerformanceMeasure(new F1MacroAverageLLoss());
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

			MLPlan mlplan = null;

			try {
				mlplan = new MLPlan(builder, trainTestSplit.get(0));
				mlplan.setLoggerName("ml2plan");

				if (CONFIG.showGUI()) {
					new JFXPanel();
					AlgorithmVisualizationWindow window = new AlgorithmVisualizationWindow(mlplan, new GraphViewPlugin(), new NodeInfoGUIPlugin<>(new JaicoreNodeInfoGenerator<>(new TFDNodeInfoGenerator())),
							new SearchRolloutHistogramPlugin<>(), new SolutionPerformanceTimelinePlugin(), new HASCOModelStatisticsPlugin());
					Platform.runLater(window);
				}

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
				Result result = Evaluation.evaluateModel(classifier, trainTestSplit.get(0), trainTestSplit.get(1), "PCutL");
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

				double extFitness = new AutoMekaGGPFitness().calculateMeasure(result.allPredictions(), result.allTrueValues());
				double extHamming = Metrics.L_Hamming(result.allTrueValues(), result.allPredictions(0.5));
				double extAccuracy = Metrics.P_Accuracy(result.allTrueValues(), result.allPredictions(0.5));
				double extRank = Metrics.L_RankLoss(result.allTrueValues(), result.allPredictions());
				double extJaccard = Metrics.P_JaccardIndex(result.allTrueValues(), result.allPredictions(0.5));
				double extInstanceF1 = Metrics.P_FmacroAvgD(result.allTrueValues(), result.allPredictions(0.5));

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

				results.put("extFitness", extFitness);
				results.put("extHamming", extHamming);
				results.put("extAccuracy", extAccuracy);
				results.put("extInstanceF1", extInstanceF1);
				results.put("extJaccard", extJaccard);
				results.put("extRank", extRank);

				processor.processResults(results);

				this.logger.info("Evaluation task completed.");
			} catch (Exception e) {
				e.printStackTrace();
				throw new ExperimentEvaluationFailedException(e);
			} finally {
				if (mlplan != null) {
					mlplan.cancel();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new ExperimentEvaluationFailedException(e);
		}
	}

	public static void main(final String[] args) throws ExperimentDBInteractionFailedException, IllegalExperimentSetupException {
		IExperimentDatabaseHandle dbHandle = new ExperimenterSQLHandle(CONFIG);
		ExperimentRunner runner = new ExperimentRunner(CONFIG, new ML2PlanAutoMLCExperimenter(), dbHandle);
		runner.randomlyConductExperiments(1, false);
		System.exit(0);
	}

}