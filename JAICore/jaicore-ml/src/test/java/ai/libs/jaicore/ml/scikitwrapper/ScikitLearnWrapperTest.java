package ai.libs.jaicore.ml.scikitwrapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassificationPredictionBatch;
import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.execution.IDatasetSplitSet;
import org.junit.Test;

import ai.libs.jaicore.ml.classification.singlelabel.SingleLabelClassification;
import ai.libs.jaicore.ml.classification.singlelabel.SingleLabelClassificationPredictionBatch;
import ai.libs.jaicore.ml.core.dataset.serialization.ArffDatasetAdapter;
import ai.libs.jaicore.ml.core.dataset.splitter.RandomHoldoutSplitter;

/**
 * REQUIREMENTS: python 3.6.4 + scikit-learn 0.20.0 need to be installed in order to run these tests.
 *
 * @author Marcel
 *
 */
public class ScikitLearnWrapperTest {

	private static final String MSG_MODELPATH_NOT_NULL = "Model path must not be null.";

	private static final String BASE_TESTRSC_PATH = "testrsc/ml/scikitwrapper/";
	private static final String REGRESSION_ARFF = BASE_TESTRSC_PATH + "0532052678.arff";
	private static final String CLASSIFICATION_ARFF = BASE_TESTRSC_PATH + "dataset_31_credit-g.arff";
	private static final String BAYESNET_TRAIN_ARFF = BASE_TESTRSC_PATH + "Bayesnet_Train.arff";
	private static final String MLP_REGRESSOR_DUMP = BASE_TESTRSC_PATH + "01673183575_MLPRegressor.pcl";
	private static final String CLASSIFIER_DUMP = BASE_TESTRSC_PATH + "0800955787_Pipeline.pcl";
	private static final String OWN_CLASSIFIER_DUMP = BASE_TESTRSC_PATH + "0532052678.arff";
	private static final String IMPORT_FOLDER = BASE_TESTRSC_PATH + "importfolder_test";

	@Test
	public void fitRegression() throws Exception {
		ScikitLearnWrapper<SingleLabelClassification, SingleLabelClassificationPredictionBatch> slw = new ScikitLearnWrapper<>("LinearRegression()", "from sklearn.linear_model import LinearRegression", EBasicProblemType.CLASSIFICATION);
		ILabeledDataset<ILabeledInstance> dataset = this.loadARFF(REGRESSION_ARFF);
		slw.setProblemType(EBasicProblemType.CLASSIFICATION);
		slw.fit(dataset);
		assertNotNull(MSG_MODELPATH_NOT_NULL, slw.getModelPath());
		assertTrue(slw.getModelPath().exists());
	}

	@Test
	public void fitAndPredict() throws Exception {
		List<String> imports = Arrays.asList("sklearn", "sklearn.ensemble");
		String constructInstruction = "sklearn.ensemble.RandomForestClassifier(n_estimators=100)";
		ScikitLearnWrapper<SingleLabelClassification, SingleLabelClassificationPredictionBatch> slw = new ScikitLearnWrapper<>(constructInstruction, ScikitLearnWrapper.getImportString(imports), false, EBasicProblemType.CLASSIFICATION);
		ILabeledDataset<ILabeledInstance> dataset = this.loadARFF(CLASSIFICATION_ARFF);
		RandomHoldoutSplitter<ILabeledDataset<ILabeledInstance>> splitter = new RandomHoldoutSplitter<>(new Random(), .7);
		IDatasetSplitSet<ILabeledDataset<ILabeledInstance>> set = splitter.nextSplitSet(dataset);

		long startTrain = System.currentTimeMillis();
		slw.fit(set.getFolds(0).get(0));
		System.out.println("Build took: " + (System.currentTimeMillis() - startTrain));

		long startVal = System.currentTimeMillis();
		assertNotNull(slw.predict(set.getFolds(0).get(1)));
		System.out.println("Validation took: " + (System.currentTimeMillis() - startVal));
	}

	@Test
	public void fitRegressionMultitarget() throws Exception {
		ScikitLearnWrapper<SingleLabelClassification, SingleLabelClassificationPredictionBatch> slw = new ScikitLearnWrapper<>("MLPRegressor(activation='logistic')", "from sklearn.neural_network import MLPRegressor",
				EBasicProblemType.REGRESSION);
		ILabeledDataset<ILabeledInstance> dataset = this.loadARFF(REGRESSION_ARFF);
		slw.setProblemType(EBasicProblemType.REGRESSION);
		int s = dataset.getNumAttributes();
		slw.setTargets(s - 1, s - 2, s - 3);
		slw.fit(dataset);
		assertNotNull(MSG_MODELPATH_NOT_NULL, slw.getModelPath());
		assertTrue(slw.getModelPath().exists());
	}

