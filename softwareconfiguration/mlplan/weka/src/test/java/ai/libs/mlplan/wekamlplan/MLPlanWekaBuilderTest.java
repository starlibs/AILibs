package ai.libs.mlplan.wekamlplan;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.junit.runners.Parameterized.Parameters;

import ai.libs.automl.AbstractMLPlanBuilderTest;
import ai.libs.mlplan.multiclass.wekamlplan.EMLPlanWekaProblemType;
import ai.libs.mlplan.multiclass.wekamlplan.MLPlanWekaBuilder;

public class MLPlanWekaBuilderTest extends AbstractMLPlanBuilderTest {

	@Parameters(name = "{0}")
	public static Collection<EMLPlanWekaProblemType[]> data() throws DatasetDeserializationFailedException {
		try {
			EMLPlanWekaProblemType[][] data = new EMLPlanWekaProblemType[EMLPlanWekaProblemType.values().length][1];
			for (int i = 0; i < data.length; i++) {
				data[i][0] = EMLPlanWekaProblemType.values()[i];
			}
			return Arrays.asList(data);
		} catch (Exception e) {
			throw new DatasetDeserializationFailedException(e);
		}
	}

	@Override
	public MLPlanWekaBuilder getBuilder() throws IOException {
		return new MLPlanWekaBuilder((EMLPlanWekaProblemType)this.problemType);
	}
}
