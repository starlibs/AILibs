package ai.libs.mlplan.sklearnmlplan;

import java.io.IOException;
import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;

import ai.libs.automl.MLPlanResultWithMinimumQualityTest;
import ai.libs.mlplan.core.AMLPlanBuilder;
import ai.libs.mlplan.sklearn.builder.MLPlanScikitLearnBuilder;

public class MLPlanScikitLearnResultWithMinimumQualityTest extends MLPlanResultWithMinimumQualityTest {

	@Override
	public AMLPlanBuilder getBuilder() throws IOException {
		return MLPlanScikitLearnBuilder.forClassification();
	}

	public static Stream<Arguments> getBenchmark() {

		return Stream.of(Arguments.of("car", 40975, 0.05));
	}
}
