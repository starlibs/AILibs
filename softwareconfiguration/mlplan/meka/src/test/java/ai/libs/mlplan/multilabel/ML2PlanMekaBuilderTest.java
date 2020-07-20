package ai.libs.mlplan.multilabel;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.junit.runners.Parameterized.Parameters;

import ai.libs.automl.AbstractMLPlanBuilderTest;
import ai.libs.mlplan.multilabel.mekamlplan.EMLPlanMekaProblemType;
import ai.libs.mlplan.multilabel.mekamlplan.ML2PlanMekaBuilder;

public class ML2PlanMekaBuilderTest extends AbstractMLPlanBuilderTest {

	@Parameters(name = "{0}")
	public static Collection<EMLPlanMekaProblemType[]> data() throws DatasetDeserializationFailedException {
		try {
			EMLPlanMekaProblemType[][] data = new EMLPlanMekaProblemType[EMLPlanMekaProblemType.values().length][1];
			for (int i = 0; i < data.length; i++) {
				data[i][0] = EMLPlanMekaProblemType.values()[i];
			}
			return Arrays.asList(data);
		} catch (Exception e) {
			throw new DatasetDeserializationFailedException(e);
		}
	}

	@Override
	public ML2PlanMekaBuilder getBuilder() throws IOException {
		return new ML2PlanMekaBuilder((EMLPlanMekaProblemType)this.problemType);
	}
}
