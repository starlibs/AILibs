package autofe.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataSetDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.algorithm.hasco.HASCOFE;
import autofe.algorithm.hasco.HASCOFE.HASCOFESolution;
import autofe.algorithm.hasco.evaluation.COCONodeEvaluator;
import autofe.algorithm.hasco.evaluation.COCOObjectEvaluator;
import autofe.algorithm.hasco.evaluation.ClusterNodeEvaluator;
import autofe.algorithm.hasco.evaluation.ClusterObjectEvaluator;
import autofe.algorithm.hasco.evaluation.EnsembleNodeEvaluator;
import autofe.algorithm.hasco.evaluation.EnsembleObjectEvaluator;
import autofe.algorithm.hasco.evaluation.LDANodeEvaluator;
import autofe.algorithm.hasco.evaluation.LDAObjectEvaluator;
import autofe.util.DataSet;
import autofe.util.DataSetUtils;
import jaicore.ml.WekaUtil;
import weka.core.Instance;
import weka.core.Instances;

public class ImageAutoFETest extends AutoFETest {
	private static final Logger logger = LoggerFactory.getLogger(ImageAutoFETest.class);

	private static final int MLPLAN_TIMEOUT = 60;
	private static final int AUTOFE_TIMEOUT = 60 * 1000;

	private static final int MAX_PIPELINE_SIZE = 12;

	private static final int USED_DATASET = DataSetUtils.MNIST_ID;

	// @Test
	public void testImageAutoFEClusterEval() throws Exception {
		logger.info("Starting Image AutoFE test...");

		/* load image dataset and create a train-test-split */
		OpenmlConnector connector = new OpenmlConnector();
		DataSetDescription ds = connector.dataGet(USED_DATASET);
		File file = ds.getDataset(API_KEY);
		Instances data = new Instances(new BufferedReader(new FileReader(file)));
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit(data, new Random(42), .01f);

		logger.info("Calculating intermediates...");
		List<INDArray> intermediate = new ArrayList<>();
		for (Instance inst : split.get(0)) {
			intermediate.add(DataSetUtils.instanceToMatrixByDataSet(inst, USED_DATASET));
		}
		logger.info("Finished intermediate calculations.");

		HASCOFE hascoFE = new HASCOFE(new File("model/catalano/catalano.json"),
				new ClusterNodeEvaluator(MAX_PIPELINE_SIZE), new DataSet(split.get(0), intermediate),
				new ClusterObjectEvaluator());
		// hascoFE.enableVisualization();
		hascoFE.setLoggerName("autofe image");
		hascoFE.runSearch(AUTOFE_TIMEOUT);
		HASCOFESolution solution = hascoFE.getCurrentlyBestSolution();
		if (solution != null) {
			logger.info(solution.toString());
			logger.info(hascoFE.getFoundClassifiers().toString());

			logger.info("Testing result features using MLPlan...");
			List<Instances> newSplit = WekaUtil.getStratifiedSplit(split.get(0), new Random(42), .7f);
			List<INDArray> intermediateSplit0 = new ArrayList<>();
			for (Instance inst : newSplit.get(0)) {
				intermediateSplit0.add(DataSetUtils.instanceToMatrixByDataSet(inst, USED_DATASET));
			}
			List<INDArray> intermediateSplit1 = new ArrayList<>();
			for (Instance inst : newSplit.get(1)) {
				intermediateSplit1.add(DataSetUtils.instanceToMatrixByDataSet(inst, USED_DATASET));
			}

			DataSet resultSplit0 = solution.getSolution().applyFilter(new DataSet(newSplit.get(0), intermediateSplit0),
					false);
			DataSet resultSplit1 = solution.getSolution().applyFilter(new DataSet(newSplit.get(1), intermediateSplit1),
					false);

			double mlPlanResult = evaluateMLPlan(MLPLAN_TIMEOUT, resultSplit0.getInstances(),
					resultSplit1.getInstances(), logger);
			System.out.println(
					"Error Rate of the solution produced by ML-Plan (Cluster): " + (100 - mlPlanResult) / 100f);

		} else
			logger.info("No solution could be found.");

	}

