package autofe.algorithm.hasco.filter.image.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataSetDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.algorithm.hasco.filter.image.CatalanoInPlaceFilter;
import autofe.util.DataSet;
import autofe.util.DataSetUtils;
import jaicore.ml.WekaUtil;
import weka.core.Instance;
import weka.core.Instances;

public class CatalanoFilterTest {

	private static final Logger logger = LoggerFactory.getLogger(CatalanoFilterTest.class);

	@Test
	public void testCatalanoWrapper() throws Exception {
		OpenmlConnector connector = new OpenmlConnector();
		DataSetDescription ds = connector.dataGet(DataSetUtils.MNIST_ID);
		File file = ds.getDataset("4350e421cdc16404033ef1812ea38c01");
		Instances data = new Instances(new BufferedReader(new FileReader(file)));
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit(data, new Random(42), .25f);

		logger.info("Calculating intermediates...");
		List<INDArray> intermediate = new ArrayList<>();
		for (Instance inst : split.get(0)) {
			intermediate.add(DataSetUtils.instanceToMatrixByDataSet(inst, DataSetUtils.MNIST_ID));
		}
		logger.info("Finished intermediate calculations.");
		DataSet originDataSet = new DataSet(split.get(0), intermediate);

		CatalanoInPlaceFilter filter = new CatalanoInPlaceFilter("GaborFilter");
		// IdentityFilter filter = new IdentityFilter();
		DataSet transDataSet = filter.applyFilter(originDataSet, true);
		transDataSet.updateInstances();

		System.out.println(Arrays.toString(transDataSet.getInstances().get(0).toDoubleArray()));
	}
}
