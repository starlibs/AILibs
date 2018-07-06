package autofe.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.algorithm.hasco.filter.meta.FilterPipeline;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.FilteredClusterer;
import weka.clusterers.SimpleKMeans;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

/**
 * Utility functions used for node and object evaluation classes.
 * 
 * @author Julian Lienen
 *
 */
public final class EvaluationUtils {
	private static final Logger logger = LoggerFactory.getLogger(EvaluationUtils.class);

	private EvaluationUtils() {
		// Utility class
	}

	/**
	 * Performs clustering using the given FilterPipeline <code>pipeline</code> and
	 * the data set <code>data</code>.
	 * 
	 * @param pipeline
	 *            FilterPipeline which transforms the given data set
	 * @param data
	 *            DataSet object which is transformed by the pipeline
	 * @return Returns 1 - accuracy
	 * @throws Exception
	 */
	public static double performClustering(final FilterPipeline pipeline, final DataSet data) throws Exception {
		if (pipeline == null || data == null)
			throw new IllegalArgumentException("Parameters 'pipeline' and 'data' must not be null!");

		logger.debug("Applying and evaluating pipeline " + pipeline.toString());

		// Load vector class due to load failure exception
		@SuppressWarnings("unused")
		no.uib.cipr.matrix.Vector vector;

		DataSet dataSet = pipeline.applyFilter(data, true);

		logger.debug("Applied pipeline.");

		return performClustering(dataSet.getInstances());
	}

	private static double performClustering(final Instances insts) throws Exception {
		logger.debug("Starting cluster evaluation...");

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
		((SimpleKMeans) clusterer.getClusterer())
				.setOptions(new String[] { "-num-slots", String.valueOf(Runtime.getRuntime().availableProcessors()),
						"-N", String.valueOf(insts.classAttribute().numValues()) });

		// ((SimpleKMeans)
		// clusterer.getClusterer()).setNumClusters(insts.classAttribute().numValues());
		// ((weka.core.EuclideanDistance) ((SimpleKMeans)
		// clusterer.getClusterer()).getDistanceFunction())
		// .setDontNormalize(true);

		clusterer.buildClusterer(removedClassInstances);

		ClusterEvaluation clusterEval = new ClusterEvaluation();
		clusterEval.setClusterer(clusterer);
		clusterEval.evaluateClusterer(insts);

		double acc = predictAccuracy(insts, clusterEval.getClassesToClusters(), clusterEval.getClusterAssignments());
		logger.debug("Cluster acc: " + acc);

		return 1 - acc;
	}

	/**
	 * Prediction of the accuracy of the given <code>instances</code>, the mapping
	 * of classes to clusters <code>classesToClusters</code> and the assignments of
	 * the instances to the specific clusters <code>clusterAssignments</code>.
	 * 
	 * @param instances
	 *            Instances which were assigned to clusters
	 * @param classesToClusters
	 *            Mapping of clusters to classes
	 * @param clusterAssignments
	 *            Assignments of the instances to the clusters
	 * @return
	 */
	public static double predictAccuracy(final Instances instances, final int[] classesToClusters,
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
