package jaicore.ml.learningcurve.extrapolation;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataSetDescription;

import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.ml.core.dataset.DatasetCreationException;
import jaicore.ml.core.dataset.sampling.inmemory.factories.SystematicSamplingFactory;
import jaicore.ml.core.dataset.weka.WekaInstance;
import jaicore.ml.core.dataset.weka.WekaInstances;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class AnchorpointsCreationTest {

	@Test
	public void anchorpointsAreCreatedAndHaveTheValues()
			throws IOException, InvalidAnchorPointsException, AlgorithmException, InterruptedException, ClassNotFoundException, DatasetCreationException {
		int[] xValues = new int[] { 2, 4, 8, 16, 32, 64 };
		Instances dataset = null;
		OpenmlConnector client = new OpenmlConnector();
		try {
			DataSetDescription description = client.dataGet(42);
			File file = description.getDataset("4350e421cdc16404033ef1812ea38c01");
			DataSource source = new DataSource(file.getCanonicalPath());
			dataset = source.getDataSet();
			dataset.setClassIndex(dataset.numAttributes() - 1);
			Attribute targetAttribute = dataset.attribute(description.getDefault_target_attribute());
			dataset.setClassIndex(targetAttribute.index());
		} catch (Exception e) {
			throw new IOException("Could not load data set from OpenML!", e);
		}

		WekaInstances<Object> simpleDataset = new WekaInstances<>(dataset);
		LearningCurveExtrapolator<WekaInstance<Object>, WekaInstances<Object>> extrapolator = new LearningCurveExtrapolator<>((x, y, ds) -> {
			Assert.assertArrayEquals(x, xValues);
			for (int i = 0; i < y.length; i++) {
				Assert.assertTrue(y[i] > 0.0d);
			}
			return null;
		}, new J48(), simpleDataset, 0.7d, xValues, new SystematicSamplingFactory<>(), 1l);
		extrapolator.extrapolateLearningCurve();
	}

}
