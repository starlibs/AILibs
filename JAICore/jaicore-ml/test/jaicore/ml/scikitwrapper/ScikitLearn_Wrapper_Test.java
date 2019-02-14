package jaicore.ml.scikitwrapper;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;

public class ScikitLearn_Wrapper_Test {

	@Test
	public void buildClassifierRegression() throws Exception {
		ScikitLearnWrapper slw = new ScikitLearnWrapper("LinearRegression()",
				"from sklearn.linear_model import LinearRegression");
		Instances dataset = loadARFF("testsrc/ml/scikitwrapper/0532052678.arff");
		slw.setIsRegression(true);
		slw.buildClassifier(dataset);
		assertThat(slw.getModelPath(), not(equalTo("")));
	}

	@Test
	public void buildClassifierRegressionMultitarget() throws Exception {
		ScikitLearnWrapper slw = new ScikitLearnWrapper("MLPRegressor(activation='logistic')",
				"from sklearn.neural_network import MLPRegressor");
		Instances dataset = loadARFF("testsrc/ml/scikitwrapper/0532052678.arff");
		slw.setIsRegression(true);
		int s = dataset.numAttributes();
		slw.setTargets(s - 3, s - 2, s - 1);
		slw.buildClassifier(dataset);
		assertThat(slw.getModelPath(), not(equalTo("")));
	}

	@Test
	public void trainAndTestClassifierRegressionMultitarget() throws Exception {
		String test_arff = "testsrc/ml/scikitwrapper/Bayesnet_Train.arff";
		ScikitLearnWrapper slw = new ScikitLearnWrapper("MLPRegressor()",
				"from sklearn.neural_network import MLPRegressor");
		Instances datasetTrain = loadARFF(test_arff);
		Instances datasetTest = datasetTrain;
		int numberInstance = datasetTest.numInstances();
		slw.setIsRegression(true);
		int s = datasetTrain.numAttributes();
		slw.setTargets(s - 3, s - 2, s - 1);
		slw.buildClassifier(datasetTrain);
		double[] result = slw.classifyInstances(datasetTest);
		assertNotNull(result);
		assertThat(numberInstance * 3, equalTo(result.length));
	}

	@Test
	public void testClassifierRegression() throws Exception {
		String test_arff = "testsrc/ml/scikitwrapper/Bayesnet_Train.arff";
		ScikitLearnWrapper slw = new ScikitLearnWrapper("MLPRegressor()",
				"from sklearn.neural_network import MLPRegressor");
		Instances datasetTest = loadARFF(test_arff);
		int numberInstance = datasetTest.numInstances();
		slw.setModelPath(
				Paths.get("testsrc/ml/scikitwrapper/01673183575_MLPRegressor.pcl").toAbsolutePath().toString());
		slw.setIsRegression(true);
		double[] result = slw.classifyInstances(datasetTest);
		assertNotNull(result);
		assertThat(numberInstance, equalTo(result.length));
	}

	@Test
	public void trainClassifierCategorical() throws Exception {
		List<String> imports = Arrays.asList("sklearn", "sklearn.pipeline", "sklearn.decomposition",
				"sklearn.ensemble");
		String constructInstruction = "sklearn.pipeline.make_pipeline(sklearn.pipeline.make_union(sklearn.decomposition.PCA(),sklearn.decomposition.FastICA()),sklearn.ensemble.RandomForestClassifier(n_estimators=100))";
		ScikitLearnWrapper slw = new ScikitLearnWrapper(constructInstruction,
				ScikitLearnWrapper.getImportString(imports));
		Instances dataset = loadARFF("testsrc/ml/scikitwrapper/dataset_31_credit-g.arff");
		slw.buildClassifier(dataset);
		assertThat(slw.getModelPath(), not(equalTo("")));
	}

