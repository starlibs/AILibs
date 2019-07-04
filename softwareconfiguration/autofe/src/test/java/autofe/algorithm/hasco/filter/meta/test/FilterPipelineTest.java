package autofe.algorithm.hasco.filter.meta.test;

import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import ai.libs.jaicore.graph.Graph;
import autofe.algorithm.hasco.filter.image.CatalanoBinaryPatternFilter;
import autofe.algorithm.hasco.filter.image.CatalanoInPlaceFilter;
import autofe.algorithm.hasco.filter.meta.FilterPipeline;
import autofe.algorithm.hasco.filter.meta.IFilter;
import autofe.util.DataSet;
import autofe.util.DataSetUtils;
import autofe.util.FileUtils;

public class FilterPipelineTest {
	@Test
	public void testPipelineExecution() throws Exception {
		Graph<IFilter> graph = new Graph<>();

		CatalanoBinaryPatternFilter robustLocalBinPattern = new CatalanoBinaryPatternFilter("RobustLocalBinaryPattern");
		CatalanoInPlaceFilter homogenityEdgeDetector = new CatalanoInPlaceFilter("HomogenityEdgeDetector");

		graph.addItem(robustLocalBinPattern);
		graph.addItem(homogenityEdgeDetector);

		graph.addEdge(robustLocalBinPattern, homogenityEdgeDetector);

		FilterPipeline fp = new FilterPipeline(null, graph);

		DataSet data = DataSetUtils.getDataSetByID(DataSetUtils.MNIST_ID);

		List<DataSet> trainTestSplit = DataSetUtils.getStratifiedSplit(data, new Random(2), .7);

		DataSet transformedDataSet = fp.applyFilter(trainTestSplit.get(0), true);
		FileUtils.saveSingleInstances(transformedDataSet.getInstances(), "generatedOutput_pipeline.arff");

		Assert.assertFalse(transformedDataSet.getIntermediateInstances().isEmpty());

		System.out.println("Transformed data set.");
	}

	// @Test
	public void testMultiplePipelineExecutions() throws Exception {
		Graph<IFilter> graph = new Graph<>();
		Graph<IFilter> graph2 = new Graph<>();

		CatalanoBinaryPatternFilter robustLocalBinPattern = new CatalanoBinaryPatternFilter("RobustLocalBinaryPattern");
		CatalanoInPlaceFilter homogenityEdgeDetector = new CatalanoInPlaceFilter("HomogenityEdgeDetector");

		graph.addItem(homogenityEdgeDetector);
		graph2.addItem(robustLocalBinPattern);

		FilterPipeline fp = new FilterPipeline(null, graph);
		FilterPipeline fp2 = new FilterPipeline(null, graph2);

		DataSet data = DataSetUtils.getDataSetByID(DataSetUtils.MNIST_ID);

		List<DataSet> trainTestSplit = DataSetUtils.getStratifiedSplit(data, new Random(2), .7);

		DataSet transformedDataSet = fp.applyFilter(trainTestSplit.get(0), true);
		transformedDataSet = fp2.applyFilter(transformedDataSet, true);
		// FileUtils.saveSingleInstances(transformedDataSet.getInstances(),
		// "generatedOutput_sep_pipeline.arff");

		System.out.println("Transformed data set.");
	}
}