	@Test
	public void trainAndTestClassifierRegressionMultitarget() throws Exception {
		ScikitLearnWrapper<SingleLabelClassification, SingleLabelClassificationPredictionBatch> slw = new ScikitLearnWrapper<>("MLPRegressor()", "from sklearn.neural_network import MLPRegressor", EBasicProblemType.CLASSIFICATION);
		ILabeledDataset<ILabeledInstance> datasetTrain = this.loadARFF(BAYESNET_TRAIN_ARFF);
		ILabeledDataset<ILabeledInstance> datasetTest = datasetTrain;
		slw.setProblemType(EBasicProblemType.CLASSIFICATION);
		int s = datasetTrain.getNumAttributes();
		int[] targetColumns = { s - 1, s - 2, s - 3 };
		slw.setTargets(targetColumns);
		slw.fit(datasetTrain);
		ISingleLabelClassificationPredictionBatch result = slw.predict(datasetTest);
		assertEquals("Unequal length of predictions and number of test ILabeledDataset<ILabeledInstance>", result.getNumPredictions(), targetColumns.length * datasetTest.size());
	}

	@Test
	public void testClassifierRegression() throws Exception {
		ScikitLearnWrapper<SingleLabelClassification, SingleLabelClassificationPredictionBatch> slw = new ScikitLearnWrapper<>("MLPRegressor()", "from sklearn.neural_network import MLPRegressor", EBasicProblemType.CLASSIFICATION);
		ILabeledDataset<ILabeledInstance> datasetTest = this.loadARFF(BAYESNET_TRAIN_ARFF);
		slw.setModelPath(new File(MLP_REGRESSOR_DUMP).getAbsoluteFile());
		slw.setProblemType(EBasicProblemType.CLASSIFICATION);
		ISingleLabelClassificationPredictionBatch result = slw.predict(datasetTest);
		assertEquals("Unequal length of predictions and number of test ILabeledDataset<ILabeledInstance>", result.getNumPredictions(), datasetTest.size());
	}

	@Test
	public void trainClassifierCategorical() throws Exception {
		List<String> imports = Arrays.asList("sklearn", "sklearn.pipeline", "sklearn.decomposition", "sklearn.ensemble");
		String constructInstruction = "sklearn.pipeline.make_pipeline(sklearn.pipeline.make_union(sklearn.decomposition.PCA(),sklearn.decomposition.FastICA()),sklearn.ensemble.RandomForestClassifier(n_estimators=100))";
		ScikitLearnWrapper<SingleLabelClassification, SingleLabelClassificationPredictionBatch> slw = new ScikitLearnWrapper<>(constructInstruction, ScikitLearnWrapper.getImportString(imports), EBasicProblemType.CLASSIFICATION);
		ILabeledDataset<ILabeledInstance> dataset = this.loadARFF(CLASSIFICATION_ARFF);
		slw.fit(dataset);
		System.out.println(slw.getModelPath());
		assertNotNull(MSG_MODELPATH_NOT_NULL, slw.getModelPath());
		assertTrue(slw.getModelPath().exists());
	}

	@Test
	public void trainAndTestClassifierCategorical() throws Exception {
		List<String> imports = Arrays.asList("sklearn", "sklearn.pipeline", "sklearn.decomposition", "sklearn.ensemble");
		String constructInstruction = "sklearn.pipeline.make_pipeline(sklearn.pipeline.make_union(sklearn.decomposition.PCA(),sklearn.decomposition.FastICA()),sklearn.ensemble.RandomForestClassifier(n_estimators=100))";
		ScikitLearnWrapper<SingleLabelClassification, SingleLabelClassificationPredictionBatch> slw = new ScikitLearnWrapper<>(constructInstruction, ScikitLearnWrapper.getImportString(imports), EBasicProblemType.CLASSIFICATION);
		ILabeledDataset<ILabeledInstance> datasetTrain = this.loadARFF(CLASSIFICATION_ARFF);
		ILabeledDataset<ILabeledInstance> datasetTest = (ILabeledDataset<ILabeledInstance>) datasetTrain.createCopy();
		slw.fit(datasetTrain);
		ISingleLabelClassificationPredictionBatch result = slw.predict(datasetTest);
		assertEquals("Unequal length of predictions and number of test ILabeledDataset<ILabeledInstance>", result.getNumPredictions(), datasetTest.size());
	}

