package autofe.util.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
	// @Test
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

	// @Test
	public void croppingTest() throws IOException {
		File datasetFolder = new File("testres/" + File.separator + "caltech101_subset");
		DataSet data = DataSetUtils.loadDatasetFromImageFolder(datasetFolder);
		System.out.println("Loaded data.");
		CatalanoInPlaceFilter filter = new CatalanoInPlaceFilter("GaussianBlur");
		filter.applyFilter(data, true);
	}

	// @Test
	public void subsamplingTest() throws Exception {
		DataSet data = DataSetUtils.getDataSetByID(DataSetUtils.MNIST_ID);

		List<DataSet> trainTestSplit = DataSetUtils.getStratifiedSplit(data, new Random(10), false, .7);
		data = trainTestSplit.get(0);

		System.out.println("Num instances / attributes: " + data.getInstances().numInstances() + " / "
				+ data.getInstances().numAttributes());

		System.out.println("Start subsampling without MLPlan factor...");
		DataSet result = DataSetUtils.subsample(data, 0.01, 200, new Random(0));
		System.out.println("Done. " + result.getInstances().numInstances());

		System.out.println("Start subsampling with custom MLPlan factor...");
		result = DataSetUtils.subsample(data, 0.05, 200, new Random(0));
		System.out.println("Done. " + result.getInstances().numInstances());

		System.out.println("Start subsampling with MLPlan factor...");
		result = DataSetUtils.subsample(data, 0.01, 200, new Random(10), 10);
		System.out.println("Done. " + result.getInstances().numInstances());
	}

	// @Test
	public void caltech101Test() throws IOException {
		File datasetFolder = new File("testres/" + File.separator + "caltech101");
		DataSet data = DataSetUtils.loadDatasetFromImageFolder(datasetFolder);
		System.out.println("Loaded data.");
		CatalanoInPlaceFilter filter = new CatalanoInPlaceFilter("GaussianBlur");
		filter.applyFilter(data, true);
	}

	@Test
	public void leafBwTest() throws IOException {
		File datasetFolder = new File("testres/" + File.separator + "leaf_bw");
		DataSet data = DataSetUtils.loadDatasetFromImageFolder(datasetFolder);
		System.out.println("Loaded data.");
		CatalanoInPlaceFilter filter = new CatalanoInPlaceFilter("GaussianBlur");
		DataSet result = filter.applyFilter(data, true);
		System.out.println(result.getIntermediateInstances().get(0).shapeInfoToString());
	}

	// @Test
	public void leafRgbTest() throws IOException {
		File datasetFolder = new File("testres/" + File.separator + "leaf_rgb");
		DataSet data = DataSetUtils.loadDatasetFromImageFolder(datasetFolder);
		System.out.println("Loaded data.");
		CatalanoInPlaceFilter filter = new CatalanoInPlaceFilter("GaussianBlur");
		filter.applyFilter(data, true);
	}
}
