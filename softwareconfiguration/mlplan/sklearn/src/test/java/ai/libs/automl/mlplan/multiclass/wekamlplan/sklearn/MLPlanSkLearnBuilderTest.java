package ai.libs.automl.mlplan.multiclass.wekamlplan.sklearn;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.junit.runners.Parameterized.Parameters;

import ai.libs.automl.AbstractMLPlanBuilderTest;
import ai.libs.mlplan.multiclass.sklearn.EMLPlanSkLearnProblemType;
import ai.libs.mlplan.multiclass.sklearn.builder.MLPlanSKLearnBuilder;

public class MLPlanSkLearnBuilderTest extends AbstractMLPlanBuilderTest {

	@Parameters(name = "{0}")
	public static Collection<EMLPlanSkLearnProblemType[]> data() throws DatasetDeserializationFailedException {
		try {
			EMLPlanSkLearnProblemType[][] data = new EMLPlanSkLearnProblemType[EMLPlanSkLearnProblemType.values().length][1];
			for (int i = 0; i < data.length; i++) {
				data[i][0] = EMLPlanSkLearnProblemType.values()[i];
			}
			return Arrays.asList(data);
		} catch (Exception e) {
			throw new DatasetDeserializationFailedException(e);
		}
	}

	@Override
	public MLPlanSKLearnBuilder getBuilder() throws IOException {
		switch ((EMLPlanSkLearnProblemType)this.problemType) {
		case CLASSIFICATION_MULTICLASS:
			return MLPlanSKLearnBuilder.forClassification();
		case CLASSIFICATION_MULTICLASS_UNLIMITED_LENGTH_PIPELINES:
			return MLPlanSKLearnBuilder.forClassificationWithUnlimitedLength();
		case RUL:
			return MLPlanSKLearnBuilder.forRUL();
		default:
			throw new IllegalArgumentException("Unknown problem type " + this.problemType);
		}
	}
}
