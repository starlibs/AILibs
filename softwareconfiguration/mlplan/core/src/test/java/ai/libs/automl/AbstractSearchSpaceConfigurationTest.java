package ai.libs.automl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.api4.java.algorithm.Timeout;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import ai.libs.hasco.builder.HASCOBuilder;
import ai.libs.hasco.builder.forwarddecomposition.HASCOViaFD;
import ai.libs.jaicore.basic.ATest;
import ai.libs.jaicore.basic.FileUtil;
import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.exceptions.ComponentInstantiationFailedException;
import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.jaicore.components.model.ComponentUtil;
import ai.libs.jaicore.components.model.RefinementConfiguredSoftwareConfigurationProblem;
import ai.libs.jaicore.components.serialization.ComponentSerialization;
import ai.libs.jaicore.logging.LoggerUtil;
import ai.libs.jaicore.test.LongTest;
import ai.libs.mlplan.core.IProblemType;

public abstract class AbstractSearchSpaceConfigurationTest extends ATest {

	private static final boolean FAIL_IMMEDIATELY = true;
	protected StringBuilder stringBuilder;
	protected final ComponentSerialization compSerializer = new ComponentSerialization();

	public File getSearchSpaceFile(final IProblemType<?> problemType) {
		return FileUtil.getExistingFileWithHighestPriority(problemType.getSearchSpaceConfigFileFromResource(), problemType.getSearchSpaceConfigFromFileSystem());
	}

	public Collection<IComponentInstance> getComponentInstances(final IProblemType<?> problemType) throws IOException {
		return new ArrayList<>(ComponentUtil.getAllAlgorithmSelectionInstances(problemType.getRequestedInterface(), this.compSerializer.deserializeRepository(this.getSearchSpaceFile(problemType))));
	}

	public abstract void prepare(IProblemType<?> problemType) throws Exception;

	@LongTest
	@ParameterizedTest(name="Test no exceptions in generation of graph for {0}")
	@MethodSource("getProblemTypes")
	public void testNoExceptionsInGraphGeneration(final IProblemType<?> problemType) throws Exception {
		this.prepare(problemType);
		File searchSpaceFile = this.getSearchSpaceFile(problemType);
		RefinementConfiguredSoftwareConfigurationProblem<Double> problem = new RefinementConfiguredSoftwareConfigurationProblem<>(searchSpaceFile, problemType.getRequestedInterface(), ci -> {
			this.logger.info("Evaluating ci {}", this.compSerializer.serialize(ci));
			try {
				problemType.getLearnerFactory().getComponentInstantiation(ci);
			} catch (ComponentInstantiationFailedException e) {
				e.printStackTrace();
				return Double.MAX_VALUE;
			}
			return 0.0;
		});
		HASCOViaFD<Double> hasco = HASCOBuilder.get(problem).withBestFirst().withRandomCompletions().getAlgorithm();
		hasco.setLoggerName(LoggerUtil.LOGGER_NAME_TESTEDALGORITHM);
		hasco.setTimeout(new Timeout(30, TimeUnit.SECONDS));
		try {
			hasco.call();
		} catch (AlgorithmTimeoutedException e) {
			/* expected behavior */
		}
		assertTrue(true);
	}

	@LongTest
	@ParameterizedTest(name="Test executability of default configs for {0}")
	@MethodSource("getProblemTypes")
	public void testExecutabilityOfDefaultConfigs(final IProblemType<?> problemType) throws Exception {
		this.logger.info("Testing default configurations for {}", problemType.getName());
		this.prepare(problemType);
		int numberOfPipelinesFound = 0;
		int numberOfErrorsFound = 0;
		this.stringBuilder = new StringBuilder();

		Collection<IComponentInstance> allComponentInstances = this.getComponentInstances(problemType);
		int n = allComponentInstances.size();
		int i = 0;
		for (IComponentInstance ciToInstantiate : allComponentInstances) {
			i++;
			this.logger.info("Considering composition {}/{}", i, n);

			List<IComponentInstance> queue = new LinkedList<>();
			queue.add(ciToInstantiate);
			this.logger.trace("Sample parameters for contained components.");
			while (!queue.isEmpty()) {
				IComponentInstance currentCI = queue.remove(0);
				if (!currentCI.getComponent().getParameters().isEmpty()) {
					ComponentInstance parametrization = null;
					try {
						parametrization = ComponentUtil.getDefaultParameterizationOfComponent(currentCI.getComponent());
						currentCI.getParameterValues().putAll(parametrization.getParameterValues());
					} catch (Exception e) {
						this.logger.warn("Could not instantiate component instance {} with max parameters", ciToInstantiate, e);
					}
				}
				if (!currentCI.getSatisfactionOfRequiredInterfaces().isEmpty()) {
					currentCI.getSatisfactionOfRequiredInterfaces().values().forEach(cil -> queue.addAll(cil));
				}
			}
			if (ciToInstantiate.getComponent().getRequiredInterfaces().size() == ciToInstantiate.getSatisfactionOfRequiredInterfaces().size()) {
				numberOfPipelinesFound++;
				if (this.doesExecutionFail(ciToInstantiate)) {
					numberOfErrorsFound++;
				}
			}
		}

		this.stringBuilder.append("\n\nTesting default configurations done. ");
		this.stringBuilder.append(numberOfPipelinesFound);
		this.stringBuilder.append(" pipelines testes, ");
		this.stringBuilder.append(numberOfErrorsFound);
		this.stringBuilder.append(" errors found.");

		assertEquals(0, numberOfErrorsFound, 0.0001, this.stringBuilder.toString());
	}

