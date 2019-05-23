package autofe.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.aeonbits.owner.ConfigFactory;
import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataSetDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.algorithm.hasco.HASCOFeatureEngineering;
import autofe.algorithm.hasco.HASCOFeatureEngineeringConfig;
import autofe.algorithm.hasco.evaluation.AbstractHASCOFEObjectEvaluator;
import autofe.algorithm.hasco.evaluation.COCOObjectEvaluator;
import autofe.algorithm.hasco.evaluation.ClusterObjectEvaluator;
import autofe.algorithm.hasco.evaluation.EnsembleObjectEvaluator;
import autofe.algorithm.hasco.evaluation.LDAObjectEvaluator;
import autofe.algorithm.hasco.filter.meta.FilterPipeline;
import autofe.algorithm.hasco.filter.meta.FilterPipelineFactory;
import autofe.util.DataSet;
import autofe.util.DataSetUtils;
import autofe.util.EvaluationUtils;
import jaicore.ml.WekaUtil;
import weka.core.Instance;
import weka.core.Instances;

public class ImageAutoFETest {
	private static final Logger logger = LoggerFactory.getLogger(ImageAutoFETest.class);

	private static final int MLPLAN_TIMEOUT = 60;
	private static final int AUTOFE_TIMEOUT = 30 * 1000;

	private static final int MAX_PIPELINE_SIZE = 5;

	private static final int USED_DATASET = DataSetUtils.MNIST_ID;
	private static final long[] DATASET_INPUT_SHAPE = DataSetUtils.getInputShapeByDataSet(USED_DATASET);

	private static final boolean ENABLE_MLPLAN_VIS = true;

	private static final File MODEL_FILE = new File("model/catalano/catalano.json");

	// @Test
	public void testImageAutoFEClusterEval() throws Exception {
		logger.info("Starting Image AutoFE test...");

		/* load image dataset and create a train-test-split */
		OpenmlConnector connector = new OpenmlConnector();
		DataSetDescription ds = connector.dataGet(USED_DATASET);
		File file = ds.getDataset(DataSetUtils.API_KEY);
		Instances data = new Instances(new BufferedReader(new FileReader(file)));
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit(data, 42, .01f);

		logger.info("Calculating intermediates...");
		List<INDArray> intermediate = new ArrayList<>();
		for (Instance inst : split.get(0)) {
			intermediate.add(DataSetUtils.instanceToMatrixByDataSet(inst, USED_DATASET));
		}
		logger.info("Finished intermediate calculations.");

		FilterPipeline solution = runHASCOImageFE(MODEL_FILE, new ClusterObjectEvaluator(),
				new DataSet(split.get(0), intermediate), DATASET_INPUT_SHAPE);

		if (solution != null) {
			logger.info(solution.toString());

			logger.info("Testing result features using MLPlan...");
			List<Instances> newSplit = WekaUtil.getStratifiedSplit(split.get(0), 42, .7f);
			List<INDArray> intermediateSplit0 = new ArrayList<>();
			for (Instance inst : newSplit.get(0)) {
				intermediateSplit0.add(DataSetUtils.instanceToMatrixByDataSet(inst, USED_DATASET));
			}
			List<INDArray> intermediateSplit1 = new ArrayList<>();
			for (Instance inst : newSplit.get(1)) {
				intermediateSplit1.add(DataSetUtils.instanceToMatrixByDataSet(inst, USED_DATASET));
			}

			DataSet resultSplit0 = solution.applyFilter(new DataSet(newSplit.get(0), intermediateSplit0), false);
			DataSet resultSplit1 = solution.applyFilter(new DataSet(newSplit.get(1), intermediateSplit1), false);

			double mlPlanResult = EvaluationUtils.evaluateMLPlan(MLPLAN_TIMEOUT, resultSplit0.getInstances(),
					resultSplit1.getInstances(), 42, logger, ENABLE_MLPLAN_VIS);
			System.out.println(
					"Error Rate of the solution produced by ML-Plan (Cluster): " + (100 - mlPlanResult) / 100f);

		} else {
			logger.info("No solution could be found.");
		}

	}

