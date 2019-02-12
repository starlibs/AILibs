package jaicore.ml.evaluation.evaluators.weka;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;
import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataSetDescription;

import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.dataset.sampling.SubsamplingMethod;
import jaicore.ml.core.dataset.sampling.WekaInstancesUtil;
import jaicore.ml.learningcurve.extrapolation.InversePowerLaw.InversePowerLawExtrapolator;
import weka.classifiers.functions.SMO;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class ExtrapolatedSaturationPointEvaluationTester {

	private IDataset<IInstance> train, test;

	@Test
	public void testClassifierEvaluationAtSaturationPoint() {
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
			fail();
		}
		IDataset<IInstance> simpleDataset = WekaInstancesUtil.wekaInstancesToDataset(dataset);
		this.createSplit(simpleDataset, 0.8, 123l);
		ExtrapolatedSaturationPointEvaluator evaluator = new ExtrapolatedSaturationPointEvaluator(
				new int[] { 8, 16, 64, 128 }, SubsamplingMethod.SYSTEMATIC_SAMPLING, this.train, 0.7,
				new InversePowerLawExtrapolator(), 123l, 0.0000000000005d, this.test);
		double evaluationResult;
		try {
			evaluationResult = evaluator.evaluate(new SMO());
			System.out.println("################### " + evaluationResult);
			Assert.assertTrue(evaluationResult > 0 && evaluationResult <= 100);
		} catch (Exception e) {
			fail();
		}
	}

	private void createSplit(IDataset<IInstance> dataset, double trainsplit, long seed) {
		this.train = dataset.createEmpty();
		this.test = dataset.createEmpty();
		IDataset<IInstance> data = dataset.createEmpty();
		data.addAll(dataset);

		// Shuffle the data
		Random random = new Random(seed);
		Collections.shuffle(data, random);

		// Stratify the data by class
		Map<Object, IDataset<IInstance>> classStrati = new HashMap<Object, IDataset<IInstance>>();
		dataset.forEach(d -> {
			Object c = d.getTargetValue(Object.class).getValue();
			if (!classStrati.containsKey(c)) {
				classStrati.put(c, dataset.createEmpty());
			}
			classStrati.get(c).add(d);
		});

		// Retrieve strati sizes
		Map<Object, Integer> classStratiSizes = new HashMap<>(classStrati.size());
		for (Object c : classStrati.keySet()) {
			classStratiSizes.put(c, classStrati.get(c).size());
		}

		// First assign one item of each class to train and test
		for (Object c : classStrati.keySet()) {
			IDataset<IInstance> availableInstances = classStrati.get(c);
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
			IDataset<IInstance> availableInstances = classStrati.get(c);
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