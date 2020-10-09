package ai.libs.mlplan.sklearnmlplan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.api4.java.ai.ml.core.dataset.splitter.SplitFailedException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ai.libs.jaicore.components.api.IComponent;
import ai.libs.jaicore.components.api.IComponentRepository;
import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.jaicore.components.model.ComponentUtil;
import ai.libs.jaicore.components.serialization.ComponentSerialization;
import ai.libs.jaicore.ml.weka.WekaUtil;
import ai.libs.mlplan.sklearn.AScikitLearnLearnerFactory;
import ai.libs.mlplan.sklearn.ScikitLearnClassifierFactory;
import weka.core.Instances;

/**
 * This tests checks whether the classifier factory for scikit learn classifiers works as expected.
 * More specifically it is checked whether the factory is capable of translating different ComponentInstances
 * into executable SKLearn classifiers.
 *
 * @author wever
 */
public class ScikitLearnClassifierFactoryTest {

	/* Constants for the tests */
	private static final File TEST_DATASET = new File("testrsc/car.arff");
	private static final double TEST_SPLIT_RATIO = .7;
	private static final long SEED = 0;

	/* Components for testing the factory */
	private static final File COMPONENT_REPO = new File("resources/automl/searchmodels/sklearn/sklearn-classification-ul.json");
	private static ComponentSerialization serializer = new ComponentSerialization();
	private static IComponentRepository repository;

	private static final String CLASSIFIER_NAME = "sklearn.ensemble.RandomForestClassifier";
	private static final String CLASSIFIER_IMPORT = "from sklearn.ensemble import RandomForestClassifier\n";

	private static final String PREPROCESSOR_NAME = "sklearn.kernel_approximation.Nystroem";
	private static final String PREPROCESSOR_IMPORT = "from sklearn.kernel_approximation import Nystroem\n";

	private static final String MAKE_PIPELINE_NAME = "sklearn.pipeline.make_pipeline";
	private static final String MAKE_PIPELINE_IMPORT = "from sklearn.pipeline import make_pipeline\n";

	private static final String MAKE_UNION_NAME = "sklearn.pipeline.make_union";
	private static final String MAKE_UNION_IMPORT = "from sklearn.pipeline import make_union\n";

	private static final String MAKE_FORWARD_NAME = "mlplan.util.model.make_forward";

	private static final String EXPECTED_CLASSIFIER = "RandomForestClassifier(max_features=\"auto\",criterion=\"gini\",min_samples_split=11,bootstrap=True,n_estimators=100,min_samples_leaf=11)";
	private static final String EXPECTED_PREPROCESSOR = "Nystroem(n_components=6,kernel=\"rbf\",gamma=0.505)";
	private static final String EXPECTED_TWO_STEP_PIPE = "make_pipeline(" + EXPECTED_PREPROCESSOR + "," + EXPECTED_CLASSIFIER + ")";
	private static final String EXPECTED_SIMPLE_MAKE_UNION_PIPE = "make_pipeline(make_union(" + EXPECTED_PREPROCESSOR + "," + EXPECTED_PREPROCESSOR + ")," + EXPECTED_CLASSIFIER + ")";
	private static final String EXPECTED_SIMPLE_MAKE_FORWARD_PIPE = "make_pipeline(" + EXPECTED_PREPROCESSOR + "," + EXPECTED_PREPROCESSOR + "," + EXPECTED_CLASSIFIER + ")";

	/* Objects for tests */
	private static List<Instances> stratSplit;
	private static AScikitLearnLearnerFactory factory;

	@BeforeAll
	public static void setup() throws FileNotFoundException, IOException, InterruptedException, SplitFailedException {
		/* load dataset */
		Instances data = new Instances(new FileReader(TEST_DATASET));
		data.setClassIndex(data.numAttributes() - 1);
		stratSplit = WekaUtil.getStratifiedSplit(data, SEED, TEST_SPLIT_RATIO);

		repository = serializer.deserializeRepository(COMPONENT_REPO);

		/* init factory */
		factory = new ScikitLearnClassifierFactory();
	}

	@Test
	public void testExtractSingleClassifier() throws Exception {
		IComponent classifierComponent = repository.getComponent(CLASSIFIER_NAME);
		Set<String> importSet = new HashSet<>();
		String constructInstruction = factory.extractSKLearnConstructInstruction(ComponentUtil.getDefaultParameterizationOfComponent(classifierComponent), importSet);

		assertTrue(importSet.contains(CLASSIFIER_IMPORT), "Import set does not contain the import for the classifier");
		assertEquals(EXPECTED_CLASSIFIER, constructInstruction, "The extracted construct instruction deviates from the expected value");
	}

