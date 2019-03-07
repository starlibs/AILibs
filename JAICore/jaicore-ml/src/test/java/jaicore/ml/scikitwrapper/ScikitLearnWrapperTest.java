package jaicore.ml.scikitwrapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import jaicore.ml.scikitwrapper.ScikitLearnWrapper.ProblemType;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;

public class ScikitLearnWrapperTest {

	private static final String BASE_TESTRSC_PATH = "testrsc/ml/scikitwrapper/";
	private static final String REGRESSION_ARFF = BASE_TESTRSC_PATH + "0532052678.arff";
	private static final String CLASSIFICATION_ARFF = BASE_TESTRSC_PATH + "dataset_31_credit-g.arff";
	private static final String BAYESNET_TRAIN_ARFF = BASE_TESTRSC_PATH + "Bayesnet_Train.arff";
	private static final String MLP_REGRESSOR_DUMP = BASE_TESTRSC_PATH + "01673183575_MLPRegressor.pcl";
	private static final String CLASSIFIER_DUMP = BASE_TESTRSC_PATH + "02055055033_Pipeline.pcl";
	private static final String OWN_CLASSIFIER_DUMP = BASE_TESTRSC_PATH + "0532052678.arff";
	private static final String IMPORT_FOLDER = BASE_TESTRSC_PATH + "importfolder_test";

	@Test
	public void buildClassifierRegression() throws Exception {
		ScikitLearnWrapper slw = new ScikitLearnWrapper("LinearRegression()", "from sklearn.linear_model import LinearRegression");
		Instances dataset = this.loadARFF(REGRESSION_ARFF);
		slw.setProblemType(ProblemType.REGRESSION);
		slw.buildClassifier(dataset);
		assertNotEquals(null, slw.getModelPath());
		assertTrue(slw.getModelPath().exists());
	}

	@Test
	public void buildClassifierRegressionMultitarget() throws Exception {
		ScikitLearnWrapper slw = new ScikitLearnWrapper("MLPRegressor(activation='logistic')", "from sklearn.neural_network import MLPRegressor");
		Instances dataset = this.loadARFF(REGRESSION_ARFF);
		slw.setProblemType(ProblemType.REGRESSION);
		int s = dataset.numAttributes();
		slw.setTargets(s - 1, s - 2, s - 3);
		slw.buildClassifier(dataset);
		assertNotEquals(null, slw.getModelPath());
		assertTrue(slw.getModelPath().exists());
	}

	@Test
	public void trainAndTestClassifierRegressionMultitarget() throws Exception {
		ScikitLearnWrapper slw = new ScikitLearnWrapper("MLPRegressor()", "from sklearn.neural_network import MLPRegressor");
		Instances datasetTrain = this.loadARFF(BAYESNET_TRAIN_ARFF);
		Instances datasetTest = datasetTrain;
		slw.setProblemType(ProblemType.REGRESSION);
		int s = datasetTrain.numAttributes();
		slw.setTargets(s - 1, s - 2, s - 3);
		slw.buildClassifier(datasetTrain);
		double[] result = slw.classifyInstances(datasetTest);
		assertEquals("Unequal length of predictions and number of test instances", result.length, datasetTest.size());
	}

	@Test
	public void testClassifierRegression() throws Exception {
		ScikitLearnWrapper slw = new ScikitLearnWrapper("MLPRegressor()", "from sklearn.neural_network import MLPRegressor");
		Instances datasetTest = this.loadARFF(BAYESNET_TRAIN_ARFF);
		slw.setModelPath(new File(MLP_REGRESSOR_DUMP).getAbsoluteFile());
		slw.setProblemType(ProblemType.REGRESSION);
		double[] result = slw.classifyInstances(datasetTest);
		assertEquals("Unequal length of predictions and number of test instances", result.length, datasetTest.size());
	}

	@Test
	public void trainClassifierCategorical() throws Exception {
		List<String> imports = Arrays.asList("sklearn", "sklearn.pipeline", "sklearn.decomposition", "sklearn.ensemble");
		String constructInstruction = "sklearn.pipeline.make_pipeline(sklearn.pipeline.make_union(sklearn.decomposition.PCA(),sklearn.decomposition.FastICA()),sklearn.ensemble.RandomForestClassifier(n_estimators=100))";
		ScikitLearnWrapper slw = new ScikitLearnWrapper(constructInstruction, ScikitLearnWrapper.getImportString(imports));
		Instances dataset = this.loadARFF(CLASSIFICATION_ARFF);
		slw.buildClassifier(dataset);
		assertNotEquals(null, slw.getModelPath());
		assertTrue(slw.getModelPath().exists());
	}

