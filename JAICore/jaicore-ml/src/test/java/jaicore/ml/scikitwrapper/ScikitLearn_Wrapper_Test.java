package jaicore.ml.scikitwrapper;

import static org.junit.Assert.assertEquals;

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

public class ScikitLearn_Wrapper_Test {

	@Test
	public void buildClassifierRegression() throws Exception {
		ScikitLearnWrapper slw = new ScikitLearnWrapper("LinearRegression()", "from sklearn.linear_model import LinearRegression");
		Instances dataset = this.loadARFF("testsrc/ml/scikitwrapper/0532052678.arff");
		slw.setProblemType(ProblemType.REGRESSION);
		slw.buildClassifier(dataset);
	}

	@Test
	public void buildClassifierRegressionMultitarget() throws Exception {
		ScikitLearnWrapper slw = new ScikitLearnWrapper("MLPRegressor(activation='logistic')", "from sklearn.neural_network import MLPRegressor");
		Instances dataset = this.loadARFF("testsrc/ml/scikitwrapper/0532052678.arff");
		slw.setProblemType(ProblemType.REGRESSION);
		int s = dataset.numAttributes();
		slw.setTargets(s - 1, s - 2, s - 3);
		slw.buildClassifier(dataset);
	}

	@Test
	public void trainAndTestClassifierRegressionMultitarget() throws Exception {
		String test_arff = "testsrc/ml/scikitwrapper/Bayesnet_Train.arff";
		ScikitLearnWrapper slw = new ScikitLearnWrapper("MLPRegressor()", "from sklearn.neural_network import MLPRegressor");
		Instances datasetTrain = this.loadARFF(test_arff);
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
		String test_arff = "testsrc/ml/scikitwrapper/Bayesnet_Train.arff";
		ScikitLearnWrapper slw = new ScikitLearnWrapper("MLPRegressor()", "from sklearn.neural_network import MLPRegressor");
		Instances datasetTest = this.loadARFF(test_arff);
		slw.setModelPath(new File("testsrc/ml/scikitwrapper/01673183575_MLPRegressor.pcl").getAbsoluteFile());
		slw.setProblemType(ProblemType.REGRESSION);
		double[] result = slw.classifyInstances(datasetTest);
		assertEquals("Unequal length of predictions and number of test instances", result.length, datasetTest.size());
	}

	@Test
	public void trainClassifierCategorical() throws Exception {
		List<String> imports = Arrays.asList("sklearn", "sklearn.pipeline", "sklearn.decomposition", "sklearn.ensemble");
		String constructInstruction = "sklearn.pipeline.make_pipeline(sklearn.pipeline.make_union(sklearn.decomposition.PCA(),sklearn.decomposition.FastICA()),sklearn.ensemble.RandomForestClassifier(n_estimators=100))";
		ScikitLearnWrapper slw = new ScikitLearnWrapper(constructInstruction, ScikitLearnWrapper.getImportString(imports));
		Instances dataset = this.loadARFF("testsrc/ml/scikitwrapper/dataset_31_credit-g.arff");
		slw.buildClassifier(dataset);
	}

	@Test
	public void trainAndTestClassifierCategorical() throws Exception {
		List<String> imports = Arrays.asList("sklearn", "sklearn.pipeline", "sklearn.decomposition", "sklearn.ensemble");
		String constructInstruction = "sklearn.pipeline.make_pipeline(sklearn.pipeline.make_union(sklearn.decomposition.PCA(),sklearn.decomposition.FastICA()),sklearn.ensemble.RandomForestClassifier(n_estimators=100))";
		ScikitLearnWrapper slw = new ScikitLearnWrapper(constructInstruction, ScikitLearnWrapper.getImportString(imports));
		Instances datasetTrain = this.loadARFF("testsrc/ml/scikitwrapper/dataset_31_credit-g.arff");
		Instances datasetTest = datasetTrain;
		slw.buildClassifier(datasetTrain);
		double[] result = slw.classifyInstances(datasetTest);
		assertEquals("Unequal length of predictions and number of test instances", result.length, datasetTest.size());
	}