	// @Test
	public void testImageAutoFELDAEvaluator() throws Exception {
		logger.info("Starting Image AutoFE test...");

		/* load image dataset and create a train-test-split */
		OpenmlConnector connector = new OpenmlConnector();
		DataSetDescription ds = connector.dataGet(USED_DATASET);
		File file = ds.getDataset(DataSetUtils.API_KEY);
		Instances data = new Instances(new BufferedReader(new FileReader(file)));
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit(data, 42, .01f);

		logger.info("Calculating intermediates...");
		List<INDArray> intermediate = new ArrayList<>();
		for (Instance inst : split.get(0)) {
			intermediate.add(DataSetUtils.instanceToMatrixByDataSet(inst, USED_DATASET));
		}
		logger.info("Finished intermediate calculations.");

		FilterPipeline solution = runHASCOImageFE(MODEL_FILE, new LDAObjectEvaluator(),
				new DataSet(split.get(0), intermediate), DATASET_INPUT_SHAPE);

		if (solution != null) {
			logger.info(solution.toString());

			logger.info("Testing result features using MLPlan...");
			List<Instances> newSplit = WekaUtil.getStratifiedSplit(split.get(0), 42, .7f);
			List<INDArray> intermediateSplit0 = new ArrayList<>();
			for (Instance inst : newSplit.get(0)) {
				intermediateSplit0.add(DataSetUtils.instanceToMatrixByDataSet(inst, USED_DATASET));
			}
			List<INDArray> intermediateSplit1 = new ArrayList<>();
			for (Instance inst : newSplit.get(1)) {
				intermediateSplit1.add(DataSetUtils.instanceToMatrixByDataSet(inst, USED_DATASET));
			}

			DataSet resultSplit0 = solution.applyFilter(new DataSet(newSplit.get(0), intermediateSplit0), false);
			DataSet resultSplit1 = solution.applyFilter(new DataSet(newSplit.get(1), intermediateSplit1), false);

			logger.info("Feature size of generated solution: " + resultSplit0.getInstances().numAttributes());

			double mlPlanResult = EvaluationUtils.evaluateMLPlan(MLPLAN_TIMEOUT, resultSplit0.getInstances(),
					resultSplit1.getInstances(), 42, logger, ENABLE_MLPLAN_VIS);
			System.out.println("Error Rate of the solution produced by ML-Plan (LDA): " + (100 - mlPlanResult) / 100f);

		} else {
			logger.info("No solution could be found.");
		}

	}

	@Test
	public void testImageAutoFERandomEvaluator() throws Exception {
		logger.info("Starting Image AutoFE test...");

		long timeStart = System.currentTimeMillis();

		List<Instances> dataSetVariations = HASCOFeatureEngineering.generateRandomDataSets(USED_DATASET, 1,
				MAX_PIPELINE_SIZE, AUTOFE_TIMEOUT, 42);
		// OpenmlConnector connector = new OpenmlConnector();
		// DataSetDescription ds = connector.dataGet(USED_DATASET);
		// File file = ds.getDataset(DataSetUtils.API_KEY);
		// Instances data = new Instances(new BufferedReader(new FileReader(file)));
		// data.setClassIndex(data.numAttributes() - 1);
		// List<Instances> split = WekaUtil.getStratifiedSplit(data, 42,
		// .05f);
		//
		long timeDataSetVar = System.currentTimeMillis();
		System.out.println("timeDataSetVar: " + timeDataSetVar);

		double mlPlanScore = EvaluationUtils.evaluateMLPlan(MLPLAN_TIMEOUT, dataSetVariations.get(0), 0.75, 42, logger,
				false, 6);
		System.out.println("MLPLan Score: " + mlPlanScore);

		long endTime = System.currentTimeMillis();
		System.out.println("timeDataSetVar: " + timeDataSetVar);
		System.out.println("endTime: " + endTime);
		System.out.println("Took: " + (endTime - timeStart));

	}

