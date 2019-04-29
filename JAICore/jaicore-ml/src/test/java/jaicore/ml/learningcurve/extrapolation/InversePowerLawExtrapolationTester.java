package jaicore.ml.learningcurve.extrapolation;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataSetDescription;

import jaicore.ml.core.dataset.sampling.inmemory.factories.SimpleRandomSamplingFactory;
import jaicore.ml.core.dataset.standard.SimpleDataset;
import jaicore.ml.core.dataset.standard.SimpleInstance;
import jaicore.ml.core.dataset.weka.WekaInstancesUtil;
import jaicore.ml.learningcurve.extrapolation.ipl.InversePowerLawExtrapolationMethod;
import jaicore.ml.learningcurve.extrapolation.ipl.InversePowerLawLearningCurve;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class InversePowerLawExtrapolationTester {

	@Test(expected = InvalidAnchorPointsException.class)
	public void testExceptionForIncorrectAnchorpoints() throws Exception {
		int[] xValues = new int[] { 1, 2, 3 };
		LearningCurveExtrapolator<SimpleInstance> extrapolator = createExtrapolationMethod();
		extrapolator.extrapolateLearningCurve(xValues);
	}

	@Test
	public void testInversePowerLawParameterCreation() throws Exception {
		int[] xValues = new int[] { 8, 16, 64, 128 };
		LearningCurveExtrapolator<SimpleInstance> extrapolator = createExtrapolationMethod();
		InversePowerLawLearningCurve curve = (InversePowerLawLearningCurve) extrapolator.extrapolateLearningCurve(xValues);
		Assert.assertNotNull(curve);
		Assert.assertTrue(curve.getCurveValue(256) > 0 && curve.getCurveValue(256) < 1);
	}

	private LearningCurveExtrapolator<SimpleInstance> createExtrapolationMethod() throws Exception {
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

		SimpleDataset simpleDataset = WekaInstancesUtil.wekaInstancesToDataset(dataset);
		return new LearningCurveExtrapolator<>(new InversePowerLawExtrapolationMethod(), new J48(), simpleDataset, 0.7d, new SimpleRandomSamplingFactory<>(), 1l);
	}

}
