package ai.libs.jaicore.ml.weka.extendedtree.synthetic;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.math3.geometry.euclidean.oned.Interval;
import org.junit.Before;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ai.libs.jaicore.ml.weka.rangequery.learner.intervaltree.ExtendedRandomTree;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;

public class ExtendedRandomTreeTest {

	private ExtendedRandomTree classifier;

	private static String trainFile = "resources/regression_data/cpu.small.arff_RQPtrain.arff";

	private static String testFile = "resources/regression_data/cpu.small.arff_RQPtest.arff";

	@Before
	public void testTrain() throws Exception {
		try (BufferedReader reader = Files.newBufferedReader(Paths.get(trainFile), StandardCharsets.UTF_8)) {
			ArffReader arffReader = new ArffReader(reader);
			Instances data = arffReader.getData();
			data.setClassIndex(data.numAttributes() - 1);

			this.classifier = new ExtendedRandomTree();
			this.classifier.buildClassifier(data);

		}
	}

	/**
	 * Test the classifier without any cross-validation
	 * @throws IOException
	 */
	@Disabled
	@Test
	public void testPredict() throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(Paths.get(testFile), StandardCharsets.UTF_8)) {
			ArffReader arffReader = new ArffReader(reader);
			Instances data = arffReader.getData();
			for (Instance instance : data) {
				// construct the real interval
				double lower = instance.value(data.numAttributes() - 1);
				double upper = instance.value(data.numAttributes() - 2);
				Instance strippedInstance = new DenseInstance(data.numAttributes() - 2);
				for (int i = 0; i < data.numAttributes() - 2; i++) {
					strippedInstance.setValue(i, instance.value(i));
				}
				Interval actualInterval = new Interval(upper, lower);
				Interval predictedInterval = this.classifier.predictInterval(strippedInstance);
				System.out.println("Actual interval: " + actualInterval + ", predicted Interval " + predictedInterval);
			}

		}
	}
}