	@Test
	public void testExtractTwoStepPipeline() throws Exception {
		IComponent makePipe = repository.getComponent(MAKE_PIPELINE_NAME);
		IComponent preprocessorComponent = repository.getComponent(PREPROCESSOR_NAME);
		IComponent classifierComponent = repository.getComponent(CLASSIFIER_NAME);

		ComponentInstance pipe = ComponentUtil.getDefaultParameterizationOfComponent(makePipe);
		pipe.getSatisfactionOfRequiredInterfaces().put("preprocessor", Arrays.asList(ComponentUtil.getDefaultParameterizationOfComponent(preprocessorComponent)));
		pipe.getSatisfactionOfRequiredInterfaces().put("classifier", Arrays.asList(ComponentUtil.getDefaultParameterizationOfComponent(classifierComponent)));

		Set<String> importSet = new HashSet<>();
		String actualConstructInstruction = factory.extractSKLearnConstructInstruction(pipe, importSet);

		assertEquals(3, importSet.size(), "Unexpected number of imports in the import set");
		assertTrue(importSet.containsAll(Arrays.asList(PREPROCESSOR_IMPORT, CLASSIFIER_IMPORT, MAKE_PIPELINE_IMPORT)), "Not all necessary imports occurred in the import set.");
		assertEquals(EXPECTED_TWO_STEP_PIPE, actualConstructInstruction, "The actual construct instruction does not match the expected.");
	}

	@Test
	public void testExtractMakeUnionPipeline() throws Exception {
		IComponent makePipe = repository.getComponent(MAKE_PIPELINE_NAME);
		IComponent preprocessorComponent = repository.getComponent(PREPROCESSOR_NAME);
		IComponent classifierComponent = repository.getComponent(CLASSIFIER_NAME);
		IComponent makeUnion = repository.getComponent(MAKE_UNION_NAME);

		ComponentInstance union = ComponentUtil.getDefaultParameterizationOfComponent(makeUnion);
		for (String prep : new String[] { "p1", "p2" }) {
			union.getSatisfactionOfRequiredInterfaces().put(prep, Arrays.asList(ComponentUtil.getDefaultParameterizationOfComponent(preprocessorComponent)));
		}

		ComponentInstance pipe = ComponentUtil.getDefaultParameterizationOfComponent(makePipe);
		pipe.getSatisfactionOfRequiredInterfaces().put("preprocessor", Arrays.asList(union));
		pipe.getSatisfactionOfRequiredInterfaces().put("classifier", Arrays.asList(ComponentUtil.getDefaultParameterizationOfComponent(classifierComponent)));

		Set<String> importSet = new HashSet<>();
		String actualConstructInstruction = factory.extractSKLearnConstructInstruction(pipe, importSet);

		assertEquals(4, importSet.size(), "Unexpected number of imports in the import set");
		assertTrue(importSet.containsAll(Arrays.asList(MAKE_UNION_IMPORT, PREPROCESSOR_IMPORT, CLASSIFIER_IMPORT, MAKE_PIPELINE_IMPORT)), "Not all necessary imports occurred in the import set.");
		assertEquals(EXPECTED_SIMPLE_MAKE_UNION_PIPE, actualConstructInstruction, "The actual construct instruction does not match the expected");
	}

	@Test
	public void testExtractMakeForwardPipeline() throws Exception {
		IComponent makePipe = repository.getComponent(MAKE_PIPELINE_NAME);
		IComponent preprocessorComponent = repository.getComponent(PREPROCESSOR_NAME);
		IComponent classifierComponent = repository.getComponent(CLASSIFIER_NAME);
		IComponent makeForward = repository.getComponent(MAKE_FORWARD_NAME);

		ComponentInstance forward = ComponentUtil.getDefaultParameterizationOfComponent(makeForward);
		for (String prep : new String[] { "base", "source" }) {
			forward.getSatisfactionOfRequiredInterfaces().put(prep, Arrays.asList(ComponentUtil.getDefaultParameterizationOfComponent(preprocessorComponent)));
		}

		ComponentInstance pipe = ComponentUtil.getDefaultParameterizationOfComponent(makePipe);
		pipe.getSatisfactionOfRequiredInterfaces().put("preprocessor", Arrays.asList(forward));
		pipe.getSatisfactionOfRequiredInterfaces().put("classifier", Arrays.asList(ComponentUtil.getDefaultParameterizationOfComponent(classifierComponent)));

		Set<String> importSet = new HashSet<>();
		String actualConstructInstruction = factory.extractSKLearnConstructInstruction(pipe, importSet);

		assertEquals(3, importSet.size(), "Unexpected number of imports in the import set");
		assertTrue(importSet.containsAll(Arrays.asList(PREPROCESSOR_IMPORT, CLASSIFIER_IMPORT, MAKE_PIPELINE_IMPORT)), "Not all necessary imports occurred in the import set.");
		assertEquals(EXPECTED_SIMPLE_MAKE_FORWARD_PIPE, actualConstructInstruction, "The actual construct instruction does not match the expected");
	}

}
