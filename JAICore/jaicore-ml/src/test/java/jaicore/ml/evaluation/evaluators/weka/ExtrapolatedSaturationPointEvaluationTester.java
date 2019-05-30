package jaicore.ml.evaluation.evaluators.weka;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;
import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataSetDescription;

import jaicore.ml.core.dataset.DatasetCreationException;
import jaicore.ml.core.dataset.sampling.inmemory.factories.SystematicSamplingFactory;
import jaicore.ml.core.dataset.weka.WekaInstance;
import jaicore.ml.core.dataset.weka.WekaInstances;
import jaicore.ml.learningcurve.extrapolation.ipl.InversePowerLawExtrapolationMethod;
import weka.classifiers.functions.SMO;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class ExtrapolatedSaturationPointEvaluationTester {

	private WekaInstances<Object> train, test;

	@Test
	public void testClassifierEvaluationAtSaturationPoint() throws Exception {
		// Load dataset from OpenML and create stratified split
		Instances dataset = null;
		OpenmlConnector client = new OpenmlConnector();
		DataSetDescription description = client.dataGet(42);
		File file = description.getDataset("4350e421cdc16404033ef1812ea38c01");
		DataSource source = new DataSource(file.getCanonicalPath());
		dataset = source.getDataSet();
		dataset.setClassIndex(dataset.numAttributes() - 1);
		Attribute targetAttribute = dataset.attribute(description.getDefault_target_attribute());
		dataset.setClassIndex(targetAttribute.index());
		this.createSplit(new WekaInstances<>(dataset), 0.8, 123l);

		// Test classifier evaluation at saturation point
		ExtrapolatedSaturationPointEvaluator<WekaInstance<Object>, WekaInstances<Object>> evaluator = new ExtrapolatedSaturationPointEvaluator<>(
				new int[] { 8, 16, 64, 128 }, new SystematicSamplingFactory<>(), this.train, 0.7,
				new InversePowerLawExtrapolationMethod(), 123l, this.test);
		evaluator.setEpsilon(0.0005d);
		double evaluationResult = evaluator.evaluate(new SMO());
		Assert.assertTrue(evaluationResult > 0 && evaluationResult <= 100);
	}

	private void createSplit(WekaInstances<Object> dataset, double trainsplit, long seed) throws DatasetCreationException {
		this.train = dataset.createEmpty();
		this.test = dataset.createEmpty();
		WekaInstances<Object> data = dataset.createEmpty();
		data.addAll(dataset);

		// Shuffle the data
		Random random = new Random(seed);
		Collections.shuffle(data, random);

		// Stratify the data by class
		Map<Object, WekaInstances<Object>> classStrati = new HashMap<>();
		for (WekaInstance<Object> d : dataset) {
			Object c = d.getTargetValue();
			if (!classStrati.containsKey(c)) {
				classStrati.put(c, dataset.createEmpty());
			}
			classStrati.get(c).add(d);
		};

		// Retrieve strati sizes
		Map<Object, Integer> classStratiSizes = new HashMap<>(classStrati.size());
		for (Object c : classStrati.keySet()) {
			classStratiSizes.put(c, classStrati.get(c).size());
		}

		// First assign one item of each class to train and test
		for (Object c : classStrati.keySet()) {
			WekaInstances<Object> availableInstances = classStrati.get(c);
			if (!availableInstances.isEmpty()) {
				train.add(availableInstances.get(0));
				availableInstances.remove(0);
			}
			if (!availableInstances.isEmpty()) {
				test.add(availableInstances.get(0));
				availableInstances.remove(0);
			}
		}

		// Distribute remaining instances over train test
		for (Object c : classStrati.keySet()) {
			WekaInstances<Object> availableInstances = classStrati.get(c);
			int trainItems = (int) Math.min(availableInstances.size(), Math.ceil(trainsplit * classStratiSizes.get(c)));
			for (int j = 0; j < trainItems; j++) {
				this.train.add(availableInstances.get(0));
				availableInstances.remove(0);
			}
			int testItems = (int) Math.min(availableInstances.size(),
					Math.ceil((1 - trainsplit) * classStratiSizes.get(c)));
			for (int j = 0; j < testItems; j++) {
				this.test.add(availableInstances.get(0));
				availableInstances.remove(0);
			}
		}

		// Shuffle train and test
		Collections.shuffle(this.train, random);
		Collections.shuffle(this.test, random);
	}

}