	// @Test
	public void testImageAutoFELDAEvaluator() throws Exception {
		logger.info("Starting Image AutoFE test...");

		/* load image dataset and create a train-test-split */
		OpenmlConnector connector = new OpenmlConnector();
		DataSetDescription ds = connector.dataGet(USED_DATASET);
		File file = ds.getDataset(API_KEY);
		Instances data = new Instances(new BufferedReader(new FileReader(file)));
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit(data, new Random(42), .01f);

		logger.info("Calculating intermediates...");
		List<INDArray> intermediate = new ArrayList<>();
		for (Instance inst : split.get(0)) {
			intermediate.add(DataSetUtils.instanceToMatrixByDataSet(inst, USED_DATASET));
		}
		logger.info("Finished intermediate calculations.");

		HASCOFE hascoFE = new HASCOFE(new File("model/catalano/catalano.json"), new LDANodeEvaluator(MAX_PIPELINE_SIZE),
				new DataSet(split.get(0), intermediate), new LDAObjectEvaluator());
		// hascoFE.enableVisualization();
		hascoFE.setLoggerName("autofe image");
		hascoFE.runSearch(AUTOFE_TIMEOUT);
		HASCOFESolution solution = hascoFE.getCurrentlyBestSolution();
		if (solution != null) {
			logger.info(solution.toString());
			logger.info(hascoFE.getFoundClassifiers().toString());

			logger.info("Testing result features using MLPlan...");
			List<Instances> newSplit = WekaUtil.getStratifiedSplit(split.get(0), new Random(42), .7f);
			List<INDArray> intermediateSplit0 = new ArrayList<>();
			for (Instance inst : newSplit.get(0)) {
				intermediateSplit0.add(DataSetUtils.instanceToMatrixByDataSet(inst, USED_DATASET));
			}
			List<INDArray> intermediateSplit1 = new ArrayList<>();
			for (Instance inst : newSplit.get(1)) {
				intermediateSplit1.add(DataSetUtils.instanceToMatrixByDataSet(inst, USED_DATASET));
			}

			DataSet resultSplit0 = solution.getSolution().applyFilter(new DataSet(newSplit.get(0), intermediateSplit0),
					false);
			DataSet resultSplit1 = solution.getSolution().applyFilter(new DataSet(newSplit.get(1), intermediateSplit1),
					false);

			logger.info("Feature size of generated solution: " + resultSplit0.getInstances().numAttributes());

			double mlPlanResult = evaluateMLPlan(MLPLAN_TIMEOUT, resultSplit0.getInstances(),
					resultSplit1.getInstances(), logger);
			System.out.println("Error Rate of the solution produced by ML-Plan (LDA): " + (100 - mlPlanResult) / 100f);

		} else
			logger.info("No solution could be found.");

	}

	@Test
	public void testImageAutoFERandomEvaluator() throws Exception {
		logger.info("Starting Image AutoFE test...");

		/* load image dataset and create a train-test-split */
		OpenmlConnector connector = new OpenmlConnector();
		DataSetDescription ds = connector.dataGet(USED_DATASET);
		File file = ds.getDataset(API_KEY);
		Instances data = new Instances(new BufferedReader(new FileReader(file)));
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit(data, new Random(42), .01f);

		logger.info("Calculating intermediates...");
		List<INDArray> intermediate = new ArrayList<>();
		for (Instance inst : split.get(0)) {
			intermediate.add(DataSetUtils.instanceToMatrixByDataSet(inst, USED_DATASET));
		}
		logger.info("Finished intermediate calculations.");

		HASCOFE hascoFE = new HASCOFE(new File("model/catalano/catalano.json"),
				getRandomNodeEvaluator(MAX_PIPELINE_SIZE), new DataSet(split.get(0), intermediate), null);
		// hascoFE.enableVisualization();
		hascoFE.setLoggerName("autofe image");
		hascoFE.runSearch(AUTOFE_TIMEOUT);
		HASCOFESolution solution = hascoFE.getCurrentlyBestSolution();
		if (solution != null) {
			logger.info(solution.toString());
			logger.info(hascoFE.getFoundClassifiers().toString());

			logger.info("Testing result features using MLPlan...");
			List<Instances> newSplit = WekaUtil.getStratifiedSplit(split.get(0), new Random(42), .7f);
			List<INDArray> intermediateSplit0 = new ArrayList<>();
			for (Instance inst : newSplit.get(0)) {
				intermediateSplit0.add(DataSetUtils.instanceToMatrixByDataSet(inst, USED_DATASET));
			}
			List<INDArray> intermediateSplit1 = new ArrayList<>();
			for (Instance inst : newSplit.get(1)) {
				intermediateSplit1.add(DataSetUtils.instanceToMatrixByDataSet(inst, USED_DATASET));
			}

			DataSet resultSplit0 = solution.getSolution().applyFilter(new DataSet(newSplit.get(0), intermediateSplit0),
					false);
			DataSet resultSplit1 = solution.getSolution().applyFilter(new DataSet(newSplit.get(1), intermediateSplit1),
					false);

			double mlPlanResult = evaluateMLPlan(MLPLAN_TIMEOUT, resultSplit0.getInstances(),
					resultSplit1.getInstances(), logger);
			System.out
					.println("Error Rate of the solution produced by ML-Plan (Random): " + (100 - mlPlanResult) / 100f);

		} else
			logger.info("No solution could be found.");

	}

