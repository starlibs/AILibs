package jaicore.ml.skikitwrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;

import org.junit.Test;

import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;

public class SkikitLearn_Wrapper_Test {
	@Test
	public void buildClassifier() throws Exception {
		SkikitLearnWrapper slw = new SkikitLearnWrapper("sklearn/neural_network/MLPRegressor", "", "");
		Instances dataset = loadARFF("testsrc/ml/skikitwrapper/0532052678.arff");
		slw.buildClassifier(dataset);
		assertNotEquals(slw.getModelPath(), "");
	}

	@Test
	public void buildAndTestClassifier() throws Exception {
		String test_arff = "testsrc/ml/skikitwrapper/Bayesnet_Train.arff";
		SkikitLearnWrapper slw = new SkikitLearnWrapper("sklearn/neural_network/MLPRegressor", "", "");
		Instances datasetTrain = loadARFF(test_arff);
		Instances datasetTest = loadARFF(test_arff);
		int numberInstance = datasetTest.numInstances();
		slw.buildClassifier(datasetTrain);
		double[] result = slw.classifyInstances(datasetTest);
		assertNotNull(result);
		assertEquals(numberInstance, result.length);
	}

	@Test
	public void testClassifier() throws Exception {
		String test_arff = "testsrc/ml/skikitwrapper/Bayesnet_Train.arff";
		SkikitLearnWrapper slw = new SkikitLearnWrapper("sklearn/neural_network/MLPRegressor", "", "");
		Instances datasetTest = loadARFF(test_arff);
		int numberInstance = datasetTest.numInstances();
		slw.setModelPath(Paths.get("testsrc/ml/skikitwrapper/0532052678_MLPRegressor.pcl").toAbsolutePath().toString());
		double[] result = slw.classifyInstances(datasetTest);
		assertNotNull(result);
		assertEquals(numberInstance, result.length);
	}
	
	@Test
	public void buildClassifierWithConstructorParams() throws Exception {
		SkikitLearnWrapper slw = new SkikitLearnWrapper("sklearn/neural_network/MLPRegressor", "", "activation='logistic'");
		Instances dataset = loadARFF("testsrc/ml/skikitwrapper/0532052678.arff");
		slw.buildClassifier(dataset);
		assertNotEquals(slw.getModelPath(), "");
	}
	
	@Test
	public void loadModule() throws Exception {
		SkikitLearnWrapper slw = new SkikitLearnWrapper("sklearn/neural_network/MLPRegressor",
				"from sklearn.linear_model import LinearRegression", "");
		Instances dataset = loadARFF("testsrc/ml/skikitwrapper/Bayesnet_Train.arff");
		slw.buildClassifier(dataset);
	}

	private Instances loadARFF(String arffPath) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(arffPath));
		ArffReader arff = new ArffReader(reader);
		Instances data = arff.getData();
		data.setClassIndex(data.numAttributes() - 1);
		return data;
	}
}
