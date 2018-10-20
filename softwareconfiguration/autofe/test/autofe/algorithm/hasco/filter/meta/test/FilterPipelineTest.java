package autofe.algorithm.hasco.filter.meta.test;

import java.util.List;
import java.util.Random;

import org.junit.Test;

import autofe.algorithm.hasco.filter.generic.IdentityFilter;
import autofe.algorithm.hasco.filter.image.CatalanoBinaryPatternFilter;
import autofe.algorithm.hasco.filter.image.CatalanoInPlaceFilter;
import autofe.algorithm.hasco.filter.meta.FilterPipeline;
import autofe.algorithm.hasco.filter.meta.IFilter;
import autofe.util.DataSet;
import autofe.util.DataSetUtils;
import autofe.util.FileUtils;
import jaicore.graph.Graph;

public class FilterPipelineTest {
	@Test
	public void testPipelineExecution() throws Exception {
		Graph<IFilter> graph = new Graph<>();

		// UnionFilter union = new UnionFilter();
		IdentityFilter ident = new IdentityFilter();
		// CatalanoInPlaceFilter highBoost = new CatalanoInPlaceFilter("HighBoost");
		CatalanoBinaryPatternFilter robustLocalBinPattern = new CatalanoBinaryPatternFilter("RobustLocalBinaryPattern");
		CatalanoInPlaceFilter homogenityEdgeDetector = new CatalanoInPlaceFilter("HomogenityEdgeDetector");

		// graph.addItem(union);
		// graph.addItem(ident);
		// graph.addItem(highBoost);
		graph.addItem(robustLocalBinPattern);
		graph.addItem(homogenityEdgeDetector);

		// graph.addEdge(union, highBoost);
		graph.addEdge(robustLocalBinPattern, homogenityEdgeDetector);
		// graph.addEdge(ident, robustLocalBinPattern);

		// graph.addEdge(highBoost, homogenityEdgeDetector);

		FilterPipeline fp = new FilterPipeline(graph);

		DataSet data = DataSetUtils.getDataSetByID(DataSetUtils.MNIST_ID);

		List<DataSet> trainTestSplit = DataSetUtils.getStratifiedSplit(data, new Random(2), .7);

		DataSet transformedDataSet = fp.applyFilter(trainTestSplit.get(0), true);
		FileUtils.saveSingleInstances(transformedDataSet.getInstances(), "generatedOutput_pipeline.arff");

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

		FilterPipeline fp = new FilterPipeline(graph);
		FilterPipeline fp2 = new FilterPipeline(graph2);

		DataSet data = DataSetUtils.getDataSetByID(DataSetUtils.MNIST_ID);

		List<DataSet> trainTestSplit = DataSetUtils.getStratifiedSplit(data, new Random(2), .7);

		DataSet transformedDataSet = fp.applyFilter(trainTestSplit.get(0), true);
		transformedDataSet = fp2.applyFilter(transformedDataSet, true);
		// FileUtils.saveSingleInstances(transformedDataSet.getInstances(),
		// "generatedOutput_sep_pipeline.arff");

		System.out.println("Transformed data set.");
	}
}
