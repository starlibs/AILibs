package autofe.algorithm.hasco.test;

import autofe.util.test.DataSetUtilsTest;
import org.junit.Assert;
import org.junit.Test;

import ai.libs.jaicore.graph.Graph;
import ai.libs.jaicore.ml.WekaUtil;
import autofe.algorithm.hasco.AutoFEWekaPipeline;
import autofe.algorithm.hasco.filter.image.PretrainedNNFilter;
import autofe.algorithm.hasco.filter.meta.FilterPipeline;
import autofe.algorithm.hasco.filter.meta.IFilter;
import autofe.util.DataSetUtils;
import autofe.util.ImageUtils;
import weka.classifiers.Classifier;
import weka.classifiers.trees.RandomForest;

public class AutoFEWekaPipelineTest {
	@Test
	public void testAutoFEWekaPipelineClone() throws Exception {

		Graph<IFilter> graph = new Graph<>();
		PretrainedNNFilter nnFilter = ImageUtils.getPretrainedNNFilterByName("VGG16", 5,
				DataSetUtilsTest.CIFAR10_INPUT_SHAPE);
		graph.addItem(nnFilter);

		FilterPipeline fp = new FilterPipeline(null, graph);

		AutoFEWekaPipeline pipeline = new AutoFEWekaPipeline(fp, new RandomForest());
		Classifier clonedClassifier = WekaUtil.cloneClassifier(pipeline);
		Assert.assertNotNull(clonedClassifier);
	}
}
