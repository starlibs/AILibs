package jaicore.ml.extendedtree;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

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
			tree.buildClassifier(data);
			
			tree.toString();
			tree.get();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
