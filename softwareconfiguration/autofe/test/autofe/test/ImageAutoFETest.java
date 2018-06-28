package autofe.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataSetDescription;

import Catalano.Imaging.FastBitmap;
import autofe.util.DataSet;
import autofe.util.DataSetUtils;
import jaicore.ml.WekaUtil;
import weka.core.Instance;
import weka.core.Instances;

public class ImageAutoFETest {
	@Test
	public void testImageAutoFE() throws Exception {
		System.out.println("Starting Image AutoFE test...");

		/* load cifar 10 dataset and create a train-test-split */
		OpenmlConnector connector = new OpenmlConnector();
		DataSetDescription ds = connector.dataGet(40927);
		File file = ds.getDataset("4350e421cdc16404033ef1812ea38c01");
		Instances data = new Instances(new BufferedReader(new FileReader(file)));
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit(data, new Random(0), .7f);

		List<INDArray> intermediate = new ArrayList<>();
		for (Instance inst : split.get(0)) {
//			intermediate.add(DataSetUtils.cifar10InstanceToBitmap(inst));
			intermediate.add(DataSetUtils.cifar10InstanceToMatrix(inst));
		}
		DataSet trainSet = new DataSet(split.get(0), intermediate);

		System.out.println(split.get(0).numAttributes());
	}
}
