package ai.libs.mlplan.sklearnmlplan;

import java.io.IOException;
import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;

import ai.libs.automl.MLPlanResultWithMinimumQualityTest;
import ai.libs.mlplan.core.AMLPlanBuilder;
import ai.libs.mlplan.sklearn.builder.MLPlanScikitLearnBuilder;

/**
 * This test assures that ML-Plan with scikit-learn as a backend produces solutions with a minimum quality. This may
 * also notice communication issues between the Python and the Java side, since these may affect the resulting performance.
 *
 * At the moment, we check whether test performance fulfills the minimum quality requirement. However, it might be enough
 * to only check whether the internally measured loss is sufficiently good.
 *
 * @author mwever
 */
public class MLPlan4ScikitLearnResultWithMinimumQualityTest extends MLPlanResultWithMinimumQualityTest {

	@Override
	public AMLPlanBuilder getBuilder() throws IOException {
		return MLPlanScikitLearnBuilder.forClassification().withNumCpus(1);
	}

	public static Stream<Arguments> getBenchmark() {
		return Stream.of(Arguments.of("car", 40975, 0.05), //
				Arguments.of("tic-tac-toe", 50, 0.05), //
				Arguments.of("credit-g", 31, 0.3));
	}
}
