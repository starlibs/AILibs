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
import autofe.algorithm.hasco.evaluation.ClusterEvaluator;
import autofe.util.DataSet;
import autofe.util.DataSetUtils;
import de.upb.crc901.mlplan.multiclass.DefaultPreorder;
import de.upb.crc901.mlplan.multiclass.MLPlan;
import jaicore.ml.WekaUtil;
import weka.classifiers.Evaluation;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.FilteredClusterer;
import weka.clusterers.SimpleKMeans;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

public class ImageAutoFETest {
	private static final Logger logger = LoggerFactory.getLogger(ImageAutoFETest.class);

	@Test
	public void testImageAutoFE() throws Exception {
		logger.info("Starting Image AutoFE test...");

		/* load cifar 10 dataset and create a train-test-split */
		OpenmlConnector connector = new OpenmlConnector();
		DataSetDescription ds = connector.dataGet(40927);
		File file = ds.getDataset("4350e421cdc16404033ef1812ea38c01");
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

		HASCOFE hascoFE = new HASCOFE(new File("model/catalano/catalano.json"), n -> null,
				new DataSet(split.get(0), intermediate), new ClusterEvaluator());
		hascoFE.setLoggerName("autofe image");
		hascoFE.runSearch(120 * 1000);
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

	// @Test
	public void cifar10ClusterTest() throws Exception {
		logger.info("Starting Image AutoFE test...");

		/* load cifar 10 dataset and create a train-test-split */
		OpenmlConnector connector = new OpenmlConnector();
		DataSetDescription ds = connector.dataGet(40927);
		File file = ds.getDataset("4350e421cdc16404033ef1812ea38c01");
		Instances data = new Instances(new BufferedReader(new FileReader(file)));
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit(data, new Random(42), .02f);

		Instances insts = split.get(0);

		FilteredClusterer clusterer = new FilteredClusterer();

		Remove filter = new Remove();
		filter.setAttributeIndices("" + (insts.classIndex() + 1));
		filter.setInputFormat(insts);
		Instances removedClassInstances = Filter.useFilter(insts, filter);

		// TODO: Kernel
		// Nystroem kernelFilter = new Nystroem();
		// TODO: Initialize kernel? (using data, cache size 250007, gamma 0.01)? =>
		// Defaults
		// Kernel kernel = new PolyKernel(insts, 250007, 2, false);
		// Kernel kernel = new RBFKernel(insts, 250007, 1);
		// kernel.buildKernel(insts);
		//
		// kernelFilter.setKernel(kernel); // insts, 250007,
		// 0.01 new RBFKernel(insts, 250007, 0.01)
		// clusterer.setFilter(kernelFilter);
		((SimpleKMeans) clusterer.getClusterer()).setNumClusters(insts.classAttribute().numValues());
		((weka.core.EuclideanDistance) ((SimpleKMeans) clusterer.getClusterer()).getDistanceFunction())
				.setDontNormalize(true);

		clusterer.buildClusterer(removedClassInstances);

		ClusterEvaluation clusterEval = new ClusterEvaluation();
		clusterEval.setClusterer(clusterer);
		clusterEval.evaluateClusterer(insts);

		logger.debug("ClusterEvaluator results: " + clusterEval.clusterResultsToString());
		// logger.info("Log likelihood:" + clusterEval.getLogLikelihood());
		// logger.info(Arrays.toString(clusterEval.getClassesToClusters()));
		// logger.info(Arrays.toString(clusterEval.getClusterAssignments()));

		double acc = predictAccuracy(insts, clusterEval.getClassesToClusters(), clusterEval.getClusterAssignments());
		logger.debug("Acc: " + acc);
	}

	private static double predictAccuracy(final Instances instances, final int[] classesToClusters,
			final double[] clusterAssignments) {
		if (instances.numInstances() != clusterAssignments.length)
			throw new IllegalArgumentException("Amount of instances must be equal to cluster assignments.");

		double correct = 0;
		double total = 0;
		for (int i = 0; i < instances.numInstances(); i++) {
			total++;
			Instance inst = instances.get(i);
			if (((int) inst.classValue()) == classesToClusters[(int) clusterAssignments[i]])
				correct++;
		}
		return correct / total;
	}
}
