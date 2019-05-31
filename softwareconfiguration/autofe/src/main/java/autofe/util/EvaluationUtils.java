package autofe.util;

import autofe.algorithm.hasco.evaluation.AbstractHASCOFEEvaluator;
import autofe.algorithm.hasco.evaluation.AbstractHASCOFENodeEvaluator;
import autofe.algorithm.hasco.filter.meta.FilterPipeline;

import org.apache.commons.math3.stat.correlation.KendallsCorrelation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.TimeOut;
import ai.libs.jaicore.ml.WekaUtil;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.search.model.travesaltree.Node;
import ai.libs.mlplan.core.AbstractMLPlanBuilder;
import ai.libs.mlplan.core.MLPlan;
import ai.libs.mlplan.core.MLPlanWekaBuilder;
import ai.libs.mlplan.multiclass.wekamlplan.weka.model.MLPipeline;
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
import weka.filters.unsupervised.attribute.RemoveUseless;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;

/**
 * Utility functions used for node and object evaluation classes.
 *
 * @author Julian Lienen
 */
public final class EvaluationUtils {
    private static final Logger logger = LoggerFactory.getLogger(EvaluationUtils.class);

    private static final String EVALUATION_STOPPED_MESSAGE = "Evaluation has been interrupted!";

    private static double kernelSplitPortion = .3;

    private EvaluationUtils() {
        // Utility class
    }

    /**
     * Performs clustering using the given FilterPipeline <code>pipeline</code> and
     * the data set <code>data</code>.
     *
     * @param pipeline FilterPipeline which transforms the given data set
     * @param data     DataSet object which is transformed by the pipeline
     * @return Returns 1 - accuracy
     * @throws Exception
     */
    public static double performClustering(final FilterPipeline pipeline, final DataSet data) throws Exception {
        if (pipeline == null || data == null) {
            throw new IllegalArgumentException("Parameters 'pipeline' (" + (pipeline == null) + ") and 'data' ("
                    + (data == null) + ") must not be null!");
        }

        logger.debug("Applying and evaluating pipeline {}", pipeline);

        // Load vector class due to load failure exception
        @SuppressWarnings("unused")
        no.uib.cipr.matrix.Vector vector = null;
        logger.debug("Vector: {}", vector);

        DataSet dataSet = pipeline.applyFilter(data, true);
        logger.debug("Number of attributes: {}", dataSet.getInstances().numAttributes());

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

        ((SimpleKMeans) clusterer.getClusterer())
                .setOptions(new String[]{"-N", String.valueOf(insts.classAttribute().numValues())});

        clusterer.buildClusterer(removedClassInstances);

        ClusterEvaluation clusterEval = new ClusterEvaluation();
        clusterEval.setClusterer(clusterer);
        clusterEval.evaluateClusterer(insts);

        return predictAccuracy(insts, clusterEval.getClassesToClusters(), clusterEval.getClusterAssignments());
    }

    public static double performKernelClustering(final Instances instances, final int numThreads) throws Exception {
        logger.debug("Starting kernelized cluster evaluation...");

        List<Instances> split = WekaUtil.getStratifiedSplit(instances, 42, kernelSplitPortion);

        ExecutorService execService = Executors.newFixedThreadPool(numThreads);
        List<Future<Double>> futures = new ArrayList<>();
        Future<Double> result0 = execService.submit(() ->
                performClustering(new Instances(split.get(0)))
        );
        futures.add(result0);

        for (Map.Entry<Kernel, Instances> entry : getKernelsWithInstances(split.get(0))) {
            if (Thread.currentThread().isInterrupted())
                throw new InterruptedException(EVALUATION_STOPPED_MESSAGE);

            Future<Double> result = execService.submit(() -> {
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
                ((SimpleKMeans) clusterer.getClusterer())
                        .setOptions(new String[]{"-N", String.valueOf(insts.classAttribute().numValues())});

                clusterer.buildClusterer(removedClassInstances);

                ClusterEvaluation clusterEval = new ClusterEvaluation();
                clusterEval.setClusterer(clusterer);
                clusterEval.evaluateClusterer(insts);

                return predictAccuracy(insts, clusterEval.getClassesToClusters(), clusterEval.getClusterAssignments());
            });
            futures.add(result);
        }

        return evaluateFutures(futures);
    }

