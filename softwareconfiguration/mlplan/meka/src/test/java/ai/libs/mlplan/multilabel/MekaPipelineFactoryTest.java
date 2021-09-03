package ai.libs.mlplan.multilabel;

import java.io.File;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.provider.Arguments;

import ai.libs.automl.AbstractComponentInstanceFactoryTest;
import ai.libs.jaicore.basic.FileUtil;
import ai.libs.jaicore.components.api.IComponentRepository;
import ai.libs.jaicore.components.serialization.ComponentSerialization;
import ai.libs.mlplan.meka.EMLPlanMekaProblemType;
import ai.libs.mlplan.meka.MekaPipelineFactory;

public class MekaPipelineFactoryTest extends AbstractComponentInstanceFactoryTest {
	private static final File SSC = FileUtil.getExistingFileWithHighestPriority(EMLPlanMekaProblemType.CLASSIFICATION_MULTILABEL.getSearchSpaceConfigFileFromResource(),
			EMLPlanMekaProblemType.CLASSIFICATION_MULTILABEL.getSearchSpaceConfigFromFileSystem());

	private static IComponentRepository repository;
	private static MekaPipelineFactory mpf;

	@BeforeAll
	public static void setup() throws Exception {
		repository = new ComponentSerialization().deserializeRepository(SSC);
		mpf = new MekaPipelineFactory();
	}

	public static Stream<Arguments> getComponentNames() {
		return getComponentNames("MLClassifier", repository);
	}

	@Override
	public MekaPipelineFactory getFactory() {
		return mpf;
	}

}