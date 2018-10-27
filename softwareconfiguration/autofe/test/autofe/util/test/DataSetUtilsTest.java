package autofe.util.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;

import autofe.algorithm.hasco.filter.image.CatalanoInPlaceFilter;
import autofe.util.DataSet;
import autofe.util.DataSetUtils;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

public class DataSetUtilsTest {
	@Test
	public void cifar10InstancesAttributesTest() {
		ArrayList<Attribute> atts = new ArrayList<>();
		for (int i = 0; i < 32 * 32 * 3 + 1; i++) {
			atts.add(new Attribute("blub" + i));
		}
		Instances instances = new Instances("test", atts, 1);
		DenseInstance inst = new DenseInstance(atts.size());
		for (int i = 0; i < inst.numAttributes(); i++) {
			inst.setValue(i, 1d);
		}
		inst.setDataset(instances);
		instances.add(inst);

		INDArray result = DataSetUtils.cifar10InstanceToMatrix(inst);
		Assert.assertArrayEquals(new long[] { 32, 32, 3 }, result.shape());
	}

	@Test
	public void croppingTest() throws IOException {
		File datasetFolder = new File("testres/" + File.separator + "caltech101_subset");
		DataSet data = DataSetUtils.loadDatasetFromImageFolder(datasetFolder);
		CatalanoInPlaceFilter filter = new CatalanoInPlaceFilter("GaussianBlur");
		filter.applyFilter(data, true);
	}
}
