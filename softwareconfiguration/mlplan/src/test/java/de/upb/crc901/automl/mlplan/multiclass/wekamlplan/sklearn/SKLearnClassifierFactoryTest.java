package de.upb.crc901.automl.mlplan.multiclass.wekamlplan.sklearn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import de.upb.crc901.mlplan.multiclass.wekamlplan.sklearn.SKLearnClassifierFactory;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.model.ComponentUtil;
import hasco.serialization.ComponentLoader;
import jaicore.ml.WekaUtil;
import weka.core.Instances;

/**
 * This tests checks whether the classifier factory for scikit learn classifiers works as expected.
 * More specifically it is checked whether the factory is capable of translating different ComponentInstances
 * into executable SKLearn classifiers.
 *
 * @author wever
 */
public class SKLearnClassifierFactoryTest {

	/* Constants for the tests */
	private static final File TEST_DATASET = new File("testrsc/car.arff");
	private static final double TEST_SPLIT_RATIO = .7;
	private static final long SEED = 0;

	/* Components for testing the factory */
	private static final File COMPONENT_REPO = new File("resources/automl/searchmodels/sklearn/ml-plan-ul.json");
	private static ComponentLoader cl;

	private static final String CLASSIFIER_NAME = "sklearn.ensemble.RandomForestClassifier";
	private static final String CLASSIFIER_IMPORT = "from sklearn.ensemble import RandomForestClassifier\n";

	private static final String PREPROCESSOR_NAME = "sklearn.kernel_approximation.Nystroem";
	private static final String PREPROCESSOR_IMPORT = "from sklearn.kernel_approximation import Nystroem\n";

	private static final String MAKE_PIPELINE_NAME = "sklearn.pipeline.make_pipeline";
	private static final String MAKE_PIPELINE_IMPORT = "from sklearn.pipeline import make_pipeline\n";

	private static final String MAKE_UNION_NAME = "sklearn.pipeline.make_union";
	private static final String MAKE_UNION_IMPORT = "from sklearn.pipeline import make_union\n";

	private static final String MAKE_FORWARD_NAME = "mlplan.util.model.make_forward";

	private static final String EXPECTED_CLASSIFIER = "RandomForestClassifier(max_features=0.53,criterion=\"gini\",min_samples_split=11.0,n_estimators=100.0,bootstrap=True,min_samples_leaf=11.0)";
	private static final String EXPECTED_PREPROCESSOR = "Nystroem(n_components=6.0,kernel=\"rbf\",gamma=0.505)";
	private static final String EXPECTED_TWO_STEP_PIPE = "make_pipeline(" + EXPECTED_PREPROCESSOR + "," + EXPECTED_CLASSIFIER + ")";
	private static final String EXPECTED_SIMPLE_MAKE_UNION_PIPE = "make_pipeline(make_union(" + EXPECTED_PREPROCESSOR + "," + EXPECTED_PREPROCESSOR + ")," + EXPECTED_CLASSIFIER + ")";
	private static final String EXPECTED_SIMPLE_MAKE_FORWARD_PIPE = "make_pipeline(" + EXPECTED_PREPROCESSOR + "," + EXPECTED_PREPROCESSOR + "," + EXPECTED_CLASSIFIER + ")";

	/* Objects for tests */
	private static List<Instances> stratSplit;
	private static SKLearnClassifierFactory factory;

	@BeforeClass
	public static void setup() throws FileNotFoundException, IOException {
		/* load dataset */
		Instances data = new Instances(new FileReader(TEST_DATASET));
		data.setClassIndex(data.numAttributes() - 1);
		stratSplit = WekaUtil.getStratifiedSplit(data, SEED, TEST_SPLIT_RATIO);

		cl = new ComponentLoader(COMPONENT_REPO);

		/* init factory */
		factory = new SKLearnClassifierFactory();
	}

	@Test
	public void testExtractSingleClassifier() throws Exception {
		Component classifierComponent = cl.getComponentWithName(CLASSIFIER_NAME);
		Set<String> importSet = new HashSet<>();
		String constructInstruction = factory.extractSKLearnConstructInstruction(ComponentUtil.defaultParameterizationOfComponent(classifierComponent), importSet);

		assertTrue("Import set does not contain the import for the classifier", importSet.contains(CLASSIFIER_IMPORT));
		assertEquals("The extracted construct instruction deviates from the expected value", EXPECTED_CLASSIFIER, constructInstruction);
	}

