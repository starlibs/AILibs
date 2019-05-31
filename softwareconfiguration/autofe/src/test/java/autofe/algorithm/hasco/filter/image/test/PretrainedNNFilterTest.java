package autofe.algorithm.hasco.filter.image.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.junit.Assert;
import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataSetDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.ml.WekaUtil;
import autofe.algorithm.hasco.filter.image.PretrainedNNFilter;
import autofe.util.DataSet;
import autofe.util.DataSetUtils;
import autofe.util.ImageUtils;
import weka.core.Instance;
import weka.core.Instances;

public class PretrainedNNFilterTest {
    private static final Logger logger = LoggerFactory.getLogger(PretrainedNNFilterTest.class);

    @Test
    public void cifar10Test() throws Exception {

        /* load dataset and create a train-test-split */
        OpenmlConnector connector = new OpenmlConnector();
        DataSetDescription ds = connector.dataGet(DataSetUtils.MNIST_ID);
        File file = ds.getDataset("4350e421cdc16404033ef1812ea38c01");
        Instances data = new Instances(new BufferedReader(new FileReader(file)));
        data.setClassIndex(data.numAttributes() - 1);
        List<Instances> split = WekaUtil.getStratifiedSplit(data, 42, .02f);

        logger.info("Calculating intermediates...");
        List<INDArray> intermediate = new ArrayList<>();
        intermediate.add(DataSetUtils.mnistInstanceToMatrix(split.get(0).get(0)));
        logger.info("Finished intermediate calculations.");

        PretrainedNNFilter filter = ImageUtils.getPretrainedNNFilterByName("VGG16", 12, intermediate.get(0).shape());

        logger.debug("Selected layer: " + filter.getCompGraph().getLayer(5).toString());

        DataSet result = filter.applyFilter(new DataSet(split.get(0), intermediate), false);
        logger.info(Arrays.toString(result.getIntermediateInstances().get(0).shape()));

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder().seed(0)
                .list().layer(0, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX).build())
                .build();

        MultiLayerNetwork mln = new MultiLayerNetwork(conf);
        mln.init();
        System.out.println(result.getIntermediateInstances().get(0).mean(3));
        INDArray prediction = mln.output(result.getIntermediateInstances().get(0));
        logger.debug("Prediction shape: " + Arrays.toString(prediction.shape()));
        logger.debug("Prediction mean of element 3: {}", prediction.mean(3));
        Assert.assertFalse(prediction.isEmpty());

    }

    @Test
    public void mnistTest() throws Exception {

        /* load dataset and create a train-test-split */
        OpenmlConnector connector = new OpenmlConnector();
        DataSetDescription ds = connector.dataGet(DataSetUtils.MNIST_ID);
        File file = ds.getDataset("4350e421cdc16404033ef1812ea38c01");
        Instances data = new Instances(new BufferedReader(new FileReader(file)));
        data.setClassIndex(data.numAttributes() - 1);
        List<Instances> split = WekaUtil.getStratifiedSplit(data, 42, .25f);

        logger.info("Calculating intermediates...");
        List<INDArray> intermediate = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Instance inst = split.get(0).get(i);
            intermediate.add(DataSetUtils.mnistInstanceToMatrix(inst));
        }

        PretrainedNNFilter filter = ImageUtils.getPretrainedNNFilterByName("LeNet", 5, intermediate.get(0).shape());

        logger.debug("Selected layer: " + filter.getCompGraph().getLayer(5).toString());

        logger.info(Arrays.toString(intermediate.get(0).shape()));
        logger.info("Finished intermediate calculations.");

        // intermediate = DataSetUtils.grayscaleMatricesToRGB(intermediate);

        DataSet result = filter.applyFilter(new DataSet(split.get(0), intermediate), false);
        logger.info(Arrays.toString(result.getIntermediateInstances().get(0).shape()));
        Assert.assertFalse(result.getIntermediateInstances().isEmpty());
    }
}
