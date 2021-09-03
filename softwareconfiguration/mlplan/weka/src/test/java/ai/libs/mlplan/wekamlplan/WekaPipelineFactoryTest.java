package ai.libs.mlplan.wekamlplan;

import java.io.File;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.provider.Arguments;

import ai.libs.automl.AbstractComponentInstanceFactoryTest;
import ai.libs.jaicore.basic.FileUtil;
import ai.libs.jaicore.components.api.IComponentRepository;
import ai.libs.jaicore.components.serialization.ComponentSerialization;
import ai.libs.mlplan.weka.EMLPlanWekaProblemType;
import ai.libs.mlplan.weka.weka.WekaPipelineFactory;

public class WekaPipelineFactoryTest extends AbstractComponentInstanceFactoryTest {
	private static final File SSC = FileUtil.getExistingFileWithHighestPriority(EMLPlanWekaProblemType.CLASSIFICATION_MULTICLASS.getSearchSpaceConfigFileFromResource(),
			EMLPlanWekaProblemType.CLASSIFICATION_MULTICLASS.getSearchSpaceConfigFromFileSystem());

	private static IComponentRepository repository;
	private static WekaPipelineFactory wpf;

	@BeforeAll
	public static void setup() throws Exception {
		repository = new ComponentSerialization().deserializeRepository(SSC);
		wpf = new WekaPipelineFactory();
	}

	@Override
	public WekaPipelineFactory getFactory() {
		return wpf;
	}

	public static Stream<Arguments> getComponentNames() {
		return getComponentNames("AbstractClassifier", repository);
	}

}