	@Test
	public void testClassifierCategorical() throws Exception {
		List<String> imports = Arrays.asList("sklearn", "sklearn.pipeline", "sklearn.decomposition", "sklearn.ensemble");
		String constructInstruction = "sklearn.pipeline.make_pipeline(sklearn.pipeline.make_union(sklearn.decomposition.PCA(),sklearn.decomposition.FastICA()),sklearn.ensemble.RandomForestClassifier(n_estimators=100))";
		ScikitLearnWrapper<SingleLabelClassification, SingleLabelClassificationPredictionBatch> slw = new ScikitLearnWrapper<>(constructInstruction, ScikitLearnWrapper.getImportString(imports), EBasicProblemType.CLASSIFICATION);
		ILabeledDataset<ILabeledInstance> datasetTest = this.loadARFF(CLASSIFICATION_ARFF);
		slw.setModelPath(new File(CLASSIFIER_DUMP).getAbsoluteFile());
		ISingleLabelClassificationPredictionBatch result = slw.predict(datasetTest);
		assertEquals("Unequal length of predictions and number of test ILabeledDataset<ILabeledInstance>", result.getNumPredictions(), datasetTest.size());
	}

	@Test
	public void getRawOutput() throws Exception {
		ScikitLearnWrapper<SingleLabelClassification, SingleLabelClassificationPredictionBatch> slw = new ScikitLearnWrapper<>("MLPRegressor()", "from sklearn.neural_network import MLPRegressor", EBasicProblemType.CLASSIFICATION);
		ILabeledDataset<ILabeledInstance> datasetTrain = this.loadARFF(BAYESNET_TRAIN_ARFF);
		ILabeledDataset<ILabeledInstance> datasetTest = this.loadARFF(BAYESNET_TRAIN_ARFF);
		slw.setProblemType(EBasicProblemType.REGRESSION);
		int s = datasetTrain.getNumAttributes();
		slw.setTargets(s - 1, s - 2, s - 3);
		slw.fit(datasetTrain);
		slw.predict(datasetTest);
		assertNotNull(MSG_MODELPATH_NOT_NULL, slw.getModelPath());
		assertTrue(slw.getModelPath().exists());
	}

	@Test
	public void loadOwnClassifierFromFileWithNamespace() throws Exception {
		File importfolder = new File(IMPORT_FOLDER);
		String importStatement = ScikitLearnWrapper.createImportStatementFromImportFolder(importfolder, true);
		ScikitLearnWrapper<SingleLabelClassification, SingleLabelClassificationPredictionBatch> slw = new ScikitLearnWrapper<>("test_module_1.My_MLPRegressor()", importStatement, EBasicProblemType.CLASSIFICATION);
		ILabeledDataset<ILabeledInstance> dataset = this.loadARFF(REGRESSION_ARFF);
		slw.setProblemType(EBasicProblemType.REGRESSION);
		int s = dataset.getNumAttributes();
		slw.setTargets(s - 1, s - 2, s - 3);
		slw.fit(dataset);
		assertNotNull(MSG_MODELPATH_NOT_NULL, slw.getModelPath());
		assertTrue(slw.getModelPath().exists());
	}

	@Test
	public void loadOwnClassifierFromFileWithoutNamespace() throws Exception {
		File importfolder = new File(IMPORT_FOLDER);
		String importStatement = ScikitLearnWrapper.createImportStatementFromImportFolder(importfolder, false);
		ScikitLearnWrapper<SingleLabelClassification, SingleLabelClassificationPredictionBatch> slw = new ScikitLearnWrapper<>("My_MLPRegressor()", importStatement, EBasicProblemType.CLASSIFICATION);
		ILabeledDataset<ILabeledInstance> dataset = this.loadARFF(OWN_CLASSIFIER_DUMP);
		slw.setProblemType(EBasicProblemType.REGRESSION);
		int s = dataset.getNumAttributes();
		slw.setTargets(s - 1, s - 2, s - 3);
		slw.fit(dataset);
		assertNotNull(MSG_MODELPATH_NOT_NULL, slw.getModelPath());
		assertTrue(slw.getModelPath().exists());
	}

	@Test
	public void invalidConstructorNoConstructionCall() throws IOException {
		boolean errorTriggeredFlag = false;
		try {
			new ScikitLearnWrapper<SingleLabelClassification, SingleLabelClassificationPredictionBatch>(null, "", EBasicProblemType.CLASSIFICATION);
		} catch (AssertionError e) {
			errorTriggeredFlag = true;
		}
		assertTrue(errorTriggeredFlag);
	}

	@Test
	public void invalidConstructorEmptyConstructionCall() throws IOException {
		boolean errorTriggeredFlag = false;
		try {
			new ScikitLearnWrapper<SingleLabelClassification, SingleLabelClassificationPredictionBatch>("", "", EBasicProblemType.CLASSIFICATION);
		} catch (AssertionError e) {
			errorTriggeredFlag = true;
		}
		assertTrue(errorTriggeredFlag);
	}

	private ILabeledDataset<ILabeledInstance> loadARFF(final String arffPath) throws IOException, DatasetDeserializationFailedException, InterruptedException {
		return ArffDatasetAdapter.readDataset(new File(arffPath));
	}
}
