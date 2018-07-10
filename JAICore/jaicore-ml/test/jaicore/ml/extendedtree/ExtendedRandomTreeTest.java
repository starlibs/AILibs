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
	private static String testFile = "resources/regression_data/cpu.small.arff";
	
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
			HashSet<Integer> subset = new HashSet<Integer>();
			subset.add(2);
			System.out.println("single new: " + 2 + ": " + tree.computeMarginalForSubsetOfFeatures(subset));
			subset.add(3);
			System.out.println("single new: " + 2 + ": " + tree.computeMarginalForSubsetOfFeatures(subset));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
