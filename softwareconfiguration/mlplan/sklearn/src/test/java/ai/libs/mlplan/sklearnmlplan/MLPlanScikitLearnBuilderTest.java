package ai.libs.mlplan.sklearnmlplan;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;

import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.junit.jupiter.params.provider.Arguments;

import ai.libs.automl.AbstractMLPlanBuilderTest;
import ai.libs.mlplan.core.IProblemType;
import ai.libs.mlplan.sklearn.EMLPlanScikitLearnProblemType;
import ai.libs.mlplan.sklearn.builder.MLPlanScikitLearnBuilder;

public class MLPlanScikitLearnBuilderTest extends AbstractMLPlanBuilderTest {

	public static Stream<Arguments> getProblemTypes() throws DatasetDeserializationFailedException {
		try {
			return Arrays.stream(EMLPlanScikitLearnProblemType.values()).map(Arguments::of);
		} catch (Exception e) {
			throw new DatasetDeserializationFailedException(e);
		}
	}

	@Override
	public MLPlanScikitLearnBuilder getBuilder(final IProblemType<?> problemType) throws IOException {
		switch ((EMLPlanScikitLearnProblemType)problemType) {
		case CLASSIFICATION_MULTICLASS:
			return MLPlanScikitLearnBuilder.forClassification();
		case CLASSIFICATION_MULTICLASS_UNLIMITED_LENGTH_PIPELINES:
			return MLPlanScikitLearnBuilder.forClassificationWithUnlimitedLength();
		case REGRESSION:
			return MLPlanScikitLearnBuilder.forRegression();
		case RUL:
			return MLPlanScikitLearnBuilder.forRUL();
		default:
			throw new IllegalArgumentException("Unknown problem type " + problemType);
		}
	}
}
