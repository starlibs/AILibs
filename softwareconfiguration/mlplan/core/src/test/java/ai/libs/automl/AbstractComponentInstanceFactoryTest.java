package ai.libs.automl;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.api4.java.ai.ml.core.exception.TrainingException;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ai.libs.jaicore.basic.ATest;
import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.api.IComponentRepository;
import ai.libs.jaicore.components.exceptions.ComponentInstantiationFailedException;
import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.jaicore.components.model.ComponentInstanceUtil;
import ai.libs.jaicore.components.model.ComponentUtil;
import ai.libs.jaicore.components.optimizingfactory.BaseFactory;
import ai.libs.jaicore.test.LongTest;

@TestMethodOrder(OrderAnnotation.class)
public abstract class AbstractComponentInstanceFactoryTest extends ATest {

	public abstract BaseFactory<?> getFactory();

	@Order(1)
	@ParameterizedTest(name = "Testing instantiation of default config component instances of type {0}")
	@MethodSource("getComponentNames")
	@LongTest
	public void testValidDefaultConfigInstantiation(final String name, final List<ComponentInstance> list) throws ComponentInstantiationFailedException, TrainingException, InterruptedException {
		boolean succeeded = true;
		for (int i = 0; (i < list.size()) && succeeded; i++) {
			try {
				this.getFactory().getComponentInstantiation(list.get(i));
			} catch (ComponentInstantiationFailedException e) {
				this.logger.warn("Could not instantiate {}.", list.get(i), e);
				succeeded = false;
			}
		}
		assertTrue(succeeded, "Could not instantiate all default configurations of all algorithm selection choices.");
	}

	@Order(2)
	@ParameterizedTest(name = "Testing instantiation of random config component instances of type {0}")
	@MethodSource("getComponentNames")
	@LongTest
	public void testValidRandomConfigInstantiation(final String name, final List<ComponentInstance> instancesToTest) throws ComponentInstantiationFailedException, TrainingException, InterruptedException {
		this.logger.info("Testing {} component instances.", instancesToTest.size());

		IntStream.range(0, instancesToTest.size()).parallel().forEach(i -> {
			this.logger.trace("Checking {}", ComponentInstanceUtil.getComponentInstanceAsComponentNames(instancesToTest.get(i)));
			int currentI = i;
			boolean success = true;
			for (int j = 0; j < 10 && success; j++) {
				IComponentInstance randomConfig = ComponentUtil.getRandomParametrization(instancesToTest.get(currentI), new Random(j));
				try {
					this.getFactory().getComponentInstantiation(randomConfig);
				} catch (ComponentInstantiationFailedException e) {
					this.logger.warn("Failed to instantiate component instance of {}", ComponentInstanceUtil.toRecursiveConstructorString(randomConfig), e);
					success = false;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			assertTrue(success, "Could not find a realization of component " + instancesToTest.get(currentI));
		});
	}

	public static Stream<Arguments> getComponentNames() {
		return Stream.of();
	}

	public static Stream<Arguments> getComponentNames(final String requestedInterfaceName, final IComponentRepository repository) {
		List<IComponentInstance> list = new ArrayList<>(ComponentUtil.getAllAlgorithmSelectionInstances(requestedInterfaceName, repository));
		Set<String> names = list.stream().map(ci -> ci.getComponent().getName()).collect(Collectors.toSet());
		return names.stream().map(name -> Arguments.of(name, list.stream().filter(ci -> ci.getComponent().getName().equals(name)).collect(Collectors.toList())));
	}
}