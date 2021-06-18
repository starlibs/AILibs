package ai.libs.mlplan.examples.multiclass.sklearn;

import java.io.File;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.provider.Arguments;

import ai.libs.automl.AbstractComponentInstanceFactoryTest;
import ai.libs.jaicore.basic.FileUtil;
import ai.libs.jaicore.components.api.IComponentRepository;
import ai.libs.jaicore.components.serialization.ComponentSerialization;
import ai.libs.mlplan.sklearn.EMLPlanScikitLearnProblemType;
import ai.libs.mlplan.sklearn.ScikitLearnClassifierFactory;

public class ScikitLearnClassifierFactoryTest extends AbstractComponentInstanceFactoryTest {
	private static final File SSC = FileUtil.getExistingFileWithHighestPriority(EMLPlanScikitLearnProblemType.CLASSIFICATION_MULTICLASS.getSearchSpaceConfigFileFromResource(),
			EMLPlanScikitLearnProblemType.CLASSIFICATION_MULTICLASS.getSearchSpaceConfigFromFileSystem());

	private static IComponentRepository repository;
	private static ScikitLearnClassifierFactory sklcf;

	@BeforeAll
	public static void setup() throws Exception {
		repository = new ComponentSerialization().deserializeRepository(SSC);
		sklcf = new ScikitLearnClassifierFactory();
	}

	@Override
	public ScikitLearnClassifierFactory getFactory() {
		return sklcf;
	}

	public static Stream<Arguments> getComponentNames() {
		return getComponentNames("AbstractClassifier", repository);
	}

}