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
import autofe.algorithm.hasco.evaluation.ClusterNodeEvaluator;
import autofe.algorithm.hasco.evaluation.ClusterObjectEvaluator;
import autofe.util.DataSet;
import autofe.util.DataSetUtils;
import de.upb.crc901.mlplan.multiclass.DefaultPreorder;
import de.upb.crc901.mlplan.multiclass.MLPlan;
import jaicore.ml.WekaUtil;
import weka.classifiers.Evaluation;
import weka.core.Instance;
import weka.core.Instances;

public class ImageAutoFETest extends AutoFETest {
	private static final Logger logger = LoggerFactory.getLogger(ImageAutoFETest.class);

	@Test
	public void testImageAutoFE() throws Exception {
		logger.info("Starting Image AutoFE test...");

		/* load cifar 10 dataset and create a train-test-split */
		OpenmlConnector connector = new OpenmlConnector();
		DataSetDescription ds = connector.dataGet(CIFAR10_ID);
		File file = ds.getDataset(API_KEY);
		Instances data = new Instances(new BufferedReader(new FileReader(file)));
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit(data, new Random(42), .02f);

		logger.info("Calculating intermediates...");
		List<INDArray> intermediate = new ArrayList<>();
		for (Instance inst : split.get(0)) {
			// intermediate.add(DataSetUtils.cifar10InstanceToBitmap(inst));
			intermediate.add(DataSetUtils.cifar10InstanceToMatrix(inst));
		}
		logger.info("Finished intermediate calculations.");

		HASCOFE hascoFE = new HASCOFE(new File("model/catalano/catalano.json"), new ClusterNodeEvaluator(20), //
				new DataSet(split.get(0), intermediate), new ClusterObjectEvaluator());
		hascoFE.setLoggerName("autofe image");
		hascoFE.runSearch(240 * 1000);
		HASCOFESolution solution = hascoFE.getCurrentlyBestSolution();
		if (solution != null) {
			logger.info(solution.toString());
			logger.info(hascoFE.getFoundClassifiers().toString());

			logger.info("Testing result features using MLPlan...");
			List<Instances> newSplit = WekaUtil.getStratifiedSplit(split.get(0), new Random(42), .7f);
			List<INDArray> intermediateSplit0 = new ArrayList<>();
			for (Instance inst : newSplit.get(0)) {
				intermediateSplit0.add(DataSetUtils.cifar10InstanceToMatrix(inst));
			}
			List<INDArray> intermediateSplit1 = new ArrayList<>();
			for (Instance inst : newSplit.get(1)) {
				intermediateSplit1.add(DataSetUtils.cifar10InstanceToMatrix(inst));
			}

			DataSet resultSplit0 = solution.getSolution().applyFilter(new DataSet(newSplit.get(0), intermediateSplit0),
					false);
			DataSet resultSplit1 = solution.getSolution().applyFilter(new DataSet(newSplit.get(1), intermediateSplit1),
					false);

			/* Initialize MLPlan using WEKA components */
			MLPlan mlplan = new MLPlan(new File("model/mlplan_weka/weka-all-autoweka.json"));
			mlplan.setLoggerName("mlplan");
			mlplan.setTimeout(30);
			mlplan.setPortionOfDataForPhase2(.3f);
			mlplan.setNodeEvaluator(new DefaultPreorder());
			mlplan.enableVisualization();
			mlplan.buildClassifier(resultSplit0.getInstances());

			/* evaluate solution produced by mlplan */
			Evaluation eval = new Evaluation(resultSplit0.getInstances());
			eval.evaluateModel(mlplan, resultSplit1.getInstances());
			System.out.println("Error Rate of the solution produced by ML-Plan: " + (100 - eval.pctCorrect()) / 100f);

		} else
			logger.info("No solution could be found.");

	}
}