	@LongTest
	@ParameterizedTest(name="Test executability of min configs for {0}")
	@MethodSource("getProblemTypes")
	public void testExecutabilityOfMinConfigs(final IProblemType<?> problemType) throws Exception {
		this.logger.info("Testing minimum configurations for {}", problemType.getName());
		this.prepare(problemType);
		int numberOfPipelinesFound = 0;
		int numberOfErrorsFound = 0;
		this.stringBuilder = new StringBuilder();

		Collection<IComponentInstance> allComponentInstances = this.getComponentInstances(problemType);
		int n = allComponentInstances.size();
		int i = 0;
		for (IComponentInstance ciToInstantiate : allComponentInstances) {

			/* compute version of composition in which all parameters are set to their minimum */
			i++;
			this.logger.info("Considering composition {}/{}", i, n);
			List<IComponentInstance> queue = new LinkedList<>();
			queue.add(ciToInstantiate);
			while (!queue.isEmpty()) {
				IComponentInstance currentCI = queue.remove(0);
				if (!currentCI.getComponent().getParameters().isEmpty()) {
					IComponentInstance parametrization = null;
					try {
						parametrization = ComponentUtil.minParameterizationOfComponent(currentCI.getComponent());
						currentCI.getParameterValues().putAll(parametrization.getParameterValues());
					} catch (Exception e) {
						this.logger.error("Could not instantiate component instance {} with min parameters", ciToInstantiate, e);
					}
				}
				if (!currentCI.getSatisfactionOfRequiredInterfaces().isEmpty()) {
					currentCI.getSatisfactionOfRequiredInterfaces().values().forEach(cil -> queue.addAll(cil));
				}
			}
			if (ciToInstantiate.getComponent().getRequiredInterfaces().size() == ciToInstantiate.getSatisfactionOfRequiredInterfaces().size()) {
				numberOfPipelinesFound++;
				if (this.doesExecutionFail(ciToInstantiate)) {
					numberOfErrorsFound++;
				}
			}
		}

		this.stringBuilder.append("\n\nTesting minimum configurations done. ");
		this.stringBuilder.append(numberOfPipelinesFound);
		this.stringBuilder.append(" pipelines testes, ");
		this.stringBuilder.append(numberOfErrorsFound);
		this.stringBuilder.append(" errors found.");

		assertEquals(0, numberOfErrorsFound, 0.0001, this.stringBuilder.toString());
	}

	@LongTest
	@ParameterizedTest(name="Test executability of max configs for {0}")
	@MethodSource("getProblemTypes")
	public void testExecutabilityOfMaxConfigs(final IProblemType<?> problemType) throws Exception {
		this.logger.info("Testing maximum configurations for {}", problemType.getName());
		this.prepare(problemType);
		int numberOfPipelinesFound = 0;
		int numberOfErrorsFound = 0;
		this.stringBuilder = new StringBuilder();

		Collection<IComponentInstance> allComponentInstances = this.getComponentInstances(problemType);
		int n = allComponentInstances.size();
		int i = 0;
		for (IComponentInstance ciToInstantiate : allComponentInstances) {
			i++;
			this.logger.info("Considering composition {}/{}", i, n);

			List<IComponentInstance> queue = new LinkedList<>();
			queue.add(ciToInstantiate);

			this.logger.trace("Sample parameters for contained components.");
			while (!queue.isEmpty()) {
				IComponentInstance currentCI = queue.remove(0);
				if (!currentCI.getComponent().getParameters().isEmpty()) {
					IComponentInstance parametrization = null;
					while (parametrization == null) {
						try {
							parametrization = ComponentUtil.maxParameterizationOfComponent(currentCI.getComponent());
						} catch (Exception e) {
							this.logger.warn("Could not instantiate component instance {} with max parameters", ciToInstantiate, e);
						}
					}
					currentCI.getParameterValues().putAll(parametrization.getParameterValues());
				}
				if (!currentCI.getSatisfactionOfRequiredInterfaces().isEmpty()) {
					currentCI.getSatisfactionOfRequiredInterfaces().values().forEach(cil -> queue.addAll(cil));
				}
			}
			if (ciToInstantiate.getComponent().getRequiredInterfaces().size() == ciToInstantiate.getSatisfactionOfRequiredInterfaces().size()) {
				numberOfPipelinesFound++;
				if (this.doesExecutionFail(ciToInstantiate)) {
					numberOfErrorsFound++;
				}
			}
		}

		this.stringBuilder.append("\n\nTesting maximum configurations done. ");
		this.stringBuilder.append(numberOfPipelinesFound);
		this.stringBuilder.append(" pipelines testes, ");
		this.stringBuilder.append(numberOfErrorsFound);
		this.stringBuilder.append(" errors found.");

		assertEquals(0, numberOfErrorsFound, 0.0001, this.stringBuilder.toString());
	}


