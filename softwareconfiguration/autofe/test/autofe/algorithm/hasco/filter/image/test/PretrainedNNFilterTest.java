package autofe.algorithm.hasco.filter.image.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataSetDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.algorithm.hasco.filter.image.PretrainedNNFilter;
import autofe.util.DataSet;
import autofe.util.DataSetUtils;
import autofe.util.ImageUtils;
import jaicore.ml.WekaUtil;
import weka.core.Instance;
import weka.core.Instances;

public class PretrainedNNFilterTest {
	private static final Logger logger = LoggerFactory.getLogger(PretrainedNNFilterTest.class);

	@Test
	public void cifar10Test() throws Exception {

		// for (Layer layer : filter.getCompGraph().getLayers()) {
		// logger.debug("Layer: " + layer.toString());
		// }

		/* load dataset and create a train-test-split */
		OpenmlConnector connector = new OpenmlConnector();
		DataSetDescription ds = connector.dataGet(DataSetUtils.MNIST_ID);
		File file = ds.getDataset("4350e421cdc16404033ef1812ea38c01");
		Instances data = new Instances(new BufferedReader(new FileReader(file)));
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit(data, new Random(42), .02f);

		logger.info("Calculating intermediates...");
		List<INDArray> intermediate = new ArrayList<>();
		intermediate.add(DataSetUtils.mnistInstanceToMatrix(split.get(0).get(0)));
		// for (Instance inst : split.get(0)) {
		// intermediate.add(DataSetUtils.cifar10InstanceToBitmap(inst));
		// intermediate.add(DataSetUtils.cifar10InstanceToMatrix(inst));
		// }
		logger.info("Finished intermediate calculations.");

		// PretrainedNNFilter filter = new PretrainedNNFilter(new VGG16(42, new int[] {
		// 1, 3, 32, 32 }, 10,
		// new Nesterovs(1e-2, 0.9), CacheMode.NONE, WorkspaceMode.ENABLED,
		// AlgoMode.PREFER_FASTEST), 5,
		// intermediate.get(0).shape(), "VGG16");
		PretrainedNNFilter filter = ImageUtils.getPretrainedNNFilterByName("VGG16", 12, intermediate.get(0).shape());
		// PretrainedNNFilter filter =
		// ImageUtils.getPretrainedNNFilterByName("ResNet50", 5,
		// intermediate.get(0).shape());

		logger.debug("Selected layer: " + filter.getCompGraph().getLayer(5).toString());

		DataSet result = filter.applyFilter(new DataSet(split.get(0), intermediate), false);
		logger.info(Arrays.toString(result.getIntermediateInstances().get(0).shape()));
		
		MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder().seed(0)
				.list().layer(0, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX).build()) // .padding(1,1).stride(2,2)..kernelSize(2,2).
				.pretrain(false).build(); // .layer(1, new OutputLayer.Builder().nOut(4096).build())
//		System.out.println(conf.toString());
		
		MultiLayerNetwork mln = new MultiLayerNetwork(conf);
		mln.init();
//		System.out.println(Arrays.toString(mln.getLayers()));
		System.out.println(result.getIntermediateInstances().get(0).mean(3));
		INDArray prediction = mln.output(result.getIntermediateInstances().get(0));
//		System.out.println(Arrays.toString(mln.output(result.getIntermediateInstances().get(0)).shape()));
		System.out.println("Prediction shape: " + Arrays.toString(prediction.shape()));
		System.out.println(prediction.mean(3));
		
	}

	// @Test
	public void mnistTest() throws Exception {

		// PretrainedNNFilter filter = new PretrainedNNFilter(new VGG16(42, new int[] {
		// 1, 1, 28, 28 }, 10,
		// new Nesterovs(1e-2, 0.9), CacheMode.NONE, WorkspaceMode.ENABLED,
		// AlgoMode.PREFER_FASTEST), 5,
		// new int[] { 1, 1, 28, 28 }, "VGG16");

		// PretrainedNNFilter filter = new PretrainedNNFilter(
		// new ResNet50(42, new int[] { 1, 1, 28, 28 }, 10, WeightInit.DISTRIBUTION, new
		// Nesterovs(1e-2, 0.9),
		// CacheMode.NONE, WorkspaceMode.ENABLED, AlgoMode.PREFER_FASTEST),
		// 5, new int[] { 1, 1, 28, 28 }, "ResNet50");

		// PretrainedNNFilter filter = new PretrainedNNFilter(new VGG19(42, new int[] {
		// 1, 1, 28, 28 }, 10,
		// new Nesterovs(1e-2, 0.9), CacheMode.NONE, WorkspaceMode.ENABLED,
		// AlgoMode.PREFER_FASTEST), 5,
		// new int[] { 1, 1, 28, 28 }, "VGG19");

		// for (Layer layer : filter.getCompGraph().getLayers()) {
		// logger.debug("Layer: " + layer.toString());
		// }

		/* load dataset and create a train-test-split */
		OpenmlConnector connector = new OpenmlConnector();
		DataSetDescription ds = connector.dataGet(DataSetUtils.MNIST_ID);
		File file = ds.getDataset("4350e421cdc16404033ef1812ea38c01");
		Instances data = new Instances(new BufferedReader(new FileReader(file)));
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit(data, new Random(42), .25f);

		logger.info("Calculating intermediates...");
		List<INDArray> intermediate = new ArrayList<>();
		// intermediate.add(DataSetUtils.mnistInstanceToMatrix(split.get(0).get(0)));
		// for (int i = 0; i < split.get(0).size(); i++) {
		for (int i = 0; i < 10; i++) {
			Instance inst = split.get(0).get(i);
			// intermediate.add(DataSetUtils.cifar10InstanceToBitmap(inst));
			intermediate.add(DataSetUtils.mnistInstanceToMatrix(inst));
		}

		// PretrainedNNFilter filter = new PretrainedNNFilter(new LeNet(42,
		// intermediate.get(0).shape(), 10,
		// new Nesterovs(1e-2, 0.9), CacheMode.NONE, WorkspaceMode.ENABLED,
		// AlgoMode.PREFER_FASTEST), 5,
		// intermediate.get(0).shape(), "LeNet");

		PretrainedNNFilter filter = ImageUtils.getPretrainedNNFilterByName("LeNet", 5, intermediate.get(0).shape());

		logger.debug("Selected layer: " + filter.getCompGraph().getLayer(5).toString());

		logger.info(Arrays.toString(intermediate.get(0).shape()));
		logger.info("Finished intermediate calculations.");

		// intermediate = DataSetUtils.grayscaleMatricesToRGB(intermediate);

		DataSet result = filter.applyFilter(new DataSet(split.get(0), intermediate), false);
		logger.info(Arrays.toString(result.getIntermediateInstances().get(0).shape()));
		// logger.info(Arrays.toString(Nd4j.toFlattened(result.getIntermediateInstances().get(0)).toDoubleVector()));
		// logger.info(Arrays.toString(Nd4j.toFlattened(result.getIntermediateInstances().get(1)).toDoubleVector()));
	}
}
