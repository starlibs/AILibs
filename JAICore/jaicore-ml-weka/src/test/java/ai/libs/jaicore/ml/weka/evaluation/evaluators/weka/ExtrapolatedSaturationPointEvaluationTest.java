package ai.libs.jaicore.ml.weka.evaluation.evaluators.weka;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.api4.java.ai.ml.core.exception.DatasetCreationException;
import org.junit.Assert;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataSetDescription;

import ai.libs.jaicore.ml.classification.loss.dataset.EClassificationPerformanceMeasure;
import ai.libs.jaicore.ml.core.evaluation.evaluator.ExtrapolatedSaturationPointEvaluator;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.SystematicSamplingFactory;
import ai.libs.jaicore.ml.functionprediction.learner.learningcurveextrapolation.ipl.InversePowerLawExtrapolationMethod;
import ai.libs.jaicore.ml.weka.classification.learner.WekaClassifier;
import ai.libs.jaicore.ml.weka.dataset.IWekaInstance;
import ai.libs.jaicore.ml.weka.dataset.IWekaInstances;
import ai.libs.jaicore.ml.weka.dataset.WekaInstances;
import weka.classifiers.functions.SMO;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class ExtrapolatedSaturationPointEvaluationTest {

	private IWekaInstances train;
	private IWekaInstances test;

	@Disabled("We currently do not support learning curve based stuff")
	@Test
	public void testClassifierEvaluationAtSaturationPoint() throws Exception {
		// Load dataset from OpenML and create stratified split
		Instances dataset = null;
		OpenmlConnector client = new OpenmlConnector();
		DataSetDescription description = client.dataGet(42);
		File file = client.datasetGet(description);
		DataSource source = new DataSource(file.getCanonicalPath());
		dataset = source.getDataSet();
		dataset.setClassIndex(dataset.numAttributes() - 1);
		Attribute targetAttribute = dataset.attribute(description.getDefault_target_attribute());
		dataset.setClassIndex(targetAttribute.index());
		this.createSplit(new WekaInstances(dataset), 0.8, 123l);

		// Test classifier evaluation at saturation point
		ExtrapolatedSaturationPointEvaluator evaluator = new ExtrapolatedSaturationPointEvaluator(new int[] { 8, 16, 64, 128 }, new SystematicSamplingFactory<>(), this.train, 0.7, new InversePowerLawExtrapolationMethod(), 123l, this.test,
				EClassificationPerformanceMeasure.ERRORRATE);
		evaluator.setEpsilon(0.0005d);
		double evaluationResult = evaluator.evaluate(new WekaClassifier(new SMO()));
		Assert.assertTrue(evaluationResult > 0 && evaluationResult <= 100);
	}

	private void createSplit(final IWekaInstances dataset, final double trainsplit, final long seed) throws DatasetCreationException, InterruptedException {
		this.train = dataset.createEmptyCopy();
		this.test = dataset.createEmptyCopy();
		IWekaInstances data = dataset.createEmptyCopy();
		data.addAll(dataset);

		// Shuffle the data
		Random random = new Random(seed);
		Collections.shuffle(data, random);

		// Stratify the data by class
		Map<Object, IWekaInstances> classStrati = new HashMap<>();
		for (IWekaInstance d : dataset) {
			Object c = d.getLabel();
			if (!classStrati.containsKey(c)) {
				classStrati.put(c, dataset.createEmptyCopy());
			}
			classStrati.get(c).add(d);
		}
		;

		// Retrieve strati sizes
		Map<Object, Integer> classStratiSizes = new HashMap<>(classStrati.size());
		for (Object c : classStrati.keySet()) {
			classStratiSizes.put(c, classStrati.get(c).size());
		}

		// First assign one item of each class to train and test
		for (Object c : classStrati.keySet()) {
			IWekaInstances availableInstances = classStrati.get(c);
			if (!availableInstances.isEmpty()) {
				this.train.add(availableInstances.get(0));
				availableInstances.remove(0);
			}
			if (!availableInstances.isEmpty()) {
				this.test.add(availableInstances.get(0));
				availableInstances.remove(0);
			}
		}

		// Distribute remaining instances over train test
		for (Object c : classStrati.keySet()) {
			IWekaInstances availableInstances = classStrati.get(c);
			int trainItems = (int) Math.min(availableInstances.size(), Math.ceil(trainsplit * classStratiSizes.get(c)));
			for (int j = 0; j < trainItems; j++) {
				this.train.add(availableInstances.get(0));
				availableInstances.remove(0);
			}
			int testItems = (int) Math.min(availableInstances.size(), Math.ceil((1 - trainsplit) * classStratiSizes.get(c)));
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