	@Test
	public void testExtractTwoStepPipeline() throws Exception {
		Component makePipe = cl.getComponentWithName(MAKE_PIPELINE_NAME);
		Component preprocessorComponent = cl.getComponentWithName(PREPROCESSOR_NAME);
		Component classifierComponent = cl.getComponentWithName(CLASSIFIER_NAME);

		ComponentInstance pipe = ComponentUtil.defaultParameterizationOfComponent(makePipe);
		pipe.getSatisfactionOfRequiredInterfaces().put("preprocessor", ComponentUtil.defaultParameterizationOfComponent(preprocessorComponent));
		pipe.getSatisfactionOfRequiredInterfaces().put("classifier", ComponentUtil.defaultParameterizationOfComponent(classifierComponent));

		Set<String> importSet = new HashSet<>();
		String actualConstructInstruction = factory.extractSKLearnConstructInstruction(pipe, importSet);

		assertEquals("Unexpected number of imports in the import set", 3, importSet.size());
		assertTrue("Not all necessary imports occurred in the import set.", importSet.containsAll(Arrays.asList(PREPROCESSOR_IMPORT, CLASSIFIER_IMPORT, MAKE_PIPELINE_IMPORT)));
		assertEquals("The actual construct instruction does not match the expected.", EXPECTED_TWO_STEP_PIPE, actualConstructInstruction);
	}

	@Test
	public void testExtractMakeUnionPipeline() throws Exception {
		Component makePipe = cl.getComponentWithName(MAKE_PIPELINE_NAME);
		Component preprocessorComponent = cl.getComponentWithName(PREPROCESSOR_NAME);
		Component classifierComponent = cl.getComponentWithName(CLASSIFIER_NAME);
		Component makeUnion = cl.getComponentWithName(MAKE_UNION_NAME);

		ComponentInstance union = ComponentUtil.defaultParameterizationOfComponent(makeUnion);
		for (String prep : new String[] { "p1", "p2" }) {
			union.getSatisfactionOfRequiredInterfaces().put(prep, ComponentUtil.defaultParameterizationOfComponent(preprocessorComponent));
		}

		ComponentInstance pipe = ComponentUtil.defaultParameterizationOfComponent(makePipe);
		pipe.getSatisfactionOfRequiredInterfaces().put("preprocessor", union);
		pipe.getSatisfactionOfRequiredInterfaces().put("classifier", ComponentUtil.defaultParameterizationOfComponent(classifierComponent));

		Set<String> importSet = new HashSet<>();
		String actualConstructInstruction = factory.extractSKLearnConstructInstruction(pipe, importSet);

		assertEquals("Unexpected number of imports in the import set", 4, importSet.size());
		assertTrue("Not all necessary imports occurred in the import set.", importSet.containsAll(Arrays.asList(MAKE_UNION_IMPORT, PREPROCESSOR_IMPORT, CLASSIFIER_IMPORT, MAKE_PIPELINE_IMPORT)));
		assertEquals("The actual construct instruction does not match the expected", EXPECTED_SIMPLE_MAKE_UNION_PIPE, actualConstructInstruction);
	}

	@Test
	public void testExtractMakeForwardPipeline() throws Exception {
		Component makePipe = cl.getComponentWithName(MAKE_PIPELINE_NAME);
		Component preprocessorComponent = cl.getComponentWithName(PREPROCESSOR_NAME);
		Component classifierComponent = cl.getComponentWithName(CLASSIFIER_NAME);
		Component makeForward = cl.getComponentWithName(MAKE_FORWARD_NAME);

		ComponentInstance forward = ComponentUtil.defaultParameterizationOfComponent(makeForward);
		for (String prep : new String[] { "base", "source" }) {
			forward.getSatisfactionOfRequiredInterfaces().put(prep, ComponentUtil.defaultParameterizationOfComponent(preprocessorComponent));
		}

		ComponentInstance pipe = ComponentUtil.defaultParameterizationOfComponent(makePipe);
		pipe.getSatisfactionOfRequiredInterfaces().put("preprocessor", forward);
		pipe.getSatisfactionOfRequiredInterfaces().put("classifier", ComponentUtil.defaultParameterizationOfComponent(classifierComponent));

		Set<String> importSet = new HashSet<>();
		String actualConstructInstruction = factory.extractSKLearnConstructInstruction(pipe, importSet);

		assertEquals("Unexpected number of imports in the import set", 3, importSet.size());
		assertTrue("Not all necessary imports occurred in the import set.", importSet.containsAll(Arrays.asList(PREPROCESSOR_IMPORT, CLASSIFIER_IMPORT, MAKE_PIPELINE_IMPORT)));
		assertEquals("The actual construct instruction does not match the expected", EXPECTED_SIMPLE_MAKE_FORWARD_PIPE, actualConstructInstruction);
	}

}
