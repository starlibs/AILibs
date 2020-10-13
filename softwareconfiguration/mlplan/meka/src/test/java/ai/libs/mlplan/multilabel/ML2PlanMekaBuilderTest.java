package ai.libs.mlplan.multilabel;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;

import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.junit.jupiter.params.provider.Arguments;

import ai.libs.automl.AbstractMLPlanBuilderTest;
import ai.libs.mlplan.core.IProblemType;
import ai.libs.mlplan.meka.EMLPlanMekaProblemType;
import ai.libs.mlplan.meka.ML2PlanMekaBuilder;

public class ML2PlanMekaBuilderTest extends AbstractMLPlanBuilderTest {

	public static Stream<Arguments> getProblemTypes() throws DatasetDeserializationFailedException {
		try {
			return Arrays.stream(EMLPlanMekaProblemType.values()).map(Arguments::of);
		} catch (Exception e) {
			throw new DatasetDeserializationFailedException(e);
		}
	}

	@Override
	public ML2PlanMekaBuilder getBuilder(final IProblemType<?> problemType) throws IOException {
		return new ML2PlanMekaBuilder((EMLPlanMekaProblemType)problemType);
	}
}
