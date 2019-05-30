package autofe.util;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.ToDoubleFunction;

import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.math3.stat.correlation.KendallsCorrelation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.mlplan.core.MLPlan;
import de.upb.crc901.mlplan.core.MLPlanWekaBuilder;
import de.upb.crc901.mlplan.multiclass.MLPlanClassifierConfig;
import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.model.MLPipeline;
import jaicore.basic.TimeOut;
import jaicore.ml.WekaUtil;
import weka.attributeSelection.ReliefFAttributeEval;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LDA;
import weka.classifiers.functions.supportVector.Kernel;
import weka.classifiers.functions.supportVector.PolyKernel;
import weka.classifiers.functions.supportVector.RBFKernel;
import weka.classifiers.lazy.IBk;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.FilteredClusterer;
import weka.clusterers.SimpleKMeans;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Nystroem;
import weka.filters.unsupervised.attribute.Remove;

/**
 * Utility functions used for node and object evaluation classes.
 *
 * @author Julian Lienen
 *
 */
public final class EvaluationUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(EvaluationUtils.class);

	private static final double DOUBLE_ZERO_PREC = 0.0001;
	private static final double KERNEL_SPLIT_PORTION = .3;

	private EvaluationUtils() {
		// Utility class
	}

	public static double performClustering(final Instances insts) throws Exception {
		LOGGER.debug("Starting cluster evaluation...");

		FilteredClusterer clusterer = new FilteredClusterer();

		Remove filter = new Remove();
		filter.setAttributeIndices("" + (insts.classIndex() + 1));
		filter.setInputFormat(insts);
		Instances removedClassInstances = Filter.useFilter(insts, filter);

		((SimpleKMeans) clusterer.getClusterer()).setOptions(new String[] { "-N", String.valueOf(insts.classAttribute().numValues()) });

		clusterer.buildClusterer(removedClassInstances);

		ClusterEvaluation clusterEval = new ClusterEvaluation();
		clusterEval.setClusterer(clusterer);
		clusterEval.evaluateClusterer(insts);

		return predictAccuracy(insts, clusterEval.getClassesToClusters(), clusterEval.getClusterAssignments());
	}

	public static double performKernelClustering(final Instances instances) throws Exception {
		LOGGER.debug("Starting kernelized cluster evaluation...");

		List<Instances> split = WekaUtil.getStratifiedSplit(instances, 42, KERNEL_SPLIT_PORTION);

		double maxScore = performClustering(new Instances(split.get(0)));
		for (Map.Entry<Kernel, Instances> entry : getKernelsWithInstances(split.get(0))) {
			Kernel kernel = entry.getKey();
			Instances insts = entry.getValue();

			FilteredClusterer clusterer = new FilteredClusterer();

			Remove filter = new Remove();
			filter.setAttributeIndices("" + (insts.classIndex() + 1));
			filter.setInputFormat(insts);

			Instances removedClassInstances = Filter.useFilter(insts, filter);
			Nystroem kernelFilter = new Nystroem();

			kernelFilter.setKernel(kernel);
			clusterer.setFilter(kernelFilter);
			((SimpleKMeans) clusterer.getClusterer()).setOptions(new String[] { "-N", String.valueOf(insts.classAttribute().numValues()) });

			clusterer.buildClusterer(removedClassInstances);

			ClusterEvaluation clusterEval = new ClusterEvaluation();
			clusterEval.setClusterer(clusterer);
			clusterEval.evaluateClusterer(insts);

			double currAcc = predictAccuracy(insts, clusterEval.getClassesToClusters(), clusterEval.getClusterAssignments());
			maxScore = Math.max(maxScore, currAcc);
		}

		LOGGER.debug("Kernelized cluster evaluation result: {}", maxScore);

		return maxScore;
	}

	public static double performKernelLDA(final Instances instances) throws Exception {
		LOGGER.debug("Starting kernelized LDA evaluation...");
		List<Instances> split = WekaUtil.getStratifiedSplit(instances, 42, KERNEL_SPLIT_PORTION);

		double maxScore = performLDA(new Instances(split.get(0)));

		for (Map.Entry<Kernel, Instances> entry : getKernelsWithInstances(split.get(0))) {
			Kernel kernel = entry.getKey();
			Instances insts = entry.getValue();

			Nystroem kernelFilter = new Nystroem();
			kernelFilter.setInputFormat(insts);
			kernelFilter.setKernel(kernel);

			insts = Filter.useFilter(insts, kernelFilter);

			try {
				maxScore = Math.max(maxScore, performLDA(insts));
			} catch (Exception e) {
				LOGGER.warn("Could not calculate the LDA score for kernel {}.", kernel.getClass().getSimpleName(), e);
				maxScore = Math.max(maxScore, 0d);
			}
		}
		return maxScore;
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
	public static double predictAccuracy(final Instances instances, final int[] classesToClusters, final double[] clusterAssignments) {
		if (instances.numInstances() != clusterAssignments.length) {
			throw new IllegalArgumentException("Amount of instances must be equal to cluster assignments.");
		}

		double correct = 0;
		double total = 0;
		for (int i = 0; i < instances.numInstances(); i++) {
			total++;
			Instance inst = instances.get(i);
			if (((int) inst.classValue()) == classesToClusters[(int) clusterAssignments[i]]) {
				correct++;
			}
		}
		if (total > 0) {
			return correct / total;
		} else {
			return 0.0;
		}
	}

	/**
	 * Penalizes high count of attributes (which leads to high run times of
	 * attribute selectors and classifier trainings).
	 *
	 * @param instances
	 * @return
	 */
	public static double calculateAttributeCountPenalty(final Instances instances) {
		return (double) instances.numAttributes() / 15000;
	}

	public static double calculateCOCOForBatch(final Instances batch) {
		batch.setClassIndex(batch.numAttributes() - 1);

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
			if (c.getRow(i).norm2Number().doubleValue() >= DOUBLE_ZERO_PREC && c.getRow(i).norm2Number().doubleValue() >= -DOUBLE_ZERO_PREC) {
				c.getRow(i).divi(c.getRow(i).norm2Number());
			}
		}

		double loss = 0;
		for (int i = 0; i < batch.numInstances(); i++) {
			double[] instValues = Arrays.copyOfRange(batch.get(i).toDoubleArray(), 0, batch.get(i).toDoubleArray().length - 1);
			INDArray fi = Nd4j.create(instValues);
			fi.muli(alpha);
			if (fi.norm2Number().doubleValue() >= DOUBLE_ZERO_PREC && fi.norm2Number().doubleValue() >= -DOUBLE_ZERO_PREC) {
				fi.divi(fi.norm2Number());
			}

			double lowerSum = 0;
			for (int j = 0; j < classes; j++) {

				INDArray tmp = fi.mmul(c.getRow(j).transpose());
				lowerSum += Math.exp(tmp.getDouble(0));

			}
			INDArray ck = c.getRow((int) Math.round(batch.get(i).classValue()));
			INDArray result = fi.mmul(ck.transpose());

			double expExpr = Math.exp(result.getDouble(0));

			loss += Math.log(expExpr / (lowerSum + 1));

			if (Double.isNaN(loss)) {
				LOGGER.warn("Got NaN value for COCO batch score.");
				break;
			}
		}

		return (-1) * loss;
	}

	public static double calculateCOEDForBatch(final Instances batch) {
		batch.setClassIndex(batch.numAttributes() - 1);

		final int D = batch.numAttributes() - 1;
		final int classes = batch.classAttribute().numValues();

		INDArray features = Nd4j.zeros(batch.numInstances(), D);
		INDArray labels = Nd4j.zeros(batch.numInstances(), 1);

		for (int i = 0; i < batch.numInstances(); i++) {
			double[] instValues = Arrays.copyOfRange(batch.get(i).toDoubleArray(), 0, batch.get(i).toDoubleArray().length - 1);
			INDArray fi = Nd4j.create(instValues);
			features.getRow(i).addiRowVector(fi);
			labels.putScalar(i, batch.get(i).classValue());
		}
		org.nd4j.linalg.dataset.DataSet dataSet = new org.nd4j.linalg.dataset.DataSet(features, labels);
		dataSet.setFeatures(features);
		NormalizerStandardize scaler = new NormalizerStandardize();
		scaler.fit(dataSet);
		scaler.preProcess(dataSet);

		// Calculate centroids
		int[] classCounts = new int[classes];
		INDArray c = Nd4j.zeros(classes, D);
		for (int i = 0; i < classes; i++) {
			for (Instance inst : batch) {
				if (Math.round(inst.classValue()) == i) {
					c.getRow(i).addiRowVector(dataSet.get(i).getFeatures());
					classCounts[i]++;
				}
			}
		}
		for (int i = 0; i < classes; i++) {
			c.getRow(i).divi(classCounts[i] + 1);
		}

		double loss = 0;
		for (int i = 0; i < batch.numInstances(); i++) {
			INDArray fi = dataSet.get(i).getFeatures();

			double lowerSum = 0;
			for (int j = 0; j < classes; j++) {

				if (i != j) {
					lowerSum += Math.exp(calculateEuclideanImageDistance(fi, c.getRow(j)));
				}
			}

			INDArray ck = c.getRow((int) Math.round(batch.get(i).classValue()));
			double upperExp = Math.exp(calculateEuclideanImageDistance(fi, ck));

			loss += Math.log(upperExp / (lowerSum + 1));

			if (Double.isNaN(loss)) {
				LOGGER.warn("Got NaN value for COED batch score.");
				break;
			}
		}

		return (-1) * loss;
	}

	private static double calculateEuclideanImageDistance(final INDArray inst1, final INDArray inst2) {
		return inst1.distance2(inst2) / inst1.length();
	}

	public static double performLDA(final Instances instances) throws Exception {
		List<Instances> split = WekaUtil.getStratifiedSplit(instances, 42, .7f);
		LDA lda = new LDA();
		lda.buildClassifier(split.get(0));

		Evaluation eval = new Evaluation(split.get(0));
		eval.evaluateModel(lda, split.get(1));

		return eval.pctCorrect() / 100.0;
	}

	public static double performEnsemble(Instances instances) throws Exception {
		List<Instances> subsample = WekaUtil.getStratifiedSplit(instances, 42, .05f);
		instances = subsample.get(0);

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

		if (totalNumericCount != 0) {
			varianceMean /= totalNumericCount;
		}

		/* KNN */
		List<Instances> split = WekaUtil.getStratifiedSplit(instances, 42, .7f);
		IBk knn = new IBk(10);
		knn.buildClassifier(split.get(0));
		Evaluation eval = new Evaluation(split.get(0));
		eval.evaluateModel(knn, split.get(1));
		double knnResult = eval.pctCorrect() / 100d;

		return 1 - (0.33 * attEvalSum + 0.33 * knnResult + 0.33 * varianceMean);
	}

	public static ToDoubleFunction<Instances> getBenchmarkFunctionByName(final String name) {
		switch (name) {
		case "Cluster":
			return data -> {
				try {
					return 1 - performClustering(data);
				} catch (Exception e1) {
					LOGGER.error("Could not perform clustering benchmark.", e1);
					return 1d;
				}
			};
		case "KernelCluster":
			return data -> {
				try {
					return 1 - performKernelClustering(data);
				} catch (Exception e1) {
					LOGGER.error("Could not perform kernel clustering benchmark.", e1);
					return 1d;
				}
			};
		case "COCO":
			return data -> {
				try {
					return calculateCOCOForBatch(data);
				} catch (Exception e1) {
					LOGGER.error("Could not calculate COCO.", e1);
					return 1d;
				}
			};
		case "COED":
			return data -> {
				try {
					return calculateCOEDForBatch(data);
				} catch (Exception e1) {
					LOGGER.error("Could not calculate COED.", e1);
					return 1d;
				}
			};
		case "LDA":
			return data -> {
				try {
					return 1 - performLDA(data);
				} catch (Exception e) {
					LOGGER.error("Could not perform LDA benchmark.", e);
					return 1d;
				}
			};
		case "KernelLDA":
			return data -> {
				try {
					return 1 - performKernelLDA(data);
				} catch (Exception e) {
					LOGGER.error("Could not perform cluster LDA benchmark.", e);
					return 1d;
				}
			};
		case "Ensemble":
			return data -> {
				try {
					return 1 - performEnsemble(data);
				} catch (Exception e) {
					LOGGER.error("Could not perform ensemble benchmark.", e);
					return 1d;
				}
			};
		default:
			throw new InvalidEvaluationFunctionException("Invalid evaluation function: " + name);
		}
	}

	public static double rankKendallsTau(final double[] ranking1, final double[] ranking2) {
		return new KendallsCorrelation().correlation(ranking1, ranking2);
	}

	public static double evaluateMLPlan(final int timeout, final Instances training, final Instances test, final int seed, final Logger logger, final int numCores) throws Exception {

		logger.debug("Starting ML-Plan execution. Training on {} instances with {} attributes.", training.numInstances(), training.numAttributes());

		/* Initialize MLPlan using WEKA components */
		MLPlanWekaBuilder builder = new MLPlanWekaBuilder();
		MLPlanClassifierConfig config = ConfigFactory.create(MLPlanClassifierConfig.class);
		config.setProperty(MLPlanClassifierConfig.K_RANDOM_SEED, seed + "");
		config.setProperty(MLPlanClassifierConfig.K_CPUS, numCores + "");
		config.setProperty(MLPlanClassifierConfig.SELECTION_PORTION, "0.1");
		builder.withAlgorithmConfig(config);
		builder.withTimeOut(new TimeOut(timeout, TimeUnit.SECONDS));
		builder.withDataset(training);

		MLPlan mlplan = builder.build();
		mlplan.setLoggerName("mlplan");
		Classifier c = mlplan.call();

		if (c == null) {
			logger.warn("Could not find a model using ML-Plan. Returning -1...");
			return -1;
		}

		String solutionString = ((MLPipeline) mlplan.getSelectedClassifier()).getBaseClassifier().getClass().getName() + " | " + ((MLPipeline) mlplan.getSelectedClassifier()).getPreprocessors();
		logger.debug("Selected classifier: {}", solutionString);

		/* evaluate solution produced by mlplan */
		Evaluation eval = new Evaluation(training);
		eval.evaluateModel(c, test);

		return eval.pctCorrect();
	}

	public static double evaluateMLPlan(final int timeout, final Instances training, final Instances test, final int seed, final Logger logger) throws Exception {
		return evaluateMLPlan(timeout, training, test, seed, logger, 1);
	}

	public static double evaluateMLPlan(final int timeout, final Instances instances, final double trainRatio, final int seed, final Logger logger, final int numCores) throws Exception {

		List<Instances> split = WekaUtil.getStratifiedSplit(instances, seed, trainRatio);

		return evaluateMLPlan(timeout, split.get(0), split.get(1), seed, logger, numCores);
	}

	private static List<Map.Entry<Kernel, Instances>> getKernelsWithInstances(final Instances insts) throws ListKernelsFailedException {
		try {
			ArrayList<Map.Entry<Kernel, Instances>> result = new ArrayList<>();
			Instances rbfInsts = new Instances(insts);
			result.add(new AbstractMap.SimpleEntry<Kernel, Instances>(new RBFKernel(rbfInsts, 250007, 0.01), rbfInsts));

			Instances poly2Insts = new Instances(insts);
			result.add(new AbstractMap.SimpleEntry<Kernel, Instances>(new PolyKernel(poly2Insts, 250007, 2, false), poly2Insts));

			Instances poly3Insts = new Instances(insts);
			result.add(new AbstractMap.SimpleEntry<Kernel, Instances>(new PolyKernel(poly3Insts, 250007, 2, false), poly3Insts));

			return result;
		} catch (Exception e) {
			throw new ListKernelsFailedException("Could not list all the kernels for the given dataset.", e);
		}
	}
}