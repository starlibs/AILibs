package ai.libs.mlplan.sklearnmlplan.searchspace;

import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;

import ai.libs.softwareconfiguration.serialization.RepositoryDeserializationTest;

public class ScikitLearnSearchSpaceDeserializationTest extends RepositoryDeserializationTest {

	private static final String BASE_PATH = "automl/searchmodels/sklearn/";

	public static Stream<Arguments> provideRepositoriesToTest() {
		return Stream.of(
				/* Index Repositories for WEKA */
				Arguments.of(BASE_PATH + "classification/base/index.json", 16), //
				Arguments.of(BASE_PATH + "classification/ext/index.json", 3), //
				Arguments.of(BASE_PATH + "datacleaner/index.json", 1), //
				Arguments.of(BASE_PATH + "preprocessing/index.json", 20), //
				Arguments.of(BASE_PATH + "regression/base/index.json", 22), //
				Arguments.of(BASE_PATH + "regression/ext/rulpipeline.json", 1), //
				Arguments.of(BASE_PATH + "regression/ext/twosteppipeline.json", 1), //
				Arguments.of(BASE_PATH + "timeseries/index.json", 2), //

				/* Full Repositories for ML-Plan with WEKA backend */
				Arguments.of(BASE_PATH + "sklearn-classification-ul.json", 41), //
				Arguments.of(BASE_PATH + "sklearn-classification.json", 37), //
				Arguments.of(BASE_PATH + "sklearn-regression.json", 43), //
				Arguments.of(BASE_PATH + "sklearn-rul.json", 46) //
		);
	}

}
