package ai.libs.automl.mlplan.multiclass.wekamlplan.sklearn;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.junit.runners.Parameterized.Parameters;

import ai.libs.automl.AbstractMLPlanBuilderTest;
import ai.libs.mlplan.multiclass.sklearn.EMLPlanScikitLearnProblemType;
import ai.libs.mlplan.multiclass.sklearn.builder.MLPlanScikitLearnBuilder;

public class MLPlanScikitLearnBuilderTest extends AbstractMLPlanBuilderTest {

	@Parameters(name = "{0}")
	public static Collection<EMLPlanScikitLearnProblemType[]> data() throws DatasetDeserializationFailedException {
		try {
			EMLPlanScikitLearnProblemType[][] data = new EMLPlanScikitLearnProblemType[EMLPlanScikitLearnProblemType.values().length][1];
			for (int i = 0; i < data.length; i++) {
				data[i][0] = EMLPlanScikitLearnProblemType.values()[i];
			}
			return Arrays.asList(data);
		} catch (Exception e) {
			throw new DatasetDeserializationFailedException(e);
		}
	}

	@Override
	public MLPlanScikitLearnBuilder getBuilder() throws IOException {
		switch ((EMLPlanScikitLearnProblemType)this.problemType) {
		case CLASSIFICATION_MULTICLASS:
			return MLPlanScikitLearnBuilder.forClassification();
		case CLASSIFICATION_MULTICLASS_UNLIMITED_LENGTH_PIPELINES:
			return MLPlanScikitLearnBuilder.forClassificationWithUnlimitedLength();
		case RUL:
			return MLPlanScikitLearnBuilder.forRUL();
		default:
			throw new IllegalArgumentException("Unknown problem type " + this.problemType);
		}
	}
}