	@Test
	public void trainAndTestClassifierCategorical() throws Exception {
		List<String> imports = Arrays.asList("sklearn", "sklearn.pipeline", "sklearn.decomposition",
				"sklearn.ensemble");
		String constructInstruction = "sklearn.pipeline.make_pipeline(sklearn.pipeline.make_union(sklearn.decomposition.PCA(),sklearn.decomposition.FastICA()),sklearn.ensemble.RandomForestClassifier(n_estimators=100))";
		ScikitLearnWrapper slw = new ScikitLearnWrapper(constructInstruction,
				ScikitLearnWrapper.getImportString(imports));
		Instances datasetTrain = loadARFF("testsrc/ml/scikitwrapper/dataset_31_credit-g.arff");
		Instances datasetTest = datasetTrain;
		slw.buildClassifier(datasetTrain);
		double[] result = slw.classifyInstances(datasetTest);
		assertThat(slw.getModelPath(), not(equalTo("")));
		assertThat(datasetTest.numInstances(), equalTo(result.length));
	}
	
	@Test
	public void testClassifierCategorical() throws Exception {
		List<String> imports = Arrays.asList("sklearn", "sklearn.pipeline", "sklearn.decomposition",
				"sklearn.ensemble");
		String test_arff = "testsrc/ml/scikitwrapper/dataset_31_credit-g.arff";
		String constructInstruction = "sklearn.pipeline.make_pipeline(sklearn.pipeline.make_union(sklearn.decomposition.PCA(),sklearn.decomposition.FastICA()),sklearn.ensemble.RandomForestClassifier(n_estimators=100))";
		ScikitLearnWrapper slw = new ScikitLearnWrapper(constructInstruction,
				ScikitLearnWrapper.getImportString(imports));
		Instances datasetTest = loadARFF(test_arff);
		int numberInstance = datasetTest.numInstances();
		slw.setModelPath(
				Paths.get("testsrc/ml/scikitwrapper/02055055033_Pipeline.pcl").toAbsolutePath().toString());
		double[] result = slw.classifyInstances(datasetTest);
		assertNotNull(result);
		assertThat(numberInstance, equalTo(result.length));
	}

	@Test
	public void getRawOutput() throws Exception {
		String test_arff = "testsrc/ml/scikitwrapper/Bayesnet_Train.arff";
		ScikitLearnWrapper slw = new ScikitLearnWrapper("MLPRegressor()",
				"from sklearn.neural_network import MLPRegressor");
		Instances datasetTrain = loadARFF(test_arff);
		Instances datasetTest = loadARFF(test_arff);
		int numberInstance = datasetTest.numInstances();
		slw.setIsRegression(true);
		int s = datasetTrain.numAttributes();
		slw.setTargets(s - 3, s - 2, s - 1);
		slw.buildClassifier(datasetTrain);
		slw.classifyInstances(datasetTest);
		List<List<Double>> rawOutput = slw.getRawLastClassificationResults();
		assertNotNull(rawOutput);
		assertThat(rawOutput.size(), equalTo(numberInstance));
		for (List<Double> column : rawOutput) {
			assertThat(column.size(), equalTo(3));
		}
	}

	@Test
	public void createImportStatementFromImportFolderInvalid() throws IOException {
		assertThat("", equalTo(ScikitLearnWrapper.createImportStatementFromImportFolder(null, true)));
		assertThat("", equalTo(ScikitLearnWrapper.createImportStatementFromImportFolder(
				new File("testsrc/ml/scikitwrapper/not_existing_folder"), true)));
		assertThat("", equalTo(ScikitLearnWrapper
				.createImportStatementFromImportFolder(new File("testsrc/ml/scikitwrapper/importfolder_empty"), true)));
	}

	@Test
	public void createImportStatementFromImportFolderValidWithNamespace() throws IOException {
		File importfolder = new File("testsrc/ml/scikitwrapper/importfolder_test");
		File initFile = new File(importfolder, "__init__.py");
		if (initFile.exists()) {
			initFile.delete();
		}
		try {
			String importStatement = ScikitLearnWrapper.createImportStatementFromImportFolder(importfolder, true);
			assertTrue(importStatement.contains("sys.path.append('" + importfolder.getAbsolutePath() + "')\n"));
			for (File module : importfolder.listFiles()) {
				if (!module.getName().startsWith("__")) {
					assertTrue(importStatement
							.contains("import " + module.getName().substring(0, module.getName().length() - 3) + "\n"));
				}
			}
			assertTrue(initFile.exists());
		} finally {
			if (initFile.exists()) {
				initFile.delete();
			}
		}
	}

