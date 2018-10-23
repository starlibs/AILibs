package autofe.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Random;

import org.junit.Test;
import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataSetDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.algorithm.hasco.HASCOFeatureEngineering;
import autofe.algorithm.hasco.HASCOFeatureEngineering.HASCOFESolution;
import autofe.algorithm.hasco.evaluation.COCONodeEvaluator;
import autofe.algorithm.hasco.evaluation.COCOObjectEvaluator;
import autofe.algorithm.hasco.evaluation.ClusterNodeEvaluator;
import autofe.algorithm.hasco.evaluation.ClusterObjectEvaluator;
import autofe.algorithm.hasco.evaluation.EnsembleNodeEvaluator;
import autofe.algorithm.hasco.evaluation.EnsembleObjectEvaluator;
import autofe.algorithm.hasco.evaluation.LDANodeEvaluator;
import autofe.util.DataSet;
import autofe.util.DataSetUtils;
import autofe.util.EvaluationUtils;
import jaicore.ml.WekaUtil;
import weka.core.Instances;

public class SimpleAutoFETest {
	private static final int MLPLAN_TIMEOUT = 30;

	private static final int MAX_PIPELINE_SIZE = 20;

	private static final Logger logger = LoggerFactory.getLogger(SimpleAutoFETest.class);

	private static final int USED_DATASET = DataSetUtils.SEGMENT_ID;
	// Shape has only to be used if pretrained neural nets are used
	private static final long[] DATASET_INPUT_SHAPE = null;

	private static final boolean ENABLE_MLPLAN_VIS = true;

	// @Test
	public void testHASCOClusterNodeEvaluator() throws Exception {
		logger.info("Starting AutoFE test...");

		/* load data for segment dataset and create a train-test-split */
		OpenmlConnector connector = new OpenmlConnector();
		DataSetDescription ds = connector.dataGet(USED_DATASET);
		File file = ds.getDataset(DataSetUtils.API_KEY);
		Instances data = new Instances(new BufferedReader(new FileReader(file)));
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit(data, new Random(0), .7f);

		HASCOFeatureEngineering hascoFE = new HASCOFeatureEngineering(new File("model/test.json"),
				new ClusterNodeEvaluator(MAX_PIPELINE_SIZE), new DataSet(split.get(0), null),
				new ClusterObjectEvaluator(), DATASET_INPUT_SHAPE);
		hascoFE.setLoggerName("autofe");
		// hascoFE.enableVisualization();
		hascoFE.runSearch(60 * 1000);
		HASCOFESolution solution = hascoFE.getCurrentlyBestSolution();

		logger.info(hascoFE.getFoundClassifiers().toString());
		if (solution != null) {
			logger.info(solution.toString());
			logger.info(hascoFE.getFoundClassifiers().toString());

			logger.info("Testing result features using MLPlan...");
			List<Instances> newSplit = WekaUtil.getStratifiedSplit(data, new Random(42), .7f);

			DataSet resultSplit0 = solution.getSolution().applyFilter(new DataSet(newSplit.get(0), null), false);
			DataSet resultSplit1 = solution.getSolution().applyFilter(new DataSet(newSplit.get(1), null), false);

			double mlPlanResult = EvaluationUtils.evaluateMLPlan(MLPLAN_TIMEOUT, resultSplit0.getInstances(),
					resultSplit1.getInstances(), 42, logger, ENABLE_MLPLAN_VIS);
			System.out.println(
					"Error Rate of the solution produced by ML-Plan (Cluster): " + (100 - mlPlanResult) / 100f);
		} else
			logger.info("No solution could be found.");
	}

