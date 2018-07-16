package jaicore.ml.extendedtree;

import static org.junit.Assert.assertNotNull;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;

import org.junit.Test;

import jaicore.ml.core.FeatureSpace;
import jaicore.ml.intervaltree.ExtendedRandomTree;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;

public class ExtendedRandomTreeTest {
	private static String testFile = "resources/regression_data/cpu_verysmall.arff";
	
	@Test
	public void testTrain(){
		try (BufferedReader reader = Files.newBufferedReader(Paths.get(testFile), StandardCharsets.UTF_8)) {
			ArffReader arffReader = new ArffReader(reader);
			Instances data = arffReader.getData();
			data.setClassIndex(data.numAttributes() - 1);

			
			ExtendedRandomTree tree = new ExtendedRandomTree();
			tree.setFeatureSpace(new FeatureSpace(data));
			tree.buildClassifier(data);
			tree.preprocess();
			double sum = 0.0d;
			for(int i = 0; i < tree.getFeatureSpace().getDimensionality(); i++) {
			HashSet<Integer> subset = new HashSet<Integer>();
			subset.add(i);
			double value = tree.computeMarginalForSubsetOfFeatures(subset);
			System.out.println("single new: " + i + ": " + value);
			}
			HashSet<Integer> subset = new HashSet<Integer>();
			subset.add(0);
			subset.add(1);
			double value = tree.computeMarginalForSubsetOfFeatures(subset);
			System.out.println("double new {0,1}: " + ": " + value);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