	@Test
	public void trainAndTestClassifierCategorical() throws Exception {
		List<String> imports = Arrays.asList("sklearn", "sklearn.pipeline", "sklearn.decomposition", "sklearn.ensemble");
		String constructInstruction = "sklearn.pipeline.make_pipeline(sklearn.pipeline.make_union(sklearn.decomposition.PCA(),sklearn.decomposition.FastICA()),sklearn.ensemble.RandomForestClassifier(n_estimators=100))";
		ScikitLearnWrapper slw = new ScikitLearnWrapper(constructInstruction, ScikitLearnWrapper.getImportString(imports));
		Instances datasetTrain = this.loadARFF(CLASSIFICATION_ARFF);
		Instances datasetTest = datasetTrain;
		slw.buildClassifier(datasetTrain);
		double[] result = slw.classifyInstances(datasetTest);
		assertEquals("Unequal length of predictions and number of test instances", result.length, datasetTest.size());
	}

	@Test
	public void testClassifierCategorical() throws Exception {
		List<String> imports = Arrays.asList("sklearn", "sklearn.pipeline", "sklearn.decomposition", "sklearn.ensemble");
		String constructInstruction = "sklearn.pipeline.make_pipeline(sklearn.pipeline.make_union(sklearn.decomposition.PCA(),sklearn.decomposition.FastICA()),sklearn.ensemble.RandomForestClassifier(n_estimators=100))";
		ScikitLearnWrapper slw = new ScikitLearnWrapper(constructInstruction, ScikitLearnWrapper.getImportString(imports));
		Instances datasetTest = this.loadARFF(CLASSIFICATION_ARFF);
		slw.setModelPath(new File(CLASSIFIER_DUMP).getAbsoluteFile());
		double[] result = slw.classifyInstances(datasetTest);
		assertEquals("Unequal length of predictions and number of test instances", result.length, datasetTest.size());
	}

	@Test
	public void getRawOutput() throws Exception {
		ScikitLearnWrapper slw = new ScikitLearnWrapper("MLPRegressor()", "from sklearn.neural_network import MLPRegressor");
		Instances datasetTrain = this.loadARFF(BAYESNET_TRAIN_ARFF);
		Instances datasetTest = this.loadARFF(BAYESNET_TRAIN_ARFF);
		slw.setProblemType(ProblemType.REGRESSION);
		int s = datasetTrain.numAttributes();
		slw.setTargets(s - 1, s - 2, s - 3);
		slw.buildClassifier(datasetTrain);
		slw.classifyInstances(datasetTest);
		assertNotEquals(null, slw.getModelPath());
		assertTrue(slw.getModelPath().exists());
	}

	@Test
	public void loadOwnClassifierFromFileWithNamespace() throws Exception {
		File importfolder = new File(IMPORT_FOLDER);
		String importStatement = ScikitLearnWrapper.createImportStatementFromImportFolder(importfolder, true);
		ScikitLearnWrapper slw = new ScikitLearnWrapper("test_module_1.My_MLPRegressor()", importStatement);
		Instances dataset = this.loadARFF(CLASSIFIER_DUMP);
		slw.setProblemType(ProblemType.REGRESSION);
		int s = dataset.numAttributes();
		slw.setTargets(s - 1, s - 2, s - 3);
		slw.buildClassifier(dataset);
		assertNotEquals(null, slw.getModelPath());
		assertTrue(slw.getModelPath().exists());
	}

	@Test
	public void loadOwnClassifierFromFileWithoutNamespace() throws Exception {
		File importfolder = new File(IMPORT_FOLDER);
		String importStatement = ScikitLearnWrapper.createImportStatementFromImportFolder(importfolder, false);
		ScikitLearnWrapper slw = new ScikitLearnWrapper("My_MLPRegressor()", importStatement);
		Instances dataset = this.loadARFF(OWN_CLASSIFIER_DUMP);
		slw.setProblemType(ProblemType.REGRESSION);
		int s = dataset.numAttributes();
		slw.setTargets(s - 1, s - 2, s - 3);
		slw.buildClassifier(dataset);
		assertNotEquals(null, slw.getModelPath());
		assertTrue(slw.getModelPath().exists());
	}

	@Test
	public void invalidConstructorNoConstructionCall() throws IOException {
		new ScikitLearnWrapper(null, "");
		assertTrue(true);
	}

	@Test
	public void invalidConstructorEmptyConstructionCall() throws IOException {
		new ScikitLearnWrapper("", "");
		assertTrue(true);
	}

	private Instances loadARFF(final String arffPath) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(arffPath));
		ArffReader arff = new ArffReader(reader);
		Instances data = arff.getData();
		data.setClassIndex(data.numAttributes() - 1);
		return data;
	}
}
