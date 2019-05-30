package jaicore.ml.extendedtree.synthetic;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.math3.geometry.euclidean.oned.Interval;
import org.junit.Before;
import org.junit.Test;

import jaicore.ml.intervaltree.ExtendedRandomTree;
import junit.framework.Assert;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;

public class ExtendedRandomTreeTest {

	private ExtendedRandomTree classifier;

	private static String trainFile = "resources/regression_data/cpu.small.arff_RQPtrain.arff";

	private static String testFile = "resources/regression_data/cpu.small.arff_RQPtest.arff";

	@Before
	public void testTrain() {
		try (BufferedReader reader = Files.newBufferedReader(Paths.get(trainFile), StandardCharsets.UTF_8)) {
			ArffReader arffReader = new ArffReader(reader);
			Instances data = arffReader.getData();
			data.setClassIndex(data.numAttributes() - 1);

			this.classifier = new ExtendedRandomTree();
			this.classifier.buildClassifier(data);

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	/**
	 * Test the classifier without any cross-validation
	 */
	@Test
	public void testPredict() {
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

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
}