    private static double evaluateFutures(List<Future<Double>> futures) {
        OptionalDouble result = futures.stream().mapToDouble(future -> {
            try {
                return future.get();
            } catch (InterruptedException | ExecutionException e) {
                logger.warn("Could not calculate the LDA score for at least one kernel.");
                Thread.currentThread().interrupt();
                return 0;
            }
        }).max();
        if (result.isPresent()) {
            return result.getAsDouble();
        } else {
            return -1d;
        }
    }

    public static double performKernelLDA(final Instances instances, final int numThreads) throws Exception {

        logger.debug("Starting kernelized LDA evaluation...");

        List<Instances> split = WekaUtil.getStratifiedSplit(instances, 42, kernelSplitPortion);

        ExecutorService execService = Executors.newFixedThreadPool(numThreads);

        List<Future<Double>> futures = new ArrayList<>();
        Future<Double> result0 = execService.submit(() -> performLDA(new Instances(split.get(0))));
        futures.add(result0);

        for (final Map.Entry<Kernel, Instances> entry : getKernelsWithInstances(split.get(0))) {
            if (Thread.currentThread().isInterrupted())
                throw new InterruptedException(EVALUATION_STOPPED_MESSAGE);

            Future<Double> result = execService.submit(() -> {
                Kernel kernel = entry.getKey();
                Instances insts = entry.getValue();

                Nystroem kernelFilter = new Nystroem();
                kernelFilter.setInputFormat(insts);
                kernelFilter.setKernel(kernel);

                insts = Filter.useFilter(insts, kernelFilter);

                return performLDA(insts);
            });
            futures.add(result);
        }

        execService.shutdown();
        execService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);

