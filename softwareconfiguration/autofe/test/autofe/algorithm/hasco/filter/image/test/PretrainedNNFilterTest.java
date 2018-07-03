package autofe.algorithm.hasco.filter.image.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.deeplearning4j.nn.conf.CacheMode;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.WorkspaceMode;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer.AlgoMode;
import org.deeplearning4j.zoo.model.AlexNet;
import org.deeplearning4j.zoo.model.VGG16;
import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataSetDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.algorithm.hasco.filter.image.PretrainedNNFilter;
import autofe.util.DataSet;
import autofe.util.DataSetUtils;
import jaicore.ml.WekaUtil;
import weka.core.Instance;
import weka.core.Instances;

public class PretrainedNNFilterTest {
	private static final Logger logger = LoggerFactory.getLogger(PretrainedNNFilterTest.class);

	@Test
	public void applyFilterTest() throws Exception {
		PretrainedNNFilter filter = new PretrainedNNFilter(new VGG16(42, new int[] {3, 32,32}, 10,new Nesterovs(1e-2, 0.9), CacheMode.NONE, WorkspaceMode.ENABLED, AlgoMode.PREFER_FASTEST), 5);
		
		/* load cifar 10 dataset and create a train-test-split */
		OpenmlConnector connector = new OpenmlConnector();
		DataSetDescription ds = connector.dataGet(40927);
		File file = ds.getDataset("4350e421cdc16404033ef1812ea38c01");
		Instances data = new Instances(new BufferedReader(new FileReader(file)));
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit(data, new Random(42), .02f);

		logger.info("Calculating intermediates...");
		List<INDArray> intermediate = new ArrayList<>();
		intermediate.add(DataSetUtils.cifar10InstanceToMatrix(split.get(0).get(0)));
//		for (Instance inst : split.get(0)) {
			// intermediate.add(DataSetUtils.cifar10InstanceToBitmap(inst));
//			intermediate.add(DataSetUtils.cifar10InstanceToMatrix(inst));
//		}
		logger.info("Finished intermediate calculations.");
		
		DataSet result = filter.applyFilter(new DataSet(split.get(0), intermediate), false);
		logger.info(result.getIntermediateInstances().get(0).toString());
	}
}
