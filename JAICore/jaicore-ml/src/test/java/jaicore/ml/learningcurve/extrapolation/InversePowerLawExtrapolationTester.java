package jaicore.ml.learningcurve.extrapolation;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataSetDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.ml.core.dataset.sampling.inmemory.factories.SimpleRandomSamplingFactory;
import jaicore.ml.core.dataset.weka.WekaInstance;
import jaicore.ml.core.dataset.weka.WekaInstances;
import jaicore.ml.learningcurve.extrapolation.ipl.InversePowerLawExtrapolationMethod;
import jaicore.ml.learningcurve.extrapolation.ipl.InversePowerLawLearningCurve;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class InversePowerLawExtrapolationTester {
	private static final Logger logger = LoggerFactory.getLogger(InversePowerLawExtrapolationTester.class);

	@Test(expected = InvalidAnchorPointsException.class)
	public void testExceptionForIncorrectAnchorpoints() throws Exception {
		LearningCurveExtrapolator<WekaInstance<Object>, WekaInstances<Object>> extrapolator = this.createExtrapolationMethod(new int[] { 1, 2, 3 });
		extrapolator.extrapolateLearningCurve();
	}

	@Test
	public void testInversePowerLawParameterCreation() throws Exception {
		LearningCurveExtrapolator<WekaInstance<Object>, WekaInstances<Object>> extrapolator = this.createExtrapolationMethod(new int[] { 8, 16, 64, 128 });
		InversePowerLawLearningCurve curve = (InversePowerLawLearningCurve) extrapolator.extrapolateLearningCurve();
		Assert.assertNotNull(curve);
		for (int i = 5; i < 20; i++) {
			int k = (int) Math.pow(2, i);
			double val = curve.getCurveValue(k);
			logger.info("Extrapolated learning curve value at {} is {}", k, val);
			Assert.assertTrue(val > 0 && val < 1);
		}
	}

	private LearningCurveExtrapolator<WekaInstance<Object>, WekaInstances<Object>> createExtrapolationMethod(final int[] xValues) throws Exception {
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

		return new LearningCurveExtrapolator<>(new InversePowerLawExtrapolationMethod(), new J48(), new WekaInstances<>(dataset), 0.7d, xValues, new SimpleRandomSamplingFactory<>(), 1l);
	}

}
