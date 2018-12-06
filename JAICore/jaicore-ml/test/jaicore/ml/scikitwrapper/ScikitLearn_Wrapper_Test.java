package jaicore.ml.scikitwrapper;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Test;

import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;

public class ScikitLearn_Wrapper_Test {

	@Test
	public void buildClassifier() throws Exception {
		ScikitLearnWrapper slw = new ScikitLearnWrapper("LinearRegression()",
				"from sklearn.linear_model import LinearRegression");
		Instances dataset = loadARFF("testsrc/ml/skikitwrapper/0532052678.arff");
		slw.setIsRegression(true);
		slw.buildClassifier(dataset);
		assertThat(slw.getModelPath(), not(equalTo("")));
	}

	@Test
	public void buildClassifier2() throws Exception {
		ScikitLearnWrapper slw = new ScikitLearnWrapper("MLPRegressor()",
				"from sklearn.neural_network import MLPRegressor");
		Instances dataset = loadARFF("testsrc/ml/skikitwrapper/0532052678.arff");
		slw.setIsRegression(true);
		int s = dataset.numAttributes();
		slw.setTargets(s - 1, s - 2, s - 3);
		slw.buildClassifier(dataset);
		assertThat(slw.getModelPath(), not(equalTo("")));
	}

	@Test
	public void buildAndTestClassifier() throws Exception {
		String test_arff = "testsrc/ml/skikitwrapper/Bayesnet_Train.arff";
		ScikitLearnWrapper slw = new ScikitLearnWrapper("MLPRegressor()",
				"from sklearn.neural_network import MLPRegressor");
		Instances datasetTrain = loadARFF(test_arff);
		Instances datasetTest = loadARFF(test_arff);
		int numberInstance = datasetTest.numInstances();
		slw.setIsRegression(true);
		int s = datasetTrain.numAttributes();
		slw.setTargets(s - 1, s - 2, s - 3);
		slw.buildClassifier(datasetTrain);
		double[] result = slw.classifyInstances(datasetTest);
		assertNotNull(result);
		assertThat(numberInstance * 3, equalTo(result.length));
	}

	@Test
	public void testClassifier() throws Exception {
		String test_arff = "testsrc/ml/skikitwrapper/Bayesnet_Train.arff";
		ScikitLearnWrapper slw = new ScikitLearnWrapper("MLPRegressor()",
				"from sklearn.neural_network import MLPRegressor");
		Instances datasetTest = loadARFF(test_arff);
		int numberInstance = datasetTest.numInstances();
		slw.setModelPath(
				Paths.get("testsrc/ml/skikitwrapper/01673183575_MLPRegressor.pcl").toAbsolutePath().toString());
		slw.setIsRegression(true);
		double[] result = slw.classifyInstances(datasetTest);
		assertNotNull(result);
		assertThat(numberInstance, equalTo(result.length));
	}

	@Test
	public void buildClassifierWithConstructorParams() throws Exception {
		ScikitLearnWrapper slw = new ScikitLearnWrapper("MLPRegressor(activation='logistic')",
				"from sklearn.neural_network import MLPRegressor");
		Instances dataset = loadARFF("testsrc/ml/skikitwrapper/0532052678.arff");
		slw.setIsRegression(true);
		slw.buildClassifier(dataset);
		assertThat(slw.getModelPath(), not(equalTo("")));
	}

	@Test
	public void trainPipeline() throws Exception {
		List<String> imports = Arrays.asList("sklearn", "sklearn.pipeline", "sklearn.decomposition",
				"sklearn.ensemble");
		String constructInstruction = "sklearn.pipeline.make_pipeline(sklearn.pipeline.make_union(sklearn.decomposition.PCA(),sklearn.decomposition.FastICA()),sklearn.ensemble.RandomForestClassifier(n_estimators=100))";
		ScikitLearnWrapper slw = new ScikitLearnWrapper(constructInstruction,
				ScikitLearnWrapper.getImportString(imports));
		Instances dataset = loadARFF("testsrc/ml/skikitwrapper/dataset_31_credit-g.arff");
		slw.buildClassifier(dataset);
		assertThat(slw.getModelPath(), not(equalTo("")));
	}

	@Test
	public void createImportStatementFromImportFolderInvalid() throws IOException {
		assertThat("", equalTo(ScikitLearnWrapper.createImportStatementFromImportFolder(null, true)));
		assertThat("", equalTo(ScikitLearnWrapper.createImportStatementFromImportFolder(
				new File("testsrc/ml/skikitwrapper/not_existing_folder"), true)));
		assertThat("", equalTo(ScikitLearnWrapper
				.createImportStatementFromImportFolder(new File("testsrc/ml/skikitwrapper/importfolder_empty"), true)));
	}

	@Test
	public void createImportStatementFromImportFolderValid() throws IOException {
		File importfolder = new File("testsrc/ml/skikitwrapper/importfolder_test");
		File initFile = new File(importfolder, "__init__.py");
		if (initFile.exists()) {
			initFile.delete();
		}
		try {
			String importStatement = ScikitLearnWrapper.createImportStatementFromImportFolder(importfolder, true);
			assertTrue(importStatement.contains("sys.path.append('" + importfolder.getAbsolutePath() + "')\n"));
			assertTrue(importStatement.contains("\nimport " + importfolder.getName()));
			assertTrue(initFile.exists());
		} finally {
			if (initFile.exists()) {
				initFile.delete();
			}
		}
	}

	@Test
	public void loadOwnClassifierFromFileWithNamespace() throws Exception {
		File importfolder = new File("testsrc/ml/skikitwrapper/importfolder_test");
		String importStatement = ScikitLearnWrapper.createImportStatementFromImportFolder(importfolder, true);
		ScikitLearnWrapper slw = new ScikitLearnWrapper("test_module_1.My_MLPRegressor()", importStatement);
		Instances dataset = loadARFF("testsrc/ml/skikitwrapper/0532052678.arff");
		slw.setIsVerbose(true);
		slw.setIsRegression(true);
		int s = dataset.numAttributes();
		slw.setTargets(s - 1, s - 2, s - 3);
		slw.buildClassifier(dataset);
	}
	
	@Test
	public void loadOwnClassifierFromFileWithoutNamespace() throws Exception {
		File importfolder = new File("testsrc/ml/skikitwrapper/importfolder_test");
		String importStatement = ScikitLearnWrapper.createImportStatementFromImportFolder(importfolder, false);
		ScikitLearnWrapper slw = new ScikitLearnWrapper("My_MLPRegressor()", importStatement);
		Instances dataset = loadARFF("testsrc/ml/skikitwrapper/0532052678.arff");
		slw.setIsVerbose(true);
		slw.setIsRegression(true);
		int s = dataset.numAttributes();
		slw.setTargets(s - 1, s - 2, s - 3);
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