        return evaluateFutures(futures);
    }

    /**
     * Prediction of the accuracy of the given <code>instances</code>, the mapping
     * of classes to clusters <code>classesToClusters</code> and the assignments of
     * the instances to the specific clusters <code>clusterAssignments</code>.
     *
     * @param instances          Instances which were assigned to clusters
     * @param classesToClusters  Mapping of clusters to classes
     * @param clusterAssignments Assignments of the instances to the clusters
     * @return Returns the accuracy
     */
    public static double predictAccuracy(final Instances instances, final int[] classesToClusters,
                                         final double[] clusterAssignments) {
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
        return total != 0 ? (correct / total) : -1;
    }

    /**
     * Penalizes high count of attributes (which leads to high run times of
     * attribute selectors and classifier training).
     *
     * @param instances Instances used for penalty calculation
     * @return Returns the value of the penality
     */
    public static double calculateAttributeCountPenalty(final Instances instances) {
        return instances.numAttributes() / 15000d;
    }

    public static double calculateCOCOForBatch(final Instances batch) throws InterruptedException {

        batch.setClassIndex(batch.numAttributes() - 1);

        final int D = batch.numAttributes() - 1;
        final int classes = batch.classAttribute().numValues();

        final double alpha = 10;
        final double doubleZeroPrec = 0.0001;

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
            if (c.getRow(i).norm2Number().doubleValue() >= doubleZeroPrec
                    && c.getRow(i).norm2Number().doubleValue() >= -doubleZeroPrec) {
                c.getRow(i).divi(c.getRow(i).norm2Number());
            }
        }

        double loss = 0;
        for (int i = 0; i < batch.numInstances(); i++) {
            if (Thread.currentThread().isInterrupted())
                throw new InterruptedException(EVALUATION_STOPPED_MESSAGE);

            double[] instValues = Arrays.copyOfRange(batch.get(i).toDoubleArray(), 0,
                    batch.get(i).toDoubleArray().length - 1);
            INDArray fi = Nd4j.create(instValues);
            fi.muli(alpha);
            if (fi.norm2Number().doubleValue() >= doubleZeroPrec
                    && fi.norm2Number().doubleValue() >= -doubleZeroPrec) {
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
                logger.warn("Got NaN value for COCO batch score.");
                break;
            }
        }

        return (-1) * loss;
    }

    public static double calculateCOEDForBatch(final Instances batch) throws InterruptedException {
        batch.setClassIndex(batch.numAttributes() - 1);

        final int D = batch.numAttributes() - 1;
        final int classes = batch.classAttribute().numValues();

        INDArray features = Nd4j.zeros(batch.numInstances(), D);
        INDArray labels = Nd4j.zeros(batch.numInstances(), 1);

        for (int i = 0; i < batch.numInstances(); i++) {
            double[] instValues = Arrays.copyOfRange(batch.get(i).toDoubleArray(), 0,
                    batch.get(i).toDoubleArray().length - 1);
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
            if (Thread.currentThread().isInterrupted())
                throw new InterruptedException(EVALUATION_STOPPED_MESSAGE);

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
                logger.warn("Got NaN value for COED batch score.");
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

        Instances train = split.get(0);
        if (train.numAttributes() > 5_000) {
            RemoveUseless rem = new RemoveUseless();
            rem.setMaximumVariancePercentageAllowed(0.9);
            rem.setInputFormat(train);
            train = Filter.useFilter(train, rem);
        }
        lda.buildClassifier(train);

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
        varianceMean /= (totalNumericCount != 0 ? totalNumericCount : 1);

        /* KNN */
        List<Instances> split = WekaUtil.getStratifiedSplit(instances, 42, .7f);
        IBk knn = new IBk(10);
        knn.buildClassifier(split.get(0));
        Evaluation eval = new Evaluation(split.get(0));
        eval.evaluateModel(knn, split.get(1));
        double knnResult = eval.pctCorrect() / 100d;

        return 1 - (0.33 * attEvalSum + 0.33 * knnResult + 0.33 * varianceMean);
    }

    public static Function<Instances, Double> getBenchmarkFunctionByName(final String name) {
        switch (name) {
            case "Cluster":
                return data -> {
                    try {
                        return 1 - performClustering(data);
                    } catch (Exception e1) {
                        logger.error("Could not perform clustering benchmark. Reason: {}", e1.getMessage());
                        return 1d;
                    }
                };
            case "KernelCluster":
                return data -> {
                    try {
                        return 1 - performKernelClustering(data, 1);
                    } catch (Exception e1) {
                        logger.error("Could not perform kernel clustering benchmark. Reason: {}", e1.getMessage());
                        return 1d;
                    }
                };
            case "COCO":
                return data -> {
                    try {
                        return calculateCOCOForBatch(data);
                    } catch (InterruptedException e1) {
                        logger.error("Could not perform coco benchmark. Reason: {}", e1.getMessage());
                        Thread.currentThread().interrupt();
                        return 1d;
                    }
                };
            case "COED":
                return data -> {
                    try {
                        return calculateCOEDForBatch(data);
                    } catch (InterruptedException e1) {
                        logger.error("Could not perform coed benchmark. Reason: {}", e1.getMessage());
                        Thread.currentThread().interrupt();
                        return 1d;
                    }
                };
            case "LDA":
                return data -> {
                    try {
                        return 1 - performLDA(data);
                    } catch (Exception e) {
                        logger.error("Could not perform LDA benchmark. Reason: {}", e.getMessage());
                        return 1d;
                    }
                };
            case "KernelLDA":
                return data -> {
                    try {
                        return 1 - performKernelLDA(data, 1);
                    } catch (Exception e) {
                        logger.error("Could not perform cluster LDA benchmark. Reason: {}", e.getMessage());
                        return 1d;
                    }
                };
            case "Ensemble":
                return data -> {
                    try {
                        return 1 - performEnsemble(data);
                    } catch (Exception e) {
                        logger.error("Could not perform ensemble benchmark. Reason: {}", e.getMessage());
                        return 1d;
                    }
                };
            // case "Random":
            default:
                return data -> new Random().nextDouble();
        }
    }

    public static AbstractHASCOFENodeEvaluator getRandomNodeEvaluator(final int maxPipelineSize) {
        return new AbstractHASCOFENodeEvaluator(maxPipelineSize) {

            @Override
            public Double f(final Node<TFDNode, ?> node) {
                if (node.getParent() == null) {
                    return null;
                }

                // If pipeline is too deep, assign worst value
                if (node.path().size() > this.maxPipelineSize) {
                    return AbstractHASCOFEEvaluator.MAX_EVAL_VALUE;
                }

                return null;
            }
        };
    }

    public static double rankKendallsTau(final double[] ranking1, final double[] ranking2) {
        KendallsCorrelation kendalsCorr = new KendallsCorrelation();
        return kendalsCorr.correlation(ranking1, ranking2);
    }

    public static double evaluateMLPlan(final int timeout, final Instances training, final Instances test,
                                        final int seed, final Logger logger, final int numCores)
            throws Exception {

        logger.debug("Starting ML-Plan execution. Training on {} instances with "
                + "{} attributes.", training.numInstances(), training.numAttributes());

        /* Initialize MLPlan using WEKA components */
        MLPlanWekaBuilder builder = AbstractMLPlanBuilder.forWeka();
        builder.withTimeOut(new TimeOut(timeout, TimeUnit.SECONDS));
        builder.withNumCpus(numCores);
        builder.withDataset(training);
        MLPlan mlplan = builder.build();
        mlplan.setRandomSeed(seed);
        Classifier clf = mlplan.call();

        if (mlplan.getSelectedClassifier() == null
                || ((MLPipeline) mlplan.getSelectedClassifier()).getBaseClassifier() == null) {
            logger.warn("Could not find a model using ML-Plan. Returning -1...");
            return -1;
        }

        String solutionString = ((MLPipeline) mlplan.getSelectedClassifier()).getBaseClassifier().getClass().getName()
                + " | " + ((MLPipeline) mlplan.getSelectedClassifier()).getPreprocessors();
        logger.debug("Selected classifier: {}", solutionString);

        /* evaluate solution produced by mlplan */
        Evaluation eval = new Evaluation(training);
        eval.evaluateModel(clf, test);

        return eval.pctCorrect();
    }

    public static double evaluateMLPlan(final int timeout, final Instances instances, final double trainRatio,
                                        final int seed, final Logger logger, final int numCores)
            throws Exception {

        List<Instances> split = WekaUtil.getStratifiedSplit(instances, seed, trainRatio);

        return evaluateMLPlan(timeout, split.get(0), split.get(1), seed, logger, numCores);
    }

    private static List<Map.Entry<Kernel, Instances>> getKernelsWithInstances(final Instances insts) throws KernelInitializationException {
        try {
            ArrayList<Map.Entry<Kernel, Instances>> result = new ArrayList<>();
            Instances rbfInsts = new Instances(insts);
            result.add(new AbstractMap.SimpleEntry<>(new RBFKernel(rbfInsts, 250007, 0.01), rbfInsts));

            Instances poly2Insts = new Instances(insts);
            result.add(new AbstractMap.SimpleEntry<>(new PolyKernel(poly2Insts, 250007, 2, false),
                    poly2Insts));

            Instances poly3Insts = new Instances(insts);
            result.add(new AbstractMap.SimpleEntry<>(new PolyKernel(poly3Insts, 250007, 2, false),
                    poly3Insts));

            return result;
        } catch (Exception e) {
            throw new KernelInitializationException("Could not instantiate a kernel due to an exception.", e);
        }
    }
}
