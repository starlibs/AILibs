package autofe.algorithm.hasco.test;

import org.junit.Test;

import autofe.algorithm.hasco.AutoFEWekaPipeline;
import autofe.algorithm.hasco.filter.image.PretrainedNNFilter;
import autofe.algorithm.hasco.filter.meta.FilterPipeline;
import autofe.algorithm.hasco.filter.meta.IFilter;
import autofe.util.DataSetUtils;
import autofe.util.ImageUtils;
import jaicore.graph.Graph;
import jaicore.ml.WekaUtil;
import weka.classifiers.Classifier;
import weka.classifiers.trees.RandomForest;

public class AutoFEWekaPipelineTest {
	@Test
	public void testAutoFEWekaPipelineClone() throws Exception {

		Graph<IFilter> graph = new Graph<>();
		PretrainedNNFilter nnFilter = ImageUtils.getPretrainedNNFilterByName("VGG16", 5,
				DataSetUtils.CIFAR10_INPUT_SHAPE);
		graph.addItem(nnFilter);

		FilterPipeline fp = new FilterPipeline(graph);

		AutoFEWekaPipeline pipeline = new AutoFEWekaPipeline(fp, new RandomForest());
		Classifier clonedClassifier = WekaUtil.cloneClassifier(pipeline);
	}
}
