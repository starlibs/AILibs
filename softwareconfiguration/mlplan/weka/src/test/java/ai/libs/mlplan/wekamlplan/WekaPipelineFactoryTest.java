package ai.libs.mlplan.wekamlplan;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.api4.java.ai.ml.core.exception.TrainingException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.common.util.concurrent.AtomicDouble;

import ai.libs.jaicore.basic.ATest;
import ai.libs.jaicore.basic.FileUtil;
import ai.libs.jaicore.basic.ValueUtil;
import ai.libs.jaicore.components.api.IComponent;
import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.api.IComponentRepository;
import ai.libs.jaicore.components.exceptions.ComponentInstantiationFailedException;
import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.jaicore.components.model.ComponentInstanceUtil;
import ai.libs.jaicore.components.model.ComponentUtil;
import ai.libs.jaicore.components.serialization.ComponentSerialization;
import ai.libs.jaicore.ml.weka.classification.learner.IWekaClassifier;
import ai.libs.jaicore.ml.weka.dataset.IWekaInstances;
import ai.libs.jaicore.ml.weka.dataset.WekaInstances;
import ai.libs.jaicore.test.LongTest;
import ai.libs.mlplan.weka.EMLPlanWekaProblemType;
import ai.libs.mlplan.weka.weka.WekaPipelineFactory;
import weka.classifiers.lazy.IBk;
import weka.core.Instances;

public class WekaPipelineFactoryTest extends ATest {
	private static final File SSC = FileUtil.getExistingFileWithHighestPriority(EMLPlanWekaProblemType.CLASSIFICATION_MULTICLASS.getSearchSpaceConfigFileFromResource(),
			EMLPlanWekaProblemType.CLASSIFICATION_MULTICLASS.getSearchSpaceConfigFromFileSystem());

	private static IComponentRepository repository;
	private static WekaPipelineFactory wpf;
	private static IWekaInstances dTrain;

	@BeforeAll
	public static void setup() throws Exception {
		repository = new ComponentSerialization().deserializeRepository(SSC);
		wpf = new WekaPipelineFactory();
		Instances data = new Instances(new FileReader(new File("testrsc/car.arff")));
		data.setClassIndex(data.numAttributes() - 1);
		dTrain = new WekaInstances(data);
	}

	@Test
	public void testValidDefaultConfigInstantiation() throws ComponentInstantiationFailedException, TrainingException, InterruptedException {
		Collection<ComponentInstance> algorithmSelections = ComponentUtil.getAllAlgorithmSelectionInstances("MLClassifier", repository);
		List<ComponentInstance> list = new ArrayList<>(algorithmSelections);

		double currentPercentage = 0.0;
		double step = 0.05;
		for (int i = 0; i < list.size(); i++) {
			IWekaClassifier c = wpf.getComponentInstantiation(list.get(i));
			if ((double) i / list.size() >= currentPercentage) {
				System.out.println("Current state: " + (currentPercentage * 100) + "%");
				currentPercentage += step;
			}
		}
	}

	public static Stream<Arguments> getComponentNames() {
		List<ComponentInstance> list = new ArrayList<>(ComponentUtil.getAllAlgorithmSelectionInstances("AbstractClassifier", repository));
		Set<String> names = list.stream().map(ci -> ci.getComponent().getName()).collect(Collectors.toSet());
		return names.stream().map(name -> Arguments.of(name, list.stream().filter(ci -> ci.getComponent().getName().equals(name)).collect(Collectors.toList())));
	}

	@ParameterizedTest(name = "Testing component instances of type {0}")
	@MethodSource("getComponentNames")
	@LongTest
	public void testValidRandomConfigInstantiation(final String name, final List<ComponentInstance> instancesToTest) throws ComponentInstantiationFailedException, TrainingException, InterruptedException {
		this.logger.info("Testing {} component instances.", instancesToTest.size());

		AtomicInteger count = new AtomicInteger(0);
		AtomicDouble currentPercentage = new AtomicDouble(0.0);
		double step = 0.01;
		IntStream.range(0, instancesToTest.size()).parallel().forEach(i -> {
			this.logger.trace("Checking {}", ComponentInstanceUtil.getComponentInstanceAsComponentNames(instancesToTest.get(i)));
			int currentI = i;
			boolean success = true;
			for (int j = 0; j < 100 && success; j++) {
				IComponentInstance randomConfig = ComponentUtil.getRandomParametrization(instancesToTest.get(currentI), new Random(j));
				try {
					wpf.getComponentInstantiation(randomConfig);
				} catch (ComponentInstantiationFailedException e) {
					this.logger.warn("Failed to instantiate component instance of {}", ComponentInstanceUtil.toRecursiveConstructorString(randomConfig), e);
					success = false;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			assertTrue(success, "Could not find a realization of component " + instancesToTest.get(currentI));

			double currentState = (double) count.incrementAndGet() / instancesToTest.size();
			boolean stateChanged = false;
			while (currentState >= currentPercentage.get()) {
				currentPercentage.addAndGet(step);
				stateChanged = true;
			}
			if (stateChanged) {
				this.logger.debug("Current state: {}%", ValueUtil.round(currentPercentage.get() * 100, 1));
			}
		});
	}
}