	@Test
	public void testClassifierCategorical() throws Exception {
		List<String> imports = Arrays.asList("sklearn", "sklearn.pipeline", "sklearn.decomposition", "sklearn.ensemble");
		String test_arff = "testsrc/ml/scikitwrapper/dataset_31_credit-g.arff";
		String constructInstruction = "sklearn.pipeline.make_pipeline(sklearn.pipeline.make_union(sklearn.decomposition.PCA(),sklearn.decomposition.FastICA()),sklearn.ensemble.RandomForestClassifier(n_estimators=100))";
		ScikitLearnWrapper slw = new ScikitLearnWrapper(constructInstruction, ScikitLearnWrapper.getImportString(imports));
		Instances datasetTest = this.loadARFF(test_arff);
		slw.setModelPath(new File("testsrc/ml/scikitwrapper/02055055033_Pipeline.pcl").getAbsoluteFile());
		double[] result = slw.classifyInstances(datasetTest);
		assertEquals("Unequal length of predictions and number of test instances", result.length, datasetTest.size());
	}

	@Test
	public void getRawOutput() throws Exception {
		String test_arff = "testsrc/ml/scikitwrapper/Bayesnet_Train.arff";
		ScikitLearnWrapper slw = new ScikitLearnWrapper("MLPRegressor()", "from sklearn.neural_network import MLPRegressor");
		Instances datasetTrain = this.loadARFF(test_arff);
		Instances datasetTest = this.loadARFF(test_arff);
		slw.setProblemType(ProblemType.REGRESSION);
		int s = datasetTrain.numAttributes();
		slw.setTargets(s - 1, s - 2, s - 3);
		slw.buildClassifier(datasetTrain);
		slw.classifyInstances(datasetTest);
	}

	@Test
	public void loadOwnClassifierFromFileWithNamespace() throws Exception {
		File importfolder = new File("testsrc/ml/scikitwrapper/importfolder_test");
		String importStatement = ScikitLearnWrapper.createImportStatementFromImportFolder(importfolder, true);
		ScikitLearnWrapper slw = new ScikitLearnWrapper("test_module_1.My_MLPRegressor()", importStatement);
		Instances dataset = this.loadARFF("testsrc/ml/scikitwrapper/0532052678.arff");
		slw.setProblemType(ProblemType.REGRESSION);
		int s = dataset.numAttributes();
		slw.setTargets(s - 1, s - 2, s - 3);
		slw.buildClassifier(dataset);
	}

	@Test
	public void loadOwnClassifierFromFileWithoutNamespace() throws Exception {
		File importfolder = new File("testsrc/ml/scikitwrapper/importfolder_test");
		String importStatement = ScikitLearnWrapper.createImportStatementFromImportFolder(importfolder, false);
		ScikitLearnWrapper slw = new ScikitLearnWrapper("My_MLPRegressor()", importStatement);
		Instances dataset = this.loadARFF("testsrc/ml/scikitwrapper/0532052678.arff");
		slw.setProblemType(ProblemType.REGRESSION);
		int s = dataset.numAttributes();
		slw.setTargets(s - 1, s - 2, s - 3);
		slw.buildClassifier(dataset);
	}

	@Test
	public void invalidConstructorNoConstructionCall() throws IOException {
		new ScikitLearnWrapper(null, "");
	}

	@Test
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
			ScikitLearnWrapper slw = new ScikitLearnWrapper("MLPRegressor()", "from sklearn.neural_network import MLPRegressor");
			Instances dataset = this.loadARFF("testsrc/ml/scikitwrapper/0532052678.arff");
			slw.setProblemType(ProblemType.REGRESSION);
			slw.buildClassifier(dataset);
		} finally {
			if (newOutputFolder.exists()) {
				for (File f : newOutputFolder.listFiles()) {
					f.delete();
				}
			}
		}
	}

	private Instances loadARFF(final String arffPath) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(arffPath));
		ArffReader arff = new ArffReader(reader);
		Instances data = arff.getData();
		data.setClassIndex(data.numAttributes() - 1);
		return data;
	}
}
