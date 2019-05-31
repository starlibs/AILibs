package autofe.algorithm.hasco.filter.image.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataSetDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.graph.Graph;
import ai.libs.jaicore.ml.WekaUtil;
import autofe.algorithm.hasco.filter.image.CatalanoBinaryPatternFilter;
import autofe.algorithm.hasco.filter.image.CatalanoInPlaceFilter;
import autofe.algorithm.hasco.filter.meta.FilterPipeline;
import autofe.algorithm.hasco.filter.meta.IFilter;
import autofe.util.DataSet;
import autofe.util.DataSetUtils;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class CatalanoFilterTest {

	private static final Logger logger = LoggerFactory.getLogger(CatalanoFilterTest.class);

	// @Test
	public void testCatalanoWrapper() throws Exception {
		OpenmlConnector connector = new OpenmlConnector();
		DataSetDescription ds = connector.dataGet(DataSetUtils.MNIST_ID);
		File file = ds.getDataset("4350e421cdc16404033ef1812ea38c01");
		Instances data = new Instances(new BufferedReader(new FileReader(file)));
		data.setClassIndex(data.numAttributes() - 1);
		List<Instances> split = WekaUtil.getStratifiedSplit(data, 42, .25f);

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

	// @Test
	public void testCatalanoWrapperNaN() throws InterruptedException {
		String instString = "[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,62,254,151,0,0,0,0,0,0,31,10,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,183,253,151,0,0,0,0,0,0,233,172,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,52,253,254,151,0,0,0,0,0,102,254,253,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,51,252,253,70,0,0,0,0,0,102,253,252,41,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,214,253,244,40,0,0,0,0,0,102,254,253,102,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,253,252,162,0,0,0,0,0,0,102,253,252,102,0,0,0,0,0,0,0,0,0,0,0,0,0,0,102,254,253,102,0,0,0,0,0,0,102,254,253,102,0,0,0,0,0,0,0,0,0,0,0,0,0,0,183,253,252,102,82,82,0,0,0,0,102,253,252,20,0,0,0,0,0,0,0,0,0,0,0,0,0,52,253,254,253,254,253,254,253,254,253,254,253,254,253,21,0,0,0,0,0,0,0,0,0,0,0,0,0,51,252,253,252,253,252,253,252,253,252,253,252,253,252,203,20,0,0,0,0,0,0,0,0,0,0,0,0,0,82,203,203,203,122,102,102,82,0,31,233,254,253,82,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,51,252,253,252,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,72,253,254,253,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,152,252,253,212,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,152,253,254,131,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,152,252,233,30,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,21,255,253,204,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,142,253,252,203,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,204,255,253,183,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,162,253,252,61,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4]";

		double[] arr = Arrays.stream(instString.substring(1, instString.length() - 1).split(",")).map(String::trim)
				.mapToDouble(Double::parseDouble).toArray();

		ArrayList<Attribute> atts = new ArrayList<Attribute>();
		for (int i = 0; i < arr.length - 1; i++) {
			atts.add(new Attribute("att" + i));
		}
		atts.add(new Attribute("class", Arrays.asList("4")));
		final Instances batch = new Instances("Test", atts, 1);
		batch.setClassIndex(batch.numAttributes() - 1);

		final Instance inst = new DenseInstance(1, arr);
		inst.setDataset(batch);
		inst.setClassValue("4");
		batch.add(inst);

		final List<INDArray> intermediate = new ArrayList<>();
		for (Instance i : batch) {
			intermediate.add(DataSetUtils.instanceToMatrixByDataSet(i, DataSetUtils.MNIST_ID));
		}

		DataSet ds = new DataSet(batch, intermediate);

		CatalanoInPlaceFilter filter = new CatalanoInPlaceFilter("HomogenityEdgeDetector");
		DataSet transformedData = filter.applyFilter(ds, true);
		transformedData.updateInstances();

		CatalanoInPlaceFilter filter2 = new CatalanoInPlaceFilter("HighBoost");
		transformedData = filter2.applyFilter(transformedData, true);
		transformedData.updateInstances();

		System.out.println(Arrays.toString(transformedData.getInstances().get(0).toDoubleArray()));
	}

	// @Test
	public void testBinaryPatternWrapper() throws Exception {
		DataSet data = DataSetUtils.getDataSetByID(DataSetUtils.MNIST_ID);

		List<DataSet> trainTestSplit = DataSetUtils.getStratifiedSplit(data, new Random(2), .7);

		CatalanoBinaryPatternFilter robustLocalBinPattern = new CatalanoBinaryPatternFilter("RobustLocalBinaryPattern");
		CatalanoInPlaceFilter homogenityEdgeDetector = new CatalanoInPlaceFilter("HomogenityEdgeDetector");

		DataSet transformedDataSet;
		transformedDataSet = homogenityEdgeDetector.applyFilter(trainTestSplit.get(0), true);

		System.out.println("Transformed data set.");
	}

	// @Test
	public void testBinaryPatternWrapperWithInstance() throws InterruptedException {

		Graph<IFilter> graph = new Graph<>();

		CatalanoBinaryPatternFilter robustLocalBinPattern = new CatalanoBinaryPatternFilter("RobustLocalBinaryPattern");
		CatalanoInPlaceFilter homogenityEdgeDetector = new CatalanoInPlaceFilter("HomogenityEdgeDetector");

		graph.addItem(robustLocalBinPattern);
		graph.addItem(homogenityEdgeDetector);

		// At first rlbp -> hed indicates inverse execution
		graph.addEdge(robustLocalBinPattern, homogenityEdgeDetector);

		FilterPipeline fp = new FilterPipeline(null, graph);

		String instString = "[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,129,254,148,144,57,34,13,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,148,253,253,253,253,253,207,177,177,163,67,12,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,41,193,253,253,253,253,253,253,253,254,253,218,210,186,138,162,82,0,0,0,0,0,0,0,0,0,0,0,0,4,20,121,159,231,245,247,251,254,253,253,253,253,253,253,209,6,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,54,61,77,150,150,198,150,198,217,253,253,66,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,19,237,253,66,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,232,253,167,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,232,253,156,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,232,253,114,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,232,253,66,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,233,255,66,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,232,253,66,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,74,250,217,12,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,99,253,209,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,151,253,185,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,199,253,99,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,51,249,253,99,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,56,253,253,99,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,56,253,253,99,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,32,181,249,56,0,0,0,0,0,0,0,0,7]";

		double[] arr = Arrays.stream(instString.substring(1, instString.length() - 1).split(",")).map(String::trim)
				.mapToDouble(Double::parseDouble).toArray();

		ArrayList<Attribute> atts = new ArrayList<>();
		for (int i = 0; i < arr.length - 1; i++) {
			atts.add(new Attribute("att" + i));
		}
		atts.add(new Attribute("class", Arrays.asList("7")));
		final Instances batch = new Instances("Test", atts, 1);
		batch.setClassIndex(batch.numAttributes() - 1);

		final Instance inst = new DenseInstance(1, arr);
		inst.setDataset(batch);
		inst.setClassValue("7");
		batch.add(inst);

		final List<INDArray> intermediate = new ArrayList<>();
		for (Instance i : batch) {
			intermediate.add(DataSetUtils.instanceToMatrixByDataSet(i, DataSetUtils.MNIST_ID));
		}

		DataSet ds = new DataSet(batch, intermediate);

		DataSet transformedData = fp.applyFilter(ds, true);

		System.out.println(Arrays.toString(transformedData.getInstances().get(0).toDoubleArray()));

	}

	@Test
	public void testBinaryPatternFilter() throws Exception {
		Graph<IFilter> graph = new Graph<>();

		CatalanoBinaryPatternFilter robustLocalBinPattern = new CatalanoBinaryPatternFilter(
				"UniformLocalBinaryPattern");
		CatalanoInPlaceFilter erosion = new CatalanoInPlaceFilter("Erosion");

		graph.addItem(robustLocalBinPattern);
		graph.addItem(erosion);
		graph.addEdge(robustLocalBinPattern, erosion);
		FilterPipeline fp = new FilterPipeline(null, graph);

		DataSet dataSet = DataSetUtils.getDataSetByID(DataSetUtils.FASHION_MNIST_ID);
		DataSet result = fp.applyFilter(
				new DataSet(dataSet.getInstances(),
						Arrays.asList(dataSet.getIntermediateInstances().get(1),
								dataSet.getIntermediateInstances().get(0), dataSet.getIntermediateInstances().get(2))),
				false);
		Assert.assertFalse(result.getIntermediateInstances().isEmpty());
		logger.debug("Intermediate instance 0: {}", result.getIntermediateInstances().get(0));
		logger.debug("Intermediate instance 1: {}", result.getIntermediateInstances().get(1));
		logger.debug("Intermediate instance 2: {}", result.getIntermediateInstances().get(2));
	}
}
