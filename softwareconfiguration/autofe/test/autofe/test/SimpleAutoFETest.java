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

import autofe.algorithm.hasco.HASCOFE;
import autofe.algorithm.hasco.HASCOFE.HASCOFESolution;
import autofe.algorithm.hasco.evaluation.ClusterEvaluator;
import autofe.util.DataSet;
import jaicore.ml.WekaUtil;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.FilteredClusterer;
import weka.clusterers.SimpleKMeans;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

public class SimpleAutoFETest {
	private static final Logger logger = LoggerFactory.getLogger(SimpleAutoFETest.class);

	// @Test
	public void testHASCO() throws Exception {
		logger.info("Starting AutoFE test...");

		/* load data for segment dataset and create a train-test-split */
		OpenmlConnector connector = new OpenmlConnector();
		DataSetDescription ds = connector.dataGet(40984);
		File file = ds.getDataset("4350e421cdc16404033ef1812ea38c01");
		Instances data = new Instances(new BufferedReader(new FileReader(file)));
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit(data, new Random(0), .7f);

		HASCOFE hascoFE = new HASCOFE(new File("model/test.json"), n -> null, new DataSet(split.get(0), null),
				new ClusterEvaluator());
		hascoFE.setLoggerName("autofe");
		// hascoFE.enableVisualization();
		hascoFE.runSearch(10 * 1000);
		HASCOFESolution solution = hascoFE.getCurrentlyBestSolution();
		logger.info(solution.toString());
		logger.info(hascoFE.getFoundClassifiers().toString());
	}

	@Test
	public void clusterEvaluatorTest() throws Exception {
		/* load data for segment dataset and create a train-test-split */
		OpenmlConnector connector = new OpenmlConnector();
		DataSetDescription ds = connector.dataGet(40984);
		File file = ds.getDataset("4350e421cdc16404033ef1812ea38c01");
		Instances data = new Instances(new BufferedReader(new FileReader(file)));
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit(data, new Random(0), .5f);

		FilteredClusterer clusterer = new FilteredClusterer();

		Instances insts = split.get(0);

		Remove filter = new Remove();
		filter.setAttributeIndices("" + (insts.classIndex() + 1));
		filter.setInputFormat(insts);
		Instances removedClassInstances = Filter.useFilter(insts, filter);

		// TODO: Kernel
		// Nystroem kernelFilter = new Nystroem();
		// TODO: Initialize kernel? (using data, cache size 250007, gamma 0.01)? =>
		// Defaults
		// Kernel kernel = new PolyKernel(insts, 250007, 2, false);
		// Kernel kernel = new RBFKernel(insts, 250007, 1000);
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
