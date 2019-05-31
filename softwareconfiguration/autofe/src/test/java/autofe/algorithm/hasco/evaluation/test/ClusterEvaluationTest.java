package autofe.algorithm.hasco.evaluation.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataSetDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.ml.WekaUtil;
import autofe.util.DataSetUtils;
import autofe.util.EvaluationUtils;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.FilteredClusterer;
import weka.clusterers.SimpleKMeans;
import weka.core.EuclideanDistance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

public class ClusterEvaluationTest {

    private static final Logger logger = LoggerFactory.getLogger(ClusterEvaluationTest.class);

    @Test
    public void evaluateTest() throws Exception {
        logger.info("Starting cluster evaluation test...");

        /* load dataset and create a train-test-split */
        OpenmlConnector connector = new OpenmlConnector();
        DataSetDescription ds = connector.dataGet(DataSetUtils.SEGMENT_ID);
        File file = ds.getDataset(DataSetUtils.API_KEY);
        Instances data = new Instances(new BufferedReader(new FileReader(file)));
        data.setClassIndex(data.numAttributes() - 1);
        List<Instances> split = WekaUtil.getStratifiedSplit(data, 42, .25f);

        Instances insts = split.get(0);

        long timeStart = System.currentTimeMillis();

        FilteredClusterer clusterer = new FilteredClusterer();

        Remove filter = new Remove();
        filter.setAttributeIndices("" + (insts.classIndex() + 1));
        filter.setInputFormat(insts);
        Instances removedClassInstances = Filter.useFilter(insts, filter);

        ((SimpleKMeans) clusterer.getClusterer())
                .setOptions(new String[]{"-num-slots", String.valueOf(Runtime.getRuntime().availableProcessors()),
                        "-N", String.valueOf(insts.classAttribute().numValues())});
        SimpleKMeans kMeans = (SimpleKMeans) clusterer.getClusterer();
        kMeans.setDistanceFunction(new EuclideanDistance());

        clusterer.buildClusterer(removedClassInstances);

        long timeStartEval = System.currentTimeMillis();

        ClusterEvaluation clusterEval = new ClusterEvaluation();
        clusterEval.setClusterer(clusterer);
        clusterEval.evaluateClusterer(insts);

        long timeTaken = System.currentTimeMillis() - timeStart;
        long timeTakenEval = System.currentTimeMillis() - timeStartEval;

        logger.debug("ClusterEvaluator results: " + clusterEval.clusterResultsToString());

        double acc = EvaluationUtils.predictAccuracy(insts, clusterEval.getClassesToClusters(),
                clusterEval.getClusterAssignments());
        Assert.assertTrue(acc > 0);
        logger.info("Acc: " + acc);
        logger.debug("Clustering took " + (timeTaken / 1000) + " s.");
        logger.debug("Clustering eval took " + (timeTakenEval / 1000) + " s.");
    }

    @Test
    public void kernelClusteringTest() throws Exception {
        logger.info("Starting cluster evaluation test...");

        /* load dataset and create a train-test-split */
        OpenmlConnector connector = new OpenmlConnector();
        DataSetDescription ds = connector.dataGet(DataSetUtils.SEGMENT_ID);
        File file = ds.getDataset(DataSetUtils.API_KEY);
        Instances data = new Instances(new BufferedReader(new FileReader(file)));
        data.setClassIndex(data.numAttributes() - 1);
        List<Instances> split = WekaUtil.getStratifiedSplit(data, 42, .05f);

        Instances insts = split.get(0);
        Assert.assertTrue(EvaluationUtils.performKernelClustering(insts, 1) > 0);
    }
}