	// @Test
	public void testHASCORandomNodeEvaluator() throws Exception {
		logger.info("Starting AutoFE test...");

		/* load data for segment dataset and create a train-test-split */
		OpenmlConnector connector = new OpenmlConnector();
		DataSetDescription ds = connector.dataGet(USED_DATASET);
		File file = ds.getDataset(DataSetUtils.API_KEY);
		Instances data = new Instances(new BufferedReader(new FileReader(file)));
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit(data, new Random(0), .7f);

		HASCOFeatureEngineering hascoFE = new HASCOFeatureEngineering(new File("model/test.json"),
				EvaluationUtils.getRandomNodeEvaluator(MAX_PIPELINE_SIZE), new DataSet(split.get(0), null),
				new ClusterObjectEvaluator(), DATASET_INPUT_SHAPE);
		hascoFE.setLoggerName("autofe");
		// hascoFE.enableVisualization();
		hascoFE.runSearch(60 * 1000);
		HASCOFESolution solution = hascoFE.getCurrentlyBestSolution();

		logger.info(hascoFE.getFoundClassifiers().toString());
		if (solution != null) {
			logger.info(solution.toString());
			logger.info(hascoFE.getFoundClassifiers().toString());

			logger.info("Testing result features using MLPlan...");
			List<Instances> newSplit = WekaUtil.getStratifiedSplit(data, new Random(42), .7f);

			DataSet resultSplit0 = solution.getSolution().applyFilter(new DataSet(newSplit.get(0), null), false);
			DataSet resultSplit1 = solution.getSolution().applyFilter(new DataSet(newSplit.get(1), null), false);

			double mlPlanResult = EvaluationUtils.evaluateMLPlan(MLPLAN_TIMEOUT, resultSplit0.getInstances(),
					resultSplit1.getInstances(), 42, logger, ENABLE_MLPLAN_VIS);
			System.out
					.println("Error Rate of the solution produced by ML-Plan (Random): " + (100 - mlPlanResult) / 100f);
		} else
			logger.info("No solution could be found.");
	}

	// @Test
	public void testHASCOLDANodeEvaluator() throws Exception {
		logger.info("Starting AutoFE test...");

		/* load data for segment dataset and create a train-test-split */
		OpenmlConnector connector = new OpenmlConnector();
		DataSetDescription ds = connector.dataGet(USED_DATASET);
		File file = ds.getDataset(DataSetUtils.API_KEY);
		Instances data = new Instances(new BufferedReader(new FileReader(file)));
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit(data, new Random(0), .7f);

		HASCOFeatureEngineering hascoFE = new HASCOFeatureEngineering(new File("model/test.json"),
				new LDANodeEvaluator(MAX_PIPELINE_SIZE), new DataSet(split.get(0), null), new ClusterObjectEvaluator(),
				DATASET_INPUT_SHAPE);
		hascoFE.setLoggerName("autofe");
		// hascoFE.enableVisualization();
		hascoFE.runSearch(60 * 1000);
		HASCOFESolution solution = hascoFE.getCurrentlyBestSolution();

		logger.info(hascoFE.getFoundClassifiers().toString());
		if (solution != null) {
			logger.info(solution.toString());
			logger.info(hascoFE.getFoundClassifiers().toString());

			logger.info("Testing result features using MLPlan...");
			List<Instances> newSplit = WekaUtil.getStratifiedSplit(data, new Random(42), .7f);

			DataSet resultSplit0 = solution.getSolution().applyFilter(new DataSet(newSplit.get(0), null), false);
			DataSet resultSplit1 = solution.getSolution().applyFilter(new DataSet(newSplit.get(1), null), false);

			double mlPlanResult = EvaluationUtils.evaluateMLPlan(MLPLAN_TIMEOUT, resultSplit0.getInstances(),
					resultSplit1.getInstances(), 42, logger, ENABLE_MLPLAN_VIS);
			System.out.println("Error Rate of the solution produced by ML-Plan (LDA): " + (100 - mlPlanResult) / 100f);
		} else
			logger.info("No solution could be found.");
	}

