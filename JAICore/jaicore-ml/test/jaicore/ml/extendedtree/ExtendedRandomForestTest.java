package jaicore.ml.extendedtree;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Sets;

import jaicore.ml.core.FeatureSpace;
import jaicore.ml.intervaltree.ExtendedRandomForest;
import jaicore.ml.intervaltree.ExtendedRandomTree;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;

public class ExtendedRandomForestTest {

	// private static String testFile = "resources/regression_data/cloud.arff";
	// private static String testFile = "resources/regression_data/cpu-medium.arff";
	// private static String testFile = "resources/regression_data/cloud.arff";
	private static String testFile = "resources/regression_data/performance.arff";

	@Test
	public void testVarianceDecompose() {

		try (BufferedReader reader = Files.newBufferedReader(Paths.get(testFile), StandardCharsets.UTF_8)) {
			ArffReader arffReader = new ArffReader(reader);
			Instances data = arffReader.getData();
			data.setClassIndex(data.numAttributes() - 1);
			ExtendedRandomForest forest = new ExtendedRandomForest(1.0d, 32, new FeatureSpace(data));
			// forest.setFeatureSpace(new FeatureSpace(data));
			// forest.setNumIterations(16);
			forest.buildClassifier(data);
			forest.prepareForest(data);
			System.out.println("size of forest: " + forest.getSize());
			double sum = 0.0d;
			HashSet<Integer> allFeatures = new HashSet<Integer>();
			for (int i = 0; i < forest.getFeatureSpace().getDimensionality(); i++) {
				allFeatures.add(i);
			}
			HashMap<Set<Integer>, Double> contributions = new HashMap<Set<Integer>, Double>();
			Set<Set<Integer>> powerset = Sets.powerSet(allFeatures);
			System.out.println("size: " + allFeatures.size());
			for (Set<Integer> features : powerset) {
				if (features.size() > 0) {
					double cont = forest.computeMarginalStandardDeviationFeatureSubset(features);
					System.out.println("Individual Standard Deviation of " + features.toString() + ": " + cont);
					contributions.put(features, cont);
					assertTrue(cont >= 0.0d);
					sum += cont;
				}
			}
//			double max = contributions.values().stream().mapToDouble(v -> v).max()
//					.orElseThrow(NoSuchElementException::new);
//			for (Set<Integer> key : contributions.keySet()) {
//				double temp = contributions.get(key);
//				temp /= max;
//				contributions.put(key, temp);
//			}
//			for (double value : contributions.values())
//				System.out.println(value);

			 System.out.println("sum of contributions = " + sum);
			// assertEquals(sum, 1.0d, 0.0001);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
