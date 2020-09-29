package ai.libs.mlplan.wekamlplan;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;

import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.junit.jupiter.params.provider.Arguments;

import ai.libs.automl.AbstractMLPlanBuilderTest;
import ai.libs.mlplan.core.IProblemType;
import ai.libs.mlplan.weka.EMLPlanWekaProblemType;
import ai.libs.mlplan.weka.MLPlanWekaBuilder;

public class MLPlanWekaBuilderTest extends AbstractMLPlanBuilderTest {

	public static Stream<Arguments> getProblemTypes() throws DatasetDeserializationFailedException {
		try {
			return Arrays.stream(EMLPlanWekaProblemType.values()).map(Arguments::of);
		} catch (Exception e) {
			throw new DatasetDeserializationFailedException(e);
		}
	}

	@Override
	public MLPlanWekaBuilder getBuilder(final IProblemType<?> problemType) throws IOException {
		return new MLPlanWekaBuilder((EMLPlanWekaProblemType) problemType);
	}
}
