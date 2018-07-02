package autofe.algorithm.hasco.evaluation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.algorithm.hasco.filter.meta.FilterPipeline;
import autofe.util.DataSet;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.FilteredClusterer;
import weka.clusterers.SimpleKMeans;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

// This is used for the search guidance
public class ClusterEvaluator extends AbstractHASCOFEObjectEvaluator {

	private static final Logger logger = LoggerFactory.getLogger(ClusterEvaluator.class);

	@Override
	public Double evaluate(FilterPipeline object) throws Exception {

		logger.debug("Starting cluster evaluation...");

		// Load vector class due to load failure exception
		@SuppressWarnings("unused")
		no.uib.cipr.matrix.Vector vector;

		DataSet dataSet = object.applyFilter(this.data, false);

		logger.debug("Applied pipeline.");

		Instances insts = dataSet.getInstances();

		FilteredClusterer clusterer = new FilteredClusterer();

		Remove filter = new Remove();
		filter.setAttributeIndices("" + (insts.classIndex() + 1));
		filter.setInputFormat(insts);
		Instances removedClassInstances = Filter.useFilter(insts, filter);

		// TODO: Kernel
		// Nystroem kernelFilter = new Nystroem();
		// TODO: Initialize kernel? (using data, cache size 250007, gamma 0.01)? =>
		// Defaults

		// kernelFilter.setKernel(new RBFKernel(insts, 250007, 0.01)); // insts, 250007,
		// 0.01
		// clusterer.setFilter(kernelFilter);
		((SimpleKMeans) clusterer.getClusterer()).setNumClusters(insts.classAttribute().numValues());
		((weka.core.EuclideanDistance) ((SimpleKMeans) clusterer.getClusterer()).getDistanceFunction())
				.setDontNormalize(true);

		clusterer.buildClusterer(removedClassInstances);

		ClusterEvaluation clusterEval = new ClusterEvaluation();
		clusterEval.setClusterer(clusterer);
		clusterEval.evaluateClusterer(insts);

		// logger.debug("ClusterEvaluator results: " +
		// clusterEval.clusterResultsToString());
		// logger.info("Log likelihood:" + clusterEval.getLogLikelihood());
		// logger.info(Arrays.toString(clusterEval.getClassesToClusters()));
		// logger.info(Arrays.toString(clusterEval.getClusterAssignments()));

		double acc = predictAccuracy(insts, clusterEval.getClassesToClusters(), clusterEval.getClusterAssignments());
		logger.debug("Acc: " + acc);

		// return new Random().nextDouble() * 10;
		return acc;
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
