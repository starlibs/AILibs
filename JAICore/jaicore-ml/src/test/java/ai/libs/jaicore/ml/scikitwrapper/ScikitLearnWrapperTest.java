package ai.libs.jaicore.ml.scikitwrapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassification;
import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassificationPredictionBatch;
import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.IPredictionBatch;
import org.api4.java.ai.ml.core.evaluation.execution.IDatasetSplitSet;
import org.api4.java.ai.ml.regression.evaluation.IRegressionPrediction;
import org.api4.java.ai.ml.regression.evaluation.IRegressionResultBatch;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ai.libs.jaicore.ml.classification.singlelabel.SingleLabelClassification;
import ai.libs.jaicore.ml.classification.singlelabel.SingleLabelClassificationPredictionBatch;
import ai.libs.jaicore.ml.core.EScikitLearnProblemType;
import ai.libs.jaicore.ml.core.dataset.serialization.ArffDatasetAdapter;
import ai.libs.jaicore.ml.core.dataset.splitter.RandomHoldoutSplitter;
import ai.libs.jaicore.test.LongTest;

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

	@Test
	@LongTest
	public void fitRegression() throws Exception {
		ScikitLearnWrapper<IRegressionPrediction, IRegressionResultBatch> slw = new ScikitLearnWrapper<>("LinearRegression()", "from sklearn.linear_model import LinearRegression", EScikitLearnProblemType.REGRESSION);
		ILabeledDataset<ILabeledInstance> dataset = this.loadARFF(REGRESSION_ARFF);
		slw.fit(dataset);
		assertNotNull(MSG_MODELPATH_NOT_NULL, slw.getModelPath());
		assertTrue(slw.getModelPath().exists());
	}

	@Test
	@LongTest
	public void fitAndPredict() throws Exception {
		List<String> imports = Arrays.asList("sklearn", "sklearn.ensemble");
		String constructInstruction = "sklearn.ensemble.RandomForestClassifier(n_estimators=100)";
		ScikitLearnWrapper<ISingleLabelClassification, ISingleLabelClassificationPredictionBatch> slw = new ScikitLearnWrapper<>(constructInstruction, ScikitLearnWrapper.getImportString(imports), true,
				EScikitLearnProblemType.CLASSIFICATION);
		ILabeledDataset<ILabeledInstance> dataset = this.loadARFF(CLASSIFICATION_ARFF);
		RandomHoldoutSplitter<ILabeledDataset<ILabeledInstance>> splitter = new RandomHoldoutSplitter<>(new Random(), .7);
		IDatasetSplitSet<ILabeledDataset<ILabeledInstance>> set = splitter.nextSplitSet(dataset);

		long startTrain = System.currentTimeMillis();
		ISingleLabelClassificationPredictionBatch preds = slw.fitAndPredict(set.getFolds(0).get(0), set.getFolds(0).get(1));
		System.out.println("Call took: " + (System.currentTimeMillis() - startTrain) + "ms");

		assertNotNull(preds);
	}

	@Disabled("Currently multi-target is not supported anymore.")
	@Test
	@LongTest
	public void fitRegressionMultitarget() throws Exception {
		ScikitLearnWrapper<SingleLabelClassification, SingleLabelClassificationPredictionBatch> slw = new ScikitLearnWrapper<>("MLPRegressor(activation='logistic')", "from sklearn.neural_network import MLPRegressor",
				EScikitLearnProblemType.REGRESSION);
		ILabeledDataset<ILabeledInstance> dataset = this.loadARFF(REGRESSION_ARFF);
		slw.setProblemType(EScikitLearnProblemType.REGRESSION);
		int s = dataset.getNumAttributes();
		slw.setTargets(s - 1, s - 2, s - 3);
		slw.fit(dataset);
		assertNotNull(MSG_MODELPATH_NOT_NULL, slw.getModelPath());
		assertTrue(slw.getModelPath().exists());
	}

	@Disabled("Currently multi-target is not supported anymore.")
	@Test
	@LongTest
	public void trainAndTestClassifierRegressionMultitarget() throws Exception {
		ScikitLearnWrapper<SingleLabelClassification, SingleLabelClassificationPredictionBatch> slw = new ScikitLearnWrapper<>("MLPRegressor()", "from sklearn.neural_network import MLPRegressor", EScikitLearnProblemType.CLASSIFICATION);
		ILabeledDataset<ILabeledInstance> datasetTrain = this.loadARFF(BAYESNET_TRAIN_ARFF);
		ILabeledDataset<ILabeledInstance> datasetTest = datasetTrain;
		slw.setProblemType(EScikitLearnProblemType.CLASSIFICATION);
		int s = datasetTrain.getNumAttributes();
		int[] targetColumns = { s - 1, s - 2, s - 3 };
		slw.setTargets(targetColumns);
		slw.fit(datasetTrain);
		IPredictionBatch result = slw.predict(datasetTest);
		assertEquals("Unequal length of predictions and number of test ILabeledDataset<ILabeledInstance>", result.getNumPredictions(), targetColumns.length * datasetTest.size());
	}

	@Disabled("Currently unsupported feature")
	@Test
	@LongTest
	public void testClassifierRegression() throws Exception {
		ScikitLearnWrapper<IRegressionPrediction, IRegressionResultBatch> slw = new ScikitLearnWrapper<>("MLPRegressor()", "from sklearn.neural_network import MLPRegressor", EScikitLearnProblemType.REGRESSION);
		ILabeledDataset<ILabeledInstance> datasetTest = this.loadARFF(BAYESNET_TRAIN_ARFF);
		slw.setModelPath(new File(MLP_REGRESSOR_DUMP).getAbsoluteFile());
		IRegressionResultBatch result = slw.predict(datasetTest);
		assertEquals("Unequal length of predictions and number of test ILabeledDataset<ILabeledInstance>", result.getNumPredictions(), datasetTest.size());
	}

	@Test
	@LongTest
	public void trainClassifierCategorical() throws Exception {
		List<String> imports = Arrays.asList("sklearn", "sklearn.pipeline", "sklearn.decomposition", "sklearn.ensemble");
		String constructInstruction = "sklearn.pipeline.make_pipeline(sklearn.pipeline.make_union(sklearn.decomposition.PCA(),sklearn.decomposition.FastICA()),sklearn.ensemble.RandomForestClassifier(n_estimators=100))";
		ScikitLearnWrapper<SingleLabelClassification, SingleLabelClassificationPredictionBatch> slw = new ScikitLearnWrapper<>(constructInstruction, ScikitLearnWrapper.getImportString(imports), EScikitLearnProblemType.CLASSIFICATION);
		ILabeledDataset<ILabeledInstance> dataset = this.loadARFF(CLASSIFICATION_ARFF);
		slw.fit(dataset);
		assertNotNull(MSG_MODELPATH_NOT_NULL, slw.getModelPath());
		assertTrue(slw.getModelPath().exists());
	}

	@Test
	@LongTest
	public void trainAndTestClassifierCategorical() throws Exception {
		List<String> imports = Arrays.asList("sklearn", "sklearn.pipeline", "sklearn.decomposition", "sklearn.ensemble");
		String constructInstruction = "sklearn.pipeline.make_pipeline(sklearn.pipeline.make_union(sklearn.decomposition.PCA(),sklearn.decomposition.FastICA()),sklearn.ensemble.RandomForestClassifier(n_estimators=100))";
		ScikitLearnWrapper<SingleLabelClassification, SingleLabelClassificationPredictionBatch> slw = new ScikitLearnWrapper<>(constructInstruction, ScikitLearnWrapper.getImportString(imports), EScikitLearnProblemType.CLASSIFICATION);
		ILabeledDataset<ILabeledInstance> datasetTrain = this.loadARFF(CLASSIFICATION_ARFF);
		ILabeledDataset<ILabeledInstance> datasetTest = (ILabeledDataset<ILabeledInstance>) datasetTrain.createCopy();
		slw.fit(datasetTrain);
		ISingleLabelClassificationPredictionBatch result = slw.predict(datasetTest);
		assertEquals("Unequal length of predictions and number of test ILabeledDataset<ILabeledInstance>", result.getNumPredictions(), datasetTest.size());
	}

	@Test
	@Disabled("Currently unsupported feature.")
	@LongTest
	public void testClassifierCategorical() throws Exception {
		List<String> imports = Arrays.asList("sklearn", "sklearn.pipeline", "sklearn.decomposition", "sklearn.ensemble");
		String constructInstruction = "sklearn.pipeline.make_pipeline(sklearn.pipeline.make_union(sklearn.decomposition.PCA(),sklearn.decomposition.FastICA()),sklearn.ensemble.RandomForestClassifier(n_estimators=100))";
		ScikitLearnWrapper<ISingleLabelClassification, ISingleLabelClassificationPredictionBatch> slw = new ScikitLearnWrapper<>(constructInstruction, ScikitLearnWrapper.getImportString(imports), EScikitLearnProblemType.CLASSIFICATION);
		ILabeledDataset<ILabeledInstance> datasetTest = this.loadARFF(CLASSIFICATION_ARFF);
		slw.setModelPath(new File(CLASSIFIER_DUMP).getAbsoluteFile());
		ISingleLabelClassificationPredictionBatch result = slw.predict(datasetTest);
		assertEquals("Unequal length of predictions and number of test ILabeledDataset<ILabeledInstance>", result.getNumPredictions(), datasetTest.size());
	}

	@Test
	@LongTest
	public void getRawOutput() throws Exception {
		ScikitLearnWrapper<IRegressionPrediction, IRegressionResultBatch> slw = new ScikitLearnWrapper<>("MLPRegressor()", "from sklearn.neural_network import MLPRegressor", EScikitLearnProblemType.REGRESSION);
		ILabeledDataset<ILabeledInstance> datasetTrain = this.loadARFF(BAYESNET_TRAIN_ARFF);
		ILabeledDataset<ILabeledInstance> datasetTest = this.loadARFF(BAYESNET_TRAIN_ARFF);
		int s = datasetTrain.getNumAttributes();
		slw.setTargets(s - 1, s - 2, s - 3);
		slw.fit(datasetTrain);
		slw.predict(datasetTest);
		assertNotNull(MSG_MODELPATH_NOT_NULL, slw.getModelPath());
		assertTrue(slw.getModelPath().exists());
	}

	@Test
	@LongTest
	public void invalidConstructorNoConstructionCall() throws IOException {
		assertThrows(AssertionError.class, () -> {
			new ScikitLearnWrapper<SingleLabelClassification, SingleLabelClassificationPredictionBatch>(null, "", EScikitLearnProblemType.CLASSIFICATION);
		});
	}

	@Test
	@LongTest
	public void invalidConstructorEmptyConstructionCall() throws IOException {
		assertThrows(AssertionError.class, () -> {
			new ScikitLearnWrapper<SingleLabelClassification, SingleLabelClassificationPredictionBatch>("", "", EScikitLearnProblemType.CLASSIFICATION);
		});
	}

	private ILabeledDataset<ILabeledInstance> loadARFF(final String arffPath) throws IOException, DatasetDeserializationFailedException, InterruptedException {
		return ArffDatasetAdapter.readDataset(new File(arffPath));
	}

}
