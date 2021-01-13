package ai.libs.mlplan.wekamlplan.searchspace;

import static org.junit.jupiter.api.Assertions.assertTrue;
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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.fasterxml.jackson.core.JsonProcessingException;

import ai.libs.jaicore.basic.ATest;
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
import ai.libs.jaicore.test.LongParameterizedTest;
import ai.libs.mlplan.weka.weka.WekaPipelineFactory;

public class RandomComponentValidityTest extends ATest {

	private static ILabeledDataset<ILabeledInstance> classificationDataset;
	private static ILabeledDataset<ILabeledInstance> regressionDataset;
	private static final List<String> CHECK_COMPONENTS = Arrays.asList();

	private static IComponentRepository repoClassification;
	private static IComponentRepository repoRegression;
	private static WekaPipelineFactory factory;

	private static final int NUM_PARAMETRIZATIONS = 50;

	@BeforeAll
	public static void setup() throws DatasetDeserializationFailedException, IOException {
		classificationDataset = OpenMLDatasetReader.deserializeDataset(50);
		repoClassification = new ComponentSerialization().deserializeRepository(new ResourceFile("automl/searchmodels/weka/weka-full.json"));
		regressionDataset = OpenMLDatasetReader.deserializeDataset(232);
		repoRegression = new ComponentSerialization().deserializeRepository(new ResourceFile("automl/searchmodels/weka/weka-full-regression.json"));
		factory = new WekaPipelineFactory();
	}

	public static Stream<Arguments> getComponentsToTestRandomConfigurations() {
		List<Arguments> argumentsList = ComponentUtil.getComponentsProvidingInterface(repoRegression, "Regressor").stream().filter(x -> !CHECK_COMPONENTS.contains(x.getName()))
				.map(x -> Arguments.of(regressionDataset, repoRegression, x.getName(), NUM_PARAMETRIZATIONS)).collect(Collectors.toList());
		argumentsList.addAll(ComponentUtil.getComponentsProvidingInterface(repoClassification, "Classifier").stream().filter(x -> !CHECK_COMPONENTS.contains(x.getName()))
				.map(x -> Arguments.of(classificationDataset, repoClassification, x.getName(), NUM_PARAMETRIZATIONS)).collect(Collectors.toList()));
		return argumentsList.stream();
	}

	@Disabled("Fails because of illegal configurations for the given data")
	@LongParameterizedTest
	@MethodSource("getComponentsToTestRandomConfigurations")
	public void testRandomConfigurationsInstantiation(final ILabeledDataset<ILabeledInstance> dataset, final IComponentRepository repo, final String componentName, final int numParameterizations)
			throws IOException, ComponentInstantiationFailedException {
		IComponent compToTest = repo.getComponent(componentName);
		this.getLogger().info("Test {} random configuration of component: {}", numParameterizations, compToTest.getName());
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
				new SupervisedLearnerExecutor().execute(learner, dataset, dataset);
			} catch (Exception e) {
				this.getLogger().error("Could not build or execute {}", ComponentInstanceUtil.getComponentInstanceString(ci), e);
				e.printStackTrace();
				fail(e);
			}

			if ((i + 1) % 10 == 0) {
				this.getLogger().info("{} components of {} evaluated", (i + 1), compToTest);
			}
		}
	}

	public static Stream<Arguments> getComponentsToTestRandomInstantiations() {
		List<Arguments> argumentList = ComponentUtil.getComponentsProvidingInterface(repoRegression, "MetaRegressor").stream().map(x -> Arguments.of(regressionDataset, repoRegression, x.getName(), NUM_PARAMETRIZATIONS))
				.collect(Collectors.toList());
		argumentList.addAll(
				ComponentUtil.getComponentsProvidingInterface(repoClassification, "MetaClassifier").stream().map(x -> Arguments.of(classificationDataset, repoClassification, x.getName(), NUM_PARAMETRIZATIONS)).collect(Collectors.toList()));
		return argumentList.stream();
	}

	@Disabled("Fails because of illegal configurations for the given data")
	@LongParameterizedTest
	@MethodSource("getComponentsToTestRandomInstantiations")
	public void testRandomInstantiation(final ILabeledDataset<ILabeledInstance> dataset, final IComponentRepository repo, final String componentName, final int numInstantiations) throws JsonProcessingException {
		IComponent compToTest = repo.getComponent(componentName);
		this.getLogger().info("Test {} random instantiation of component: {}", numInstantiations, compToTest);
		if (compToTest.getParameters().isEmpty() && compToTest.getRequiredInterfaces().isEmpty()) {
			return;
		}

		boolean allSucceeded = true;
		for (int i = 0; i < numInstantiations; i++) {
			IComponentInstance ci = ComponentUtil.getRandomInstantiationOfComponent(compToTest, repo, new Random(i));
			if (componentName.toLowerCase().contains("randomforest")) {
				if (Integer.parseInt(ci.getParameterValues().get("N")) > regressionDataset.size()) {
					continue;
				}
			}
			try {
				IWekaClassifier learner = factory.getComponentInstantiation(ci);
				new SupervisedLearnerExecutor().execute(learner, dataset, dataset);
			} catch (Exception e) {
				this.getLogger().error("Could not build or execute {}", ComponentInstanceUtil.getComponentInstanceString(ci), e);
				allSucceeded = false;
				e.printStackTrace();
				fail(e);
			}

			if ((i + 1) % 10 == 0) {
				this.getLogger().info("{} components of {} evaluated", (i + 1), compToTest);
			}
		}

		assertTrue(allSucceeded, "Successfully tested random instantiations of " + componentName);
	}

}
