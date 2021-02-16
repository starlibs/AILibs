package ai.libs.jaicore.ml.scikitwrapper;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.IPrediction;
import org.api4.java.ai.ml.core.evaluation.IPredictionBatch;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.jupiter.api.Test;

import ai.libs.jaicore.ml.core.dataset.serialization.ArffDatasetAdapter;

public class ScikitLearnWrapperTest {

	private static final String BASE_TESTRSC_PATH = "testrsc/ml/scikitwrapper/";

	@BeforeClass
	@AfterClass
	public static void setup() throws IOException {
		FileUtils.deleteDirectory(new File("tmp"));
	}

	@Test
	public void learnClassification() throws Exception {
		ILabeledDataset<ILabeledInstance> data = ArffDatasetAdapter.readDataset(new File(BASE_TESTRSC_PATH + "dataset_31_credit-g.arff"));
		String imports = "from sklearn.ensemble import RandomForestClassifier\n";
		String pipeline = "RandomForestClassifier(n_estimators=100)";

		FileUtils.deleteDirectory(new File("tmp"));
		IScikitLearnWrapper model = new ScikitLearnClassificationWrapper<>(pipeline, imports);
		model.fit(data);
		assertTrue(model.getSKLearnScriptFile().exists());
		assertTrue(model.getModelFile().exists());

		model.predict(data);
		assertTrue(model.getOutputFile(model.getDataName(data)).exists());

		FileUtils.deleteDirectory(new File("tmp"));
		model = new ScikitLearnClassificationWrapper<IPrediction, IPredictionBatch>(pipeline, imports);
		model.fitAndPredict(data, data);
		assertTrue(model.getSKLearnScriptFile().exists());
		assertTrue(model.getOutputFile(model.getDataName(data)).exists());
	}

	@Test
	public void learnRegression() throws Exception {
		ILabeledDataset<ILabeledInstance> data = ArffDatasetAdapter.readDataset(new File(BASE_TESTRSC_PATH + "0532052678.arff"));
		String imports = "from sklearn.linear_model import LinearRegression\n";
		String pipeline = "LinearRegression()";

		FileUtils.deleteDirectory(new File("tmp"));
		IScikitLearnWrapper model = new ScikitLearnRegressionWrapper<>(pipeline, imports);
		model.fit(data);
		assertTrue(model.getSKLearnScriptFile().exists());
		assertTrue(model.getModelFile().exists());

		model.predict(data);
		assertTrue(model.getOutputFile(model.getDataName(data)).exists());

		FileUtils.deleteDirectory(new File("tmp"));
		model = new ScikitLearnRegressionWrapper<IPrediction, IPredictionBatch>(pipeline, imports);
		model.fitAndPredict(data, data);
		assertTrue(model.getSKLearnScriptFile().exists());
		assertTrue(model.getOutputFile(model.getDataName(data)).exists());
	}

	@Test
	public void learnRUL() throws Exception {
		ILabeledDataset<ILabeledInstance> trainingData = ArffDatasetAdapter.readDataset(new File(BASE_TESTRSC_PATH + "CMAPSS_FD001_train.arff"));
		ILabeledDataset<ILabeledInstance> testingData = ArffDatasetAdapter.readDataset(new File(BASE_TESTRSC_PATH + "CMAPSS_FD001_test.arff"));
		String imports = "from python_connection.feature_generation.ultra_fast_shapelets_feature_generator import UltraFastShapeletsFeatureExtractor\n"
				+ "from python_connection.feature_generation.uni_to_multi_numpy_feature_generator import UniToMultivariateNumpyBasedFeatureGenerator\n" + "from sklearn.pipeline import make_pipeline\n"
				+ "from sklearn.svm import SVR\n";
		String pipeline = "make_pipeline(UniToMultivariateNumpyBasedFeatureGenerator(univariate_ts_feature_generator=UltraFastShapeletsFeatureExtractor(keep_candidates_percentage=0.1212768581941968)), SVR(C=0.27332631373580385,coef0=1.3310979035891473,degree=3,gamma=1.2,kernel=\"poly\",max_iter=5236,shrinking=True,tol=0.27707849007413665))";

		FileUtils.deleteDirectory(new File("tmp"));
		IScikitLearnWrapper model = new ScikitLearnRULWrapper<>(pipeline, imports);
		model.fit(trainingData);
		assertTrue(model.getSKLearnScriptFile().exists());
		assertTrue(model.getModelFile().exists());

		model.predict(testingData);
		assertTrue(model.getOutputFile(model.getDataName(testingData)).exists());

		FileUtils.deleteDirectory(new File("tmp"));
		model = new ScikitLearnRULWrapper<IPrediction, IPredictionBatch>(pipeline, imports);
		model.fitAndPredict(trainingData, testingData);
		assertTrue(model.getSKLearnScriptFile().exists());
		assertTrue(model.getOutputFile(model.getDataName(testingData)).exists());
	}

	@Test
	public void learnFeatureEngineering() throws Exception {
		ILabeledDataset<ILabeledInstance> trainingData = ArffDatasetAdapter.readDataset(new File(BASE_TESTRSC_PATH + "CMAPSS_FD001_train.arff"));
		ILabeledDataset<ILabeledInstance> testingData = ArffDatasetAdapter.readDataset(new File(BASE_TESTRSC_PATH + "CMAPSS_FD001_test.arff"));
		String imports = "from python_connection.feature_generation.ultra_fast_shapelets_feature_generator import UltraFastShapeletsFeatureExtractor\n"
				+ "from python_connection.feature_generation.uni_to_multi_numpy_feature_generator import UniToMultivariateNumpyBasedFeatureGenerator\n"
				+ "from sklearn.pipeline import make_pipeline\n";
		String pipeline = "make_pipeline(UniToMultivariateNumpyBasedFeatureGenerator(univariate_ts_feature_generator=UltraFastShapeletsFeatureExtractor(keep_candidates_percentage=0.1212768581941968)))";

		FileUtils.deleteDirectory(new File("tmp"));
		IScikitLearnWrapper model = new ScikitLearnFeatureEngineeringWrapper<>(pipeline, imports);
		model.fit(trainingData);
		assertTrue(model.getSKLearnScriptFile().exists());
		assertTrue(model.getModelFile().exists());
		assertTrue(model.getOutputFile(model.getDataName(trainingData)).exists());

		model.predict(testingData);
		assertTrue(model.getOutputFile(model.getDataName(testingData)).exists());

		FileUtils.deleteDirectory(new File("tmp"));
		model = new ScikitLearnFeatureEngineeringWrapper<IPrediction, IPredictionBatch>(pipeline, imports);
		model.fitAndPredict(trainingData, testingData);
		assertTrue(model.getSKLearnScriptFile().exists());
		assertTrue(model.getOutputFile(model.getDataName(trainingData)).exists());
		assertTrue(model.getOutputFile(model.getDataName(testingData)).exists());
	}

}