	@ParameterizedTest(name="Test executability of cat configs for {0}")
	@MethodSource("getProblemTypes")
	public void testExecutabilityOfCatConfigs(final IProblemType<?> problemType) throws Exception {
		this.logger.info("Testing categorical configurations for {}", problemType.getName());
		this.prepare(problemType);
		int numberOfPipelinesFound = 0;
		int numberOfErrorsFound = 0;
		this.stringBuilder = new StringBuilder();

		Collection<IComponentInstance> allComponentInstances = this.getComponentInstances(problemType);
		for (IComponentInstance componentInstance : allComponentInstances) {
			List<IComponentInstance> componentInstanceClonesWithAllPosibleCategoricalParameters = new ArrayList<>();
			List<IComponentInstance> queue = new LinkedList<>();
			queue.add(componentInstance);
			while (!queue.isEmpty()) {
				IComponentInstance currentCI = queue.remove(0);
				if (!currentCI.getComponent().getParameters().isEmpty()) {
					List<IComponentInstance> parameterizedComponentInstances = new ArrayList<>();
					String currentComponentName = currentCI.getComponent().getName();
					try {
						parameterizedComponentInstances.addAll(ComponentUtil.categoricalParameterizationsOfComponent(currentCI.getComponent()));
					} catch (Exception e) {
						this.logger.warn("Could not instantiate component instance {} with categorical parameters", componentInstance, e);
					}
					for (IComponentInstance parameterization : parameterizedComponentInstances) {
						IComponentInstance option = new ComponentInstance((ComponentInstance) componentInstance);
						List<IComponentInstance> optionQueue = new LinkedList<>();
						optionQueue.add(option);
						while (!optionQueue.isEmpty()) {
							IComponentInstance currentOption = optionQueue.remove(0);
							if (!currentOption.getComponent().getParameters().isEmpty() && currentOption.getComponent().getName().equals(currentComponentName)) {
								currentOption.getParameterValues().putAll(parameterization.getParameterValues());
							}
							if (!currentOption.getSatisfactionOfRequiredInterfaces().isEmpty()) {
								currentOption.getSatisfactionOfRequiredInterfaces().values().forEach(cil -> optionQueue.addAll(cil));
							}

						}
						componentInstanceClonesWithAllPosibleCategoricalParameters.add(option);
					}
				}
				if (currentCI.getComponent().getRequiredInterfaces().size() == currentCI.getSatisfactionOfRequiredInterfaces().size()) {
					currentCI.getSatisfactionOfRequiredInterfaces().values().forEach(cil -> queue.addAll(cil));
				}
			}
			for (IComponentInstance instance : componentInstanceClonesWithAllPosibleCategoricalParameters.stream().distinct().collect(Collectors.toList())) {
				numberOfPipelinesFound++;
				if (this.doesExecutionFail(instance)) {
					numberOfErrorsFound++;
				}
			}
		}

		this.stringBuilder.append("\n\nTesting categorical configurations done. ");
		this.stringBuilder.append(numberOfPipelinesFound);
		this.stringBuilder.append(" pipelines testes, ");
		this.stringBuilder.append(numberOfErrorsFound);
		this.stringBuilder.append(" errors found.");

		assertEquals(0, numberOfErrorsFound, 0.0001, this.stringBuilder.toString());
	}

	private boolean doesExecutionFail(final IComponentInstance componentInstance) throws Exception {
		try {
			this.execute(componentInstance);
			return false;
		} catch (Exception e) {
			this.stringBuilder.append("\n\n========================================================================================\n");
			this.stringBuilder.append("Could not execute pipeline:\n");
			this.stringBuilder.append(this.compSerializer.serialize(componentInstance));
			this.stringBuilder.append("\n");
			this.stringBuilder.append("Unknown Reason\n" + this.getReasonForFailure(e));
			if (FAIL_IMMEDIATELY) {
				fail(this.stringBuilder.toString());
			}
			return true;
		}
	}

	public abstract void execute(final IComponentInstance componentInstance) throws Exception;

	public abstract String getReasonForFailure(Exception e);

}
