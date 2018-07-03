package jaicore.ml.extendedtree;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

import jaicore.ml.core.FeatureSpace;
import jaicore.ml.intervaltree.ExtendedRandomForest;
import jaicore.ml.intervaltree.ExtendedRandomTree;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;

public class ExtendedRandomForestTest {

	private static String testFile = "resources/regression_data/cpu.small.arff";

	@Test
	public void testTrain() {
		try (BufferedReader reader = Files.newBufferedReader(Paths.get(testFile), StandardCharsets.UTF_8)) {
			ArffReader arffReader = new ArffReader(reader);
			Instances data = arffReader.getData();
			data.setClassIndex(data.numAttributes() - 1);

			ExtendedRandomForest forest = new ExtendedRandomForest(new FeatureSpace(data));
			// forest.setFeatureSpace(new FeatureSpace(data));
			forest.setNumIterations(20);
			forest.buildClassifier(data);
			System.out.println("size of forest: " + forest.getSize());
			forest.prepareForest();
			double sum = 0;
			for (int i = 0; i < forest.getFeatureSpace().getDimensionality() - 1; i++) {
				double curImp = forest.computeMarginalForSingleFeature(i);
				System.out.println("importance of feature " + i + ": " + curImp);
				sum += curImp;
			}
			System.out.println("Sum of importance values: " + sum);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