	// @Test
	public void testImageAutoFEEnsembleEvaluator() throws Exception {
		logger.info("Starting Image AutoFE test...");

		/* load image dataset and create a train-test-split */
		OpenmlConnector connector = new OpenmlConnector();
		DataSetDescription ds = connector.dataGet(USED_DATASET);
		File file = ds.getDataset(API_KEY);
		Instances data = new Instances(new BufferedReader(new FileReader(file)));
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit(data, new Random(42), .01f);

		logger.info("Calculating intermediates...");
		List<INDArray> intermediate = new ArrayList<>();
		for (Instance inst : split.get(0)) {
			intermediate.add(DataSetUtils.instanceToMatrixByDataSet(inst, USED_DATASET));
		}
		logger.info("Finished intermediate calculations.");

		HASCOFE hascoFE = new HASCOFE(new File("model/catalano/catalano.json"),
				new EnsembleNodeEvaluator(MAX_PIPELINE_SIZE), new DataSet(split.get(0), intermediate),
				new EnsembleObjectEvaluator());
		// hascoFE.enableVisualization();
		hascoFE.setLoggerName("autofe image");
		hascoFE.runSearch(AUTOFE_TIMEOUT);
		HASCOFESolution solution = hascoFE.getCurrentlyBestSolution();
		if (solution != null) {
			logger.info(solution.toString());
			logger.info(hascoFE.getFoundClassifiers().toString());

			logger.info("Testing result features using MLPlan...");
			List<Instances> newSplit = WekaUtil.getStratifiedSplit(split.get(0), new Random(42), .7f);
			List<INDArray> intermediateSplit0 = new ArrayList<>();
			for (Instance inst : newSplit.get(0)) {
				intermediateSplit0.add(DataSetUtils.instanceToMatrixByDataSet(inst, USED_DATASET));
			}
			List<INDArray> intermediateSplit1 = new ArrayList<>();
			for (Instance inst : newSplit.get(1)) {
				intermediateSplit1.add(DataSetUtils.instanceToMatrixByDataSet(inst, USED_DATASET));
			}

			DataSet resultSplit0 = solution.getSolution().applyFilter(new DataSet(newSplit.get(0), intermediateSplit0),
					false);
			DataSet resultSplit1 = solution.getSolution().applyFilter(new DataSet(newSplit.get(1), intermediateSplit1),
					false);

			logger.info("Feature size of generated solution: " + resultSplit0.getInstances().numAttributes());

			double mlPlanResult = evaluateMLPlan(MLPLAN_TIMEOUT, resultSplit0.getInstances(),
					resultSplit1.getInstances(), logger);
			System.out.println(
					"Error Rate of the solution produced by ML-Plan (Ensemble): " + (100 - mlPlanResult) / 100f);

		} else
			logger.info("No solution could be found.");
	}

	// @Test
	public void testImageAutoFECOCOEvaluator() throws Exception {
		logger.info("Starting Image AutoFE test...");

		/* load image dataset and create a train-test-split */
		OpenmlConnector connector = new OpenmlConnector();
		DataSetDescription ds = connector.dataGet(USED_DATASET);
		File file = ds.getDataset(API_KEY);
		Instances data = new Instances(new BufferedReader(new FileReader(file)));
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit(data, new Random(42), .01f);

		logger.info("Calculating intermediates...");
		List<INDArray> intermediate = new ArrayList<>();
		for (Instance inst : split.get(0)) {
			intermediate.add(DataSetUtils.instanceToMatrixByDataSet(inst, USED_DATASET));
		}
		logger.info("Finished intermediate calculations.");

		HASCOFE hascoFE = new HASCOFE(new File("model/catalano/catalano.json"),
				new COCONodeEvaluator(MAX_PIPELINE_SIZE), new DataSet(split.get(0), intermediate),
				new COCOObjectEvaluator());
		// hascoFE.enableVisualization();
		hascoFE.setLoggerName("autofe image");
		hascoFE.runSearch(AUTOFE_TIMEOUT);
		HASCOFESolution solution = hascoFE.getCurrentlyBestSolution();
		if (solution != null) {
			logger.info(solution.toString());
			logger.info(hascoFE.getFoundClassifiers().toString());

			logger.info("Testing result features using MLPlan...");
			List<Instances> newSplit = WekaUtil.getStratifiedSplit(split.get(0), new Random(42), .7f);
			List<INDArray> intermediateSplit0 = new ArrayList<>();
			for (Instance inst : newSplit.get(0)) {
				intermediateSplit0.add(DataSetUtils.instanceToMatrixByDataSet(inst, USED_DATASET));
			}
			List<INDArray> intermediateSplit1 = new ArrayList<>();
			for (Instance inst : newSplit.get(1)) {
				intermediateSplit1.add(DataSetUtils.instanceToMatrixByDataSet(inst, USED_DATASET));
			}

			DataSet resultSplit0 = solution.getSolution().applyFilter(new DataSet(newSplit.get(0), intermediateSplit0),
					false);
			DataSet resultSplit1 = solution.getSolution().applyFilter(new DataSet(newSplit.get(1), intermediateSplit1),
					false);

			logger.info("Feature size of generated solution: " + resultSplit0.getInstances().numAttributes());

			double mlPlanResult = evaluateMLPlan(MLPLAN_TIMEOUT, resultSplit0.getInstances(),
					resultSplit1.getInstances(), logger);
			System.out.println("Error Rate of the solution produced by ML-Plan (COCO): " + (100 - mlPlanResult) / 100f);

		} else
			logger.info("No solution could be found.");
	}
}