	// @Test
	public void testImageAutoFEEnsembleEvaluator() throws Exception {
		logger.info("Starting Image AutoFE test...");

		/* load image dataset and create a train-test-split */
		OpenmlConnector connector = new OpenmlConnector();
		DataSetDescription ds = connector.dataGet(USED_DATASET);
		File file = ds.getDataset(DataSetUtils.API_KEY);
		Instances data = new Instances(new BufferedReader(new FileReader(file)));
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit(data, 42, .01f);

		logger.info("Calculating intermediates...");
		List<INDArray> intermediate = new ArrayList<>();
		for (Instance inst : split.get(0)) {
			intermediate.add(DataSetUtils.instanceToMatrixByDataSet(inst, USED_DATASET));
		}
		logger.info("Finished intermediate calculations.");

		FilterPipeline solution = runHASCOImageFE(MODEL_FILE, new EnsembleObjectEvaluator(),
				new DataSet(split.get(0), intermediate), DATASET_INPUT_SHAPE);

		if (solution != null) {
			logger.info(solution.toString());

			logger.info("Testing result features using MLPlan...");
			List<Instances> newSplit = WekaUtil.getStratifiedSplit(split.get(0), 42, .7f);
			List<INDArray> intermediateSplit0 = new ArrayList<>();
			for (Instance inst : newSplit.get(0)) {
				intermediateSplit0.add(DataSetUtils.instanceToMatrixByDataSet(inst, USED_DATASET));
			}
			List<INDArray> intermediateSplit1 = new ArrayList<>();
			for (Instance inst : newSplit.get(1)) {
				intermediateSplit1.add(DataSetUtils.instanceToMatrixByDataSet(inst, USED_DATASET));
			}

			DataSet resultSplit0 = solution.applyFilter(new DataSet(newSplit.get(0), intermediateSplit0), false);
			DataSet resultSplit1 = solution.applyFilter(new DataSet(newSplit.get(1), intermediateSplit1), false);

			logger.info("Feature size of generated solution: " + resultSplit0.getInstances().numAttributes());

			double mlPlanResult = EvaluationUtils.evaluateMLPlan(MLPLAN_TIMEOUT, resultSplit0.getInstances(),
					resultSplit1.getInstances(), 42, logger, ENABLE_MLPLAN_VIS);
			System.out.println(
					"Error Rate of the solution produced by ML-Plan (Ensemble): " + (100 - mlPlanResult) / 100f);

		} else {
			logger.info("No solution could be found.");
		}
	}

	// @Test
	public void testImageAutoFECOCOEvaluator() throws Exception {
		logger.info("Starting Image AutoFE test...");

		/* load image dataset and create a train-test-split */
		OpenmlConnector connector = new OpenmlConnector();
		DataSetDescription ds = connector.dataGet(USED_DATASET);
		File file = ds.getDataset(DataSetUtils.API_KEY);
		Instances data = new Instances(new BufferedReader(new FileReader(file)));
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit(data, 42, .01f);

		logger.info("Calculating intermediates...");
		List<INDArray> intermediate = new ArrayList<>();
		for (Instance inst : split.get(0)) {
			intermediate.add(DataSetUtils.instanceToMatrixByDataSet(inst, USED_DATASET));
		}
		logger.info("Finished intermediate calculations.");

		FilterPipeline solution = runHASCOImageFE(MODEL_FILE, new COCOObjectEvaluator(),
				new DataSet(split.get(0), intermediate), DATASET_INPUT_SHAPE);

		if (solution != null) {
			logger.info(solution.toString());

			logger.info("Testing result features using MLPlan...");
			List<Instances> newSplit = WekaUtil.getStratifiedSplit(split.get(0), 42, .7f);
			List<INDArray> intermediateSplit0 = new ArrayList<>();
			for (Instance inst : newSplit.get(0)) {
				intermediateSplit0.add(DataSetUtils.instanceToMatrixByDataSet(inst, USED_DATASET));
			}
			List<INDArray> intermediateSplit1 = new ArrayList<>();
			for (Instance inst : newSplit.get(1)) {
				intermediateSplit1.add(DataSetUtils.instanceToMatrixByDataSet(inst, USED_DATASET));
			}

			DataSet resultSplit0 = solution.applyFilter(new DataSet(newSplit.get(0), intermediateSplit0), false);
			DataSet resultSplit1 = solution.applyFilter(new DataSet(newSplit.get(1), intermediateSplit1), false);

			logger.info("Feature size of generated solution: " + resultSplit0.getInstances().numAttributes());

			double mlPlanResult = EvaluationUtils.evaluateMLPlan(MLPLAN_TIMEOUT, resultSplit0.getInstances(),
					resultSplit1.getInstances(), 42, logger, ENABLE_MLPLAN_VIS);
			System.out.println("Error Rate of the solution produced by ML-Plan (COCO): " + (100 - mlPlanResult) / 100f);

		} else {
			logger.info("No solution could be found.");
		}
	}

	public static FilterPipeline runHASCOImageFE(final File componentFile,
			final AbstractHASCOFEObjectEvaluator benchmark, final DataSet data, final long[] shape) throws Exception {
		HASCOFeatureEngineeringConfig config = ConfigFactory.create(HASCOFeatureEngineeringConfig.class);

		HASCOFeatureEngineering hasco = new HASCOFeatureEngineering(componentFile,
				new FilterPipelineFactory(shape), benchmark, config);
		return hasco.build(data);
	}
}
