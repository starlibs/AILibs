package jaicore.ml.extendedtree;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;

import org.junit.Test;

import jaicore.ml.core.FeatureSpace;
import jaicore.ml.intervaltree.ExtendedRandomForest;
import jaicore.ml.intervaltree.ExtendedRandomTree;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;

public class ExtendedRandomForestTest {

	private static String testFile = "resources/regression_data/cpu.small.arff";

	@Test
	public void testVarianceDecompose() {
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
			for (int i = 0; i < forest.getFeatureSpace().getDimensionality() - 1; i++) {
				HashSet<Integer> subset = new HashSet<Integer>();
				subset.add(i);
				double curImp = forest.computeMarginalForFeatureSubset(subset);
				System.out.println("importance of feature " + i + ": " + curImp);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
