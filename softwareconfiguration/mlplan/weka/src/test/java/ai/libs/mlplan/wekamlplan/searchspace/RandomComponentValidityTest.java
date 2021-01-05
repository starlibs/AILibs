package ai.libs.mlplan.wekamlplan.searchspace;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.execution.ILearnerRunReport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.fasterxml.jackson.core.JsonProcessingException;

import ai.libs.jaicore.basic.ResourceFile;
import ai.libs.jaicore.components.api.IComponent;
import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.api.IComponentRepository;
import ai.libs.jaicore.components.exceptions.ComponentInstantiationFailedException;
import ai.libs.jaicore.components.model.ComponentInstanceUtil;
import ai.libs.jaicore.components.model.ComponentUtil;
import ai.libs.jaicore.components.serialization.ComponentSerialization;
import ai.libs.jaicore.ml.core.dataset.serialization.OpenMLDatasetReader;
import ai.libs.jaicore.ml.core.evaluation.evaluator.SupervisedLearnerExecutor;
import ai.libs.jaicore.ml.weka.classification.learner.IWekaClassifier;
import ai.libs.jaicore.test.MediumParameterizedTest;
import ai.libs.mlplan.weka.weka.WekaPipelineFactory;

public class RandomComponentValidityTest {

	private static ILabeledDataset<ILabeledInstance> classificationDataset;
	private static ILabeledDataset<ILabeledInstance> regressionDataset;
	private static final List<String> CHECK_COMPONENTS = Arrays.asList();

	private static IComponentRepository repo;
	private static WekaPipelineFactory factory;

	private static final int NUM_PARAMETRIZATIONS = 50;

	private static final boolean TEST_CLASSIFICATION = false;
	private static ILabeledDataset<ILabeledInstance> dataset;

	@BeforeAll
	public static void setup() throws DatasetDeserializationFailedException, IOException {
		if (TEST_CLASSIFICATION) {
			dataset = OpenMLDatasetReader.deserializeDataset(50);
			repo = new ComponentSerialization().deserializeRepository(new ResourceFile("automl/searchmodels/weka/weka-full.json"));
		} else {
			dataset = OpenMLDatasetReader.deserializeDataset(232);
			repo = new ComponentSerialization().deserializeRepository(new ResourceFile("automl/searchmodels/weka/weka-full-regression.json"));
		}
		factory = new WekaPipelineFactory();
	}

	public static Stream<Arguments> getComponentsToTestRandomConfigurations() {
		List<Arguments> argumentsList = ComponentUtil.getComponentsProvidingInterface(repo, "Regressor").stream().filter(x -> !CHECK_COMPONENTS.contains(x.getName())).map(x -> Arguments.of(x.getName(), NUM_PARAMETRIZATIONS))
				.collect(Collectors.toList());
		return argumentsList.stream();
	}

	@Disabled
	@MediumParameterizedTest
	@MethodSource("getComponentsToTestRandomConfigurations")
	public void testRandomConfigurationsInstantiation(final String componentName, final int numParameterizations) throws IOException, ComponentInstantiationFailedException {
		IComponent compToTest = repo.getComponent(componentName);
		System.out.println("Test " + numParameterizations + " random configuration of component: " + compToTest.getName());
		if (compToTest.getParameters().isEmpty() && compToTest.getRequiredInterfaces().isEmpty()) {
			return;
		}

		for (int i = 0; i < numParameterizations; i++) {
			IComponentInstance ci = ComponentUtil.getRandomParameterizationOfComponent(compToTest, new Random(i));
			if (componentName.toLowerCase().contains("randomforest")) {
				if (Integer.parseInt(ci.getParameterValues().get("N")) > 8) {
					continue;
				}
			}
			try {
				IWekaClassifier learner = factory.getComponentInstantiation(ci);
				ILearnerRunReport report = new SupervisedLearnerExecutor().execute(learner, regressionDataset, regressionDataset);
			} catch (Exception e) {
				System.out.println(ComponentInstanceUtil.getComponentInstanceString(ci));
				e.printStackTrace();
				fail(e);
			}

			if ((i + 1) % 10 == 0) {
				System.out.println((i + 1) + " components tested");
			}
		}
	}

	public static Stream<Arguments> getComponentsToTestRandomInstantiations() {
		return ComponentUtil.getComponentsProvidingInterface(repo, "MetaRegressor").stream().map(x -> Arguments.of(x.getName(), NUM_PARAMETRIZATIONS));
	}

	@MediumParameterizedTest
	@MethodSource("getComponentsToTestRandomInstantiations")
	public void testRandomInstantiation(final String componentName, final int numInstantiations) throws JsonProcessingException {
		IComponent compToTest = repo.getComponent(componentName);
		System.out.println("Test " + numInstantiations + " random instantiation of component: " + compToTest);
		if (compToTest.getParameters().isEmpty() && compToTest.getRequiredInterfaces().isEmpty()) {
			return;
		}

		for (int i = 0; i < numInstantiations; i++) {
			IComponentInstance ci = ComponentUtil.getRandomInstantiationOfComponent(compToTest, repo, new Random(i));
			if (componentName.toLowerCase().contains("randomforest")) {
				if (Integer.parseInt(ci.getParameterValues().get("N")) > regressionDataset.size()) {
					continue;
				}
			}
			try {
				IWekaClassifier learner = factory.getComponentInstantiation(ci);
				ILearnerRunReport report = new SupervisedLearnerExecutor().execute(learner, regressionDataset, regressionDataset);
			} catch (Exception e) {
				System.out.println("Failed for: " + ComponentInstanceUtil.getComponentInstanceString(ci));
				e.printStackTrace();
				fail(e);
			}

			if ((i + 1) % 10 == 0) {
				System.out.println((i + 1) + " components tested");
			}
		}
	}

}
