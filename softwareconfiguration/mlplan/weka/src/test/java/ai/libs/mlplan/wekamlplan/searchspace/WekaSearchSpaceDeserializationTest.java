package ai.libs.mlplan.wekamlplan.searchspace;

import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;

import ai.libs.softwareconfiguration.serialization.RepositoryDeserializationTest;

public class WekaSearchSpaceDeserializationTest extends RepositoryDeserializationTest {

	private static final String BASE_PATH = "automl/searchmodels/weka/";

	public static Stream<Arguments> provideRepositoriesToTest() {
		return Stream.of(
				/* Index Repositories for WEKA */
				Arguments.of(BASE_PATH + "base/index.json", 26), //
				Arguments.of(BASE_PATH + "base/smo_kernel/index.json", 4), //
				Arguments.of(BASE_PATH + "ext/twosteppipeline.json", 1), //
				Arguments.of(BASE_PATH + "meta/index.json", 12), //
				Arguments.of(BASE_PATH + "preprocessing/index.json", 13), //
				/* Full Repositories for ML-Plan with WEKA backend */
				Arguments.of(BASE_PATH + "weka-tiny.json", 4), //
				Arguments.of(BASE_PATH + "weka-reduced5.json", 23), //
				Arguments.of(BASE_PATH + "weka-reduced.json", 30), //
				Arguments.of(BASE_PATH + "weka-full.json", 52) //
				);
	}

}
