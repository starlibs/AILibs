package de.upb.crc901.mlplan.examples.multilabel.meka;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.aeonbits.owner.ConfigCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.mlplan.core.MLPlan;
import de.upb.crc901.mlplan.core.MLPlanBuilder;
import de.upb.crc901.mlplan.multiclass.MLPlanClassifierConfig;
import hasco.gui.statsplugin.HASCOModelStatisticsPlugin;
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
import jaicore.ml.core.evaluation.measure.multilabel.AutoMEKAGGPFitnessMeasure;
import jaicore.ml.core.evaluation.measure.multilabel.AutoMEKAGGPFitnessMeasureLoss;
import jaicore.ml.core.evaluation.measure.multilabel.F1MacroAverageLLoss;
import jaicore.ml.core.evaluation.measure.multilabel.HammingLoss;
import jaicore.ml.core.evaluation.measure.multilabel.InstanceWiseF1AsLoss;
import jaicore.ml.core.evaluation.measure.multilabel.RankLoss;
import jaicore.ml.evaluation.evaluators.weka.measurebridge.SimpleMLCEvaluatorMeasureBridge;
import jaicore.ml.wekautil.dataset.splitter.MultilabelDatasetSplitter;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNodeInfoGenerator;
import jaicore.search.gui.plugins.rollouthistograms.SearchRolloutHistogramPlugin;
import jaicore.search.model.travesaltree.JaicoreNodeInfoGenerator;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.core.MLUtils;
import meka.core.Metrics;
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
			Instances train = MultilabelDatasetSplitter.getTrainSplit(data, splitDescriptionTrainTest, testFold, testSeed);
			Instances test = MultilabelDatasetSplitter.getTestSplit(data, splitDescriptionTrainTest, testFold, testSeed);

			TimeOut mlplanTimeOut = new TimeOut(Integer.parseInt(experimentDescription.get("timeout")), TimeUnit.MINUTES);
			TimeOut nodeEvalTimeOut = new TimeOut(Integer.parseInt(experimentDescription.get("node_timeout")), TimeUnit.MINUTES);

			// Prepare connection
			ResultsDBConnection connection = new ResultsDBConnection("intermediate_measurements", "final_measurements", "ordered_metric", experimentEntry.getId(), "ML2Plan", adapter);

			// Evaluation: test
			this.logger.info("Now test...");

			MLPlanBuilder builder = new MLPlanBuilder();
			builder.withMekaDefaultConfiguration();
			builder.withTimeoutForNodeEvaluation(nodeEvalTimeOut);
			builder.withTimeoutForSingleSolutionEvaluation(nodeEvalTimeOut);

			int metricIdToOptimize = Integer.parseInt(experimentDescription.get("metric_id"));
			switch (metricIdToOptimize) {
			case 8: // rank loss
				builder.withEvaluatorMeasureBridge(new SimpleMLCEvaluatorMeasureBridge(new RankLoss()));
				break;
			case 1: // hamming
				builder.withEvaluatorMeasureBridge(new SimpleMLCEvaluatorMeasureBridge(new HammingLoss()));
				break;
			case 62: // F1Measure avgd by instances
				builder.withEvaluatorMeasureBridge(new SimpleMLCEvaluatorMeasureBridge(new InstanceWiseF1AsLoss()));
				break;
			case 74: // F1Measure avgd by labels (standard F1 measure for MLC)
				builder.withEvaluatorMeasureBridge(new SimpleMLCEvaluatorMeasureBridge(new F1MacroAverageLLoss()));
				break;
			case 73: // fitness
			default:
				builder.withEvaluatorMeasureBridge(new SimpleMLCEvaluatorMeasureBridge(new AutoMEKAGGPFitnessMeasureLoss()));
				break;
			}

			MLPlanClassifierConfig algoConfig = builder.getAlgorithmConfig();
			algoConfig.setProperty(MLPlanClassifierConfig.SELECTION_PORTION, "0.3");
			algoConfig.setProperty(MLPlanClassifierConfig.SEARCH_MCCV_ITERATIONS, "1");
			algoConfig.setProperty(MLPlanClassifierConfig.K_RANDOM_COMPLETIONS_NUM, "3");
			builder.withAlgorithmConfig(algoConfig);

			MLPlan mlplan = null;

			try {
				mlplan = new MLPlan(builder, train);
				mlplan.setTimeout(mlplanTimeOut);
				mlplan.setNumCPUs(CONFIG.getNumberOfCPUs());
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

				this.logger.info("Evaluate classifier...");
				// Result result = Evaluation.evaluateModel(classifier, train, test);
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

				MultiLabelClassifier h = (MultiLabelClassifier) mlplan.getSelectedClassifier();

				double[][] predictions = new double[test.size()][];
				int[][] intPred = new int[test.size()][];
				int[][] gt = new int[test.size()][];
				List<double[]> gtList = new LinkedList<>();

				for (int i = 0; i < test.size(); i++) {
					double[] dist = h.distributionForInstance(test.get(i));
					predictions[i] = dist;
					intPred[i] = Arrays.stream(dist).mapToInt(x -> (int) x).toArray();

					int[] gtVec = new int[test.classIndex()];
					for (int l = 0; l < test.classIndex(); l++) {
						gtVec[l] = (int) test.get(i).value(l);
					}
					gt[i] = gtVec;
					gtList.add(Arrays.stream(gtVec).mapToDouble(x -> (double) x).toArray());
				}
				List<double[]> predictionsList = Arrays.stream(predictions).collect(Collectors.toList());

				// resultfields = completed, classifier_name, classifier_string, intValue, extFitness, extHamming, extInstaceF1, extAccuracy, extRank, extJaccard

				double extFitness = new AutoMEKAGGPFitnessMeasure().calculateAvgMeasure(predictionsList, gtList);
				double extHamming = Metrics.L_Hamming(gt, intPred);
				double extAccuracy = Metrics.P_Accuracy(gt, intPred);
				double extRank = Metrics.L_RankLoss(gt, predictions);
				double extJaccard = Metrics.P_JaccardIndex(gt, intPred);
				double extInstanceF1 = Metrics.P_FmacroAvgD(gt, intPred);

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