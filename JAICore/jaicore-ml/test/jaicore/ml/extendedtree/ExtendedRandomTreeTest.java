package jaicore.ml.extendedtree;

import static org.junit.Assert.assertNotNull;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Sets;

import jaicore.ml.core.FeatureSpace;
import jaicore.ml.intervaltree.ExtendedRandomTree;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;

public class ExtendedRandomTreeTest {
	private static String testFile = "resources/regression_data/cloud.arff";
	
	@Test
	public void testTrain(){
		try (BufferedReader reader = Files.newBufferedReader(Paths.get(testFile), StandardCharsets.UTF_8)) {
			ArffReader arffReader = new ArffReader(reader);
			Instances data = arffReader.getData();
			data.setClassIndex(data.numAttributes() - 1);

			
			ExtendedRandomTree tree = new ExtendedRandomTree();
			tree.setMinNum(5);
			tree.setFeatureSpace(new FeatureSpace(data));
			tree.buildClassifier(data);
			tree.preprocess();
			double sum = 0.0d;
			HashSet<Integer> allFeatures = new HashSet<Integer>();
			for(int i = 0; i < tree.getFeatureSpace().getDimensionality(); i++) {
				allFeatures.add(i);
			}
			Set<Set<Integer>> powerset = Sets.powerSet(allFeatures);
			for(Set<Integer> features : powerset) {
				if(features.size() > 0) {
					double cont = tree.computeMarginalForSubsetOfFeatures(features);
					System.out.println("Variance contribution of " + features.toString() + ": " + cont);
					sum += cont;
				}
			}
			System.out.println("sum of contributions = " + sum);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