	// @Test
	public void testHASCOEnsembleNodeEvaluator() throws Exception {
		logger.info("Starting AutoFE test...");

		/* load data for segment dataset and create a train-test-split */
		OpenmlConnector connector = new OpenmlConnector();
		DataSetDescription ds = connector.dataGet(USED_DATASET);
		File file = ds.getDataset(DataSetUtils.API_KEY);
		Instances data = new Instances(new BufferedReader(new FileReader(file)));
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit(data, new Random(0), .7f);

		HASCOFeatureEngineering hascoFE = new HASCOFeatureEngineering(new File("model/test.json"),
				new EnsembleNodeEvaluator(MAX_PIPELINE_SIZE), new DataSet(split.get(0), null),
				new EnsembleObjectEvaluator(), DATASET_INPUT_SHAPE);
		hascoFE.setLoggerName("autofe");
		// hascoFE.enableVisualization();
		hascoFE.runSearch(60 * 1000);
		HASCOFESolution solution = hascoFE.getCurrentlyBestSolution();

		logger.info(hascoFE.getFoundClassifiers().toString());
		if (solution != null) {
			logger.info(solution.toString());
			logger.info(hascoFE.getFoundClassifiers().toString());

			logger.info("Testing result features using MLPlan...");
			List<Instances> newSplit = WekaUtil.getStratifiedSplit(data, new Random(42), .7f);

			DataSet resultSplit0 = solution.getSolution().applyFilter(new DataSet(newSplit.get(0), null), false);
			DataSet resultSplit1 = solution.getSolution().applyFilter(new DataSet(newSplit.get(1), null), false);

			double mlPlanResult = EvaluationUtils.evaluateMLPlan(MLPLAN_TIMEOUT, resultSplit0.getInstances(),
					resultSplit1.getInstances(), 42, logger, ENABLE_MLPLAN_VIS);
			System.out.println(
					"Error Rate of the solution produced by ML-Plan (Ensemble): " + (100 - mlPlanResult) / 100f);
		} else
			logger.info("No solution could be found.");
	}

	@Test
	public void testHASCOCOCONodeEvaluator() throws Exception {
		logger.info("Starting AutoFE test...");

		/* load data for segment dataset and create a train-test-split */
		OpenmlConnector connector = new OpenmlConnector();
		DataSetDescription ds = connector.dataGet(USED_DATASET);
		File file = ds.getDataset(DataSetUtils.API_KEY);
		Instances data = new Instances(new BufferedReader(new FileReader(file)));
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit(data, new Random(0), .7f);

		HASCOFeatureEngineering hascoFE = new HASCOFeatureEngineering(new File("model/test.json"),
				new COCONodeEvaluator(MAX_PIPELINE_SIZE), new DataSet(split.get(0), null), new COCOObjectEvaluator(),
				DATASET_INPUT_SHAPE);
		hascoFE.setLoggerName("autofe");
		// hascoFE.enableVisualization();
		hascoFE.runSearch(60 * 1000);
		HASCOFESolution solution = hascoFE.getCurrentlyBestSolution();

		logger.info(hascoFE.getFoundClassifiers().toString());
		if (solution != null) {
			logger.info(solution.toString());
			logger.info(hascoFE.getFoundClassifiers().toString());

			logger.info("Testing result features using MLPlan...");
			List<Instances> newSplit = WekaUtil.getStratifiedSplit(data, new Random(42), .7f);

			DataSet resultSplit0 = solution.getSolution().applyFilter(new DataSet(newSplit.get(0), null), false);
			DataSet resultSplit1 = solution.getSolution().applyFilter(new DataSet(newSplit.get(1), null), false);

			double mlPlanResult = EvaluationUtils.evaluateMLPlan(MLPLAN_TIMEOUT, resultSplit0.getInstances(),
					resultSplit1.getInstances(), 42, logger, ENABLE_MLPLAN_VIS);
			System.out.println("Error Rate of the solution produced by ML-Plan (COCO): " + (100 - mlPlanResult) / 100f);
		} else
			logger.info("No solution could be found.");
	}
}
