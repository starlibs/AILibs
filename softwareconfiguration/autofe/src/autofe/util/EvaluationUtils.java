package autofe.util;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.ops.transforms.Transforms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.algorithm.hasco.evaluation.AbstractHASCOFENodeEvaluator;
import autofe.algorithm.hasco.filter.meta.FilterPipeline;
import fantail.core.Correlation;
import jaicore.ml.WekaUtil;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.search.structure.core.Node;
import weka.attributeSelection.ReliefFAttributeEval;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LDA;
import weka.classifiers.lazy.IBk;
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
		// Initialize kernel? (using data, cache size 250007, gamma 0.01)? =>
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

	/**
	 * Penalizes high count of attributes (which leads to high run times of
	 * attribute selectors and classifier trainings).
	 * 
	 * @param instances
	 * @return
	 */
	public static double calculateAttributeCountPenalty(final Instances instances) {
		// TODO: Which attribute number to use?
		return instances.numAttributes() / 15000;
	}

	public static double calculateCOCOForBatch(final Instances batch) {

		final int D = batch.numAttributes() - 1;
		final int classes = batch.classAttribute().numValues();

		final double alpha = 10;

		// Calculate centroids
		int[] classCounts = new int[classes];
		INDArray c = Nd4j.zeros(classes, D);
		for (int i = 0; i < classes; i++) {
			for (Instance inst : batch) {
				if (Math.round(inst.classValue()) == i) {
					double[] instValues = Arrays.copyOfRange(inst.toDoubleArray(), 0, inst.toDoubleArray().length - 1);
					c.getRow(i).addiRowVector(Nd4j.create(instValues));
					classCounts[i]++;
				}
			}
		}
		for (int i = 0; i < classes; i++) {
			c.getRow(i).divi(classCounts[i] + 1);
			c.getRow(i).divi(c.getRow(i).norm2Number());
		}

		// double loss = 0;
		// for (int i = 0; i < batch.numInstances(); i++) {
		// double[] instValues = Arrays.copyOfRange(batch.get(i).toDoubleArray(), 0,
		// batch.get(i).toDoubleArray().length - 1);
		// INDArray f_i = Nd4j.create(instValues);
		//
		// double lowerSum = 0;
		// for (int j = 0; j < classes; j++) {
		// if (i != j)
		// lowerSum += Math.exp(calculateCosSim(f_i, c.getRow(j)));
		//
		// }
		//
		// loss += Math.exp(calculateCosSim(f_i, c.getRow((int)
		// Math.round(batch.get(i).classValue()))))
		// / (lowerSum + 1);
		// }

		double loss = 0;
		for (int i = 0; i < batch.numInstances(); i++) {
			double[] instValues = Arrays.copyOfRange(batch.get(i).toDoubleArray(), 0,
					batch.get(i).toDoubleArray().length - 1);
			INDArray f_i = Nd4j.create(instValues);
			f_i.muli(alpha);
			f_i.divi(f_i.norm2Number());

			double lowerSum = 0;
			for (int j = 0; j < classes; j++) {

				// TODO: In paper, the case i != j is NOT excluded!
				// if (i != j) {
				INDArray tmp = f_i.mmul(c.getRow(j).transpose());
				lowerSum += Math.exp(tmp.getDouble(0));
				// }

			}
			INDArray c_k = c.getRow((int) Math.round(batch.get(i).classValue()));
			INDArray result = f_i.mmul(c_k.transpose());
			loss += Math.log(Math.exp(result.getDouble(0)) / (lowerSum + 1));
		}

		return (-1) * loss;
	}

	private static double calculateCosSim(final INDArray f1, final INDArray f2) {
		return Transforms.cosineSim(f1, f2);
	}

	public static double performLDA(final Instances instances) throws Exception {
		List<Instances> split = WekaUtil.getStratifiedSplit(instances, new Random(42), .7f);

		LDA lda = new LDA();
		// FLDA lda = new FLDA();
		lda.buildClassifier(split.get(0));

		Evaluation eval = new Evaluation(split.get(0));
		eval.evaluateModel(lda, split.get(1));

		return eval.pctCorrect() / 100.0;
	}

	public static double performEnsemble(final Instances instances) throws Exception {
		/* Relief */
		ReliefFAttributeEval relief = new ReliefFAttributeEval();
		relief.buildEvaluator(instances);
		double attEvalSum = 0;
		for (int i = 0; i < instances.numAttributes() - 1; i++) {
			attEvalSum += relief.evaluateAttribute(i);
		}
		attEvalSum /= instances.numAttributes();

		/* Variance */
		double varianceMean = 0;
		int totalNumericCount = 0;
		for (int i = 0; i < instances.numAttributes() - 1; i++) {
			if (instances.attribute(i).isNumeric()) {
				instances.attributeStats(i).numericStats.calculateDerived();
				varianceMean += Math.pow(instances.attributeStats(i).numericStats.stdDev, 2);
				totalNumericCount++;
			}
		}
		varianceMean /= totalNumericCount;

		/* KNN */
		List<Instances> split = WekaUtil.getStratifiedSplit(instances, new Random(42), .7f);
		IBk knn = new IBk(10);
		knn.buildClassifier(split.get(0));
		Evaluation eval = new Evaluation(split.get(0));
		eval.evaluateModel(knn, split.get(1));
		double knnResult = eval.pctCorrect() / 100d;

		return 1 - (0.33 * attEvalSum + 0.33 * knnResult + 0.33 * varianceMean);
	}

	public static Function<Instances, Double> getBenchmarkFuntionByName(final String name) {
		switch (name) {
		case "Cluster":
			return (data) -> {
				try {
					return performClustering(data);
				} catch (Exception e1) {
					logger.error("Could not perform clustering benchmark. Reason: " + e1.getMessage());
					return 1d;
				}
			};
		case "COCO":
			return (data) -> calculateCOCOForBatch(data);
		case "LDA":
			return (data) -> {
				try {
					return performLDA(data);
				} catch (Exception e) {
					logger.error("Could not perform LDA benchmark. Reason: " + e.getMessage());
					return 1d;
				}
			};
		case "Ensemble":
			return (data) -> {
				try {
					return performEnsemble(data);
				} catch (Exception e) {
					logger.error("Could not perform ensemble benchmark. Reason: " + e.getMessage());
					return 1d;
				}
			};
		// case "Random":
		default:
			return (data) -> new Random().nextDouble();
		}
	}

	public static AbstractHASCOFENodeEvaluator getRandomNodeEvaluator(final int maxPipelineSize) {
		return new AbstractHASCOFENodeEvaluator(maxPipelineSize) {

			@Override
			public Double f(Node<TFDNode, ?> node) throws Throwable {
				if (node.getParent() == null)
					return null;

				// If pipeline is too deep, assign worst value
				if (node.path().size() > this.maxPipelineSize)
					return AbstractHASCOFENodeEvaluator.MAX_EVAL_VALUE;

				return null;
			}
		};
	}

	public static double rankKendallsTau(final double[] ranking1, final double[] ranking2) {
		return Correlation.rankKendallTauBeta(ranking1, ranking2);
	}

}