	@Test
	public void createImportStatementFromImportFolderValidWithoutNamespace() throws IOException {
		File importfolder = new File("testsrc/ml/scikitwrapper/importfolder_test");
		File initFile = new File(importfolder, "__init__.py");
		if (initFile.exists()) {
			initFile.delete();
		}
		try {
			String importStatement = ScikitLearnWrapper.createImportStatementFromImportFolder(importfolder, false);
			assertTrue(importStatement.contains("sys.path.append('" + importfolder.getAbsolutePath() + "')\n"));
			for (File module : importfolder.listFiles()) {
				if (!module.getName().startsWith("__")) {
					assertTrue(importStatement.contains(
							"from " + module.getName().substring(0, module.getName().length() - 3) + " import *\n"));
				}
			}
			assertTrue(initFile.exists());
		} finally {
			if (initFile.exists()) {
				initFile.delete();
			}
		}
	}

	@Test
	public void loadOwnClassifierFromFileWithNamespace() throws Exception {
		File importfolder = new File("testsrc/ml/scikitwrapper/importfolder_test");
		String importStatement = ScikitLearnWrapper.createImportStatementFromImportFolder(importfolder, true);
		ScikitLearnWrapper slw = new ScikitLearnWrapper("test_module_1.My_MLPRegressor()", importStatement);
		Instances dataset = loadARFF("testsrc/ml/scikitwrapper/0532052678.arff");
		slw.setIsRegression(true);
		int s = dataset.numAttributes();
		slw.setTargets(s - 3, s - 2, s - 1);
		slw.buildClassifier(dataset);
		assertThat(slw.getModelPath(), not(equalTo("")));
	}

	@Test
	public void loadOwnClassifierFromFileWithoutNamespace() throws Exception {
		File importfolder = new File("testsrc/ml/scikitwrapper/importfolder_test");
		String importStatement = ScikitLearnWrapper.createImportStatementFromImportFolder(importfolder, false);
		ScikitLearnWrapper slw = new ScikitLearnWrapper("My_MLPRegressor()", importStatement);
		Instances dataset = loadARFF("testsrc/ml/scikitwrapper/0532052678.arff");
		slw.setIsRegression(true);
		int s = dataset.numAttributes();
		slw.setTargets(s - 3, s - 2, s - 1);
		slw.buildClassifier(dataset);
		assertThat(slw.getModelPath(), not(equalTo("")));
	}

	@Test(expected = AssertionError.class)
	public void invalidConstructorNoConstructionCall() throws IOException {
		new ScikitLearnWrapper(null, "");
	}

	@Test(expected = AssertionError.class)
	public void invalidConstructorEmptyConstructionCall() throws IOException {
		new ScikitLearnWrapper("", "");
	}

	@Test
	public void changeOutputFolder() throws Exception {
		File newOutputFolder = new File("testsrc/ml/scikitwrapper/newOutputFolder");
		if (newOutputFolder.exists()) {
			for (File f : newOutputFolder.listFiles()) {
				f.delete();
			}
		} else {
			newOutputFolder.mkdirs();
		}
		try {
			ScikitLearnWrapper slw = new ScikitLearnWrapper("MLPRegressor()",
					"from sklearn.neural_network import MLPRegressor");
			Instances dataset = loadARFF("testsrc/ml/scikitwrapper/0532052678.arff");
			slw.setOutputFolder(newOutputFolder);
			slw.setIsRegression(true);
			slw.buildClassifier(dataset);
			assertTrue(newOutputFolder.listFiles().length == 1);
			assertTrue(newOutputFolder.listFiles()[0].getName().contains("MLPRegressor"));
		} finally {
			if (newOutputFolder.exists()) {
				for (File f : newOutputFolder.listFiles()) {
					f.delete();
				}
			}
		}
	}

	private Instances loadARFF(String arffPath) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(arffPath));
		ArffReader arff = new ArffReader(reader);
		Instances data = arff.getData();
		data.setClassIndex(data.numAttributes() - 1);
		return data;
	}
}
