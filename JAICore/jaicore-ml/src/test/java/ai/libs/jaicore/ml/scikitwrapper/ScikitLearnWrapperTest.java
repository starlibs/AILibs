package ai.libs.jaicore.ml.scikitwrapper;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.dataset.splitter.SplitFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.exception.PredictionException;
import org.api4.java.ai.ml.core.exception.TrainingException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ai.libs.jaicore.ml.core.EScikitLearnProblemType;
import ai.libs.jaicore.ml.core.dataset.serialization.ArffDatasetAdapter;
import ai.libs.jaicore.ml.core.dataset.splitter.RandomHoldoutSplitter;
import ai.libs.jaicore.test.MediumParameterizedTest;

public class ScikitLearnWrapperTest {

	public static final String FOLDER_TMP = "tmp";
	public static final String FOLDER_MODEL_DUMPS = FOLDER_TMP + "model_dumps";
	public static final String BASE_TESTRSC_PATH = "testrsc/ml/scikitwrapper/";

	@BeforeEach
	@AfterEach
	public void onSetUp() throws IOException {
		for (File file : new File(FOLDER_TMP).listFiles()) {
			if (!file.isDirectory() && !file.getName().endsWith(".py")) {
				if (!file.delete()) {
					System.out.println("delet did not work for: " + file.getName());
				}
			}
		}
		FileUtils.deleteDirectory(new File(FOLDER_MODEL_DUMPS));
	}

	@BeforeAll
	@AfterAll
	public static void onTearDown() throws IOException {
		FileUtils.deleteDirectory(new File(FOLDER_TMP));
	}

	@MediumParameterizedTest
	@MethodSource("arguments")
	public void testFitPredict(final EScikitLearnProblemType problemType, final String datasetName, final IScikitLearnWrapper model)
			throws IOException, InterruptedException, TrainingException, PredictionException, DatasetDeserializationFailedException, SplitFailedException {
		ILabeledDataset<ILabeledInstance> data = ArffDatasetAdapter.readDataset(new File(BASE_TESTRSC_PATH + datasetName));
		List<ILabeledDataset<ILabeledInstance>> split = RandomHoldoutSplitter.createSplit(data, 42, .7);

		model.fit(split.get(0));
		assertTrue(model.getSKLearnScriptFile().exists());
		assertTrue(model.getModelFile().exists());
		if (problemType == EScikitLearnProblemType.TIME_SERIES_FEATURE_ENGINEERING) {
			assertTrue(model.getOutputFile(model.getDataName(split.get(0))).exists());
		}

		model.predict(split.get(1));
		assertTrue(model.getOutputFile(model.getDataName(split.get(1))).exists());
	}

	@MediumParameterizedTest
	@MethodSource("arguments")
	public void testFitAndPredict(final EScikitLearnProblemType problemType, final String datasetName, final IScikitLearnWrapper model)
			throws IOException, InterruptedException, TrainingException, PredictionException, DatasetDeserializationFailedException, SplitFailedException {
		ILabeledDataset<ILabeledInstance> data = ArffDatasetAdapter.readDataset(new File(BASE_TESTRSC_PATH + datasetName));
		List<ILabeledDataset<ILabeledInstance>> split = RandomHoldoutSplitter.createSplit(data, 42, .7);

		model.fitAndPredict(split.get(0), split.get(1));
		assertTrue(model.getSKLearnScriptFile().exists());
		if (problemType == EScikitLearnProblemType.TIME_SERIES_FEATURE_ENGINEERING) {
			assertTrue(model.getOutputFile(model.getDataName(split.get(0))).exists());
		}
		assertTrue(model.getOutputFile(model.getDataName(split.get(1))).exists());
	}

	public static Stream<Arguments> arguments() throws IOException {
		return Stream.of( //
				Arguments.of(EScikitLearnProblemType.CLASSIFICATION, "dataset_31_credit-g.arff", new ScikitLearnClassificationWrapper<>("RandomForestClassifier(n_estimators=100)", "from sklearn.ensemble import RandomForestClassifier\n")), //
				Arguments.of(EScikitLearnProblemType.REGRESSION, "0532052678.arff", new ScikitLearnRegressionWrapper<>("LinearRegression()", "from sklearn.linear_model import LinearRegression\n")), //
				Arguments.of(EScikitLearnProblemType.TIME_SERIES_REGRESSION, "CMAPSS_FD001_train.arff", new ScikitLearnTimeSeriesRegressionWrapper<>(
						"make_pipeline(UniToMultivariateNumpyBasedFeatureGenerator(univariate_ts_feature_generator=UltraFastShapeletsFeatureExtractor(keep_candidates_percentage=0.1212768581941968)), SVR(C=0.27332631373580385,coef0=1.3310979035891473,degree=3,gamma=1.2,kernel=\"poly\",max_iter=5236,shrinking=True,tol=0.27707849007413665))",
						"from python_connection.feature_generation.ultra_fast_shapelets_feature_generator import UltraFastShapeletsFeatureExtractor\n"
								+ "from python_connection.feature_generation.uni_to_multi_numpy_feature_generator import UniToMultivariateNumpyBasedFeatureGenerator\n" + "from sklearn.pipeline import make_pipeline\n"
								+ "from sklearn.svm import SVR\n")), //
				Arguments.of(EScikitLearnProblemType.TIME_SERIES_FEATURE_ENGINEERING, "CMAPSS_FD001_train.arff",
						new ScikitLearnTimeSeriesFeatureEngineeringWrapper<>(
								"make_pipeline(UniToMultivariateNumpyBasedFeatureGenerator(univariate_ts_feature_generator=UltraFastShapeletsFeatureExtractor(keep_candidates_percentage=0.1212768581941968)))",
								"from python_connection.feature_generation.ultra_fast_shapelets_feature_generator import UltraFastShapeletsFeatureExtractor\n"
										+ "from python_connection.feature_generation.uni_to_multi_numpy_feature_generator import UniToMultivariateNumpyBasedFeatureGenerator\n" + "from sklearn.pipeline import make_pipeline\n")) //
		);
	}

}
