package ai.libs.automl;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.FileUtil;
import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.jaicore.components.model.ComponentUtil;
import ai.libs.jaicore.components.serialization.ComponentLoader;
import ai.libs.mlplan.core.IProblemType;

public abstract class AbstractSearchSpaceConfigurationTest {

	private static final Logger LOGGER = LoggerFactory.getLogger("mlplan");

	protected final IProblemType<?> problemType;
	protected final List<ComponentInstance> allComponentInstances;
	protected StringBuilder stringBuilder;

	public AbstractSearchSpaceConfigurationTest(final IProblemType<?> problemType) throws IOException {
		this.problemType = problemType;
		this.allComponentInstances = new ArrayList<>(ComponentUtil.getAllAlgorithmSelectionInstances(this.problemType.getRequestedInterface(),
				new ComponentLoader(FileUtil.getExistingFileWithHighestPriority(this.problemType.getSearchSpaceConfigFileFromResource(), this.problemType.getSearchSpaceConfigFromFileSystem())).getComponents()));

	}

	@Test
	public void testDefaultConfigs() throws Exception {
		LOGGER.info("Testing default configurations for {}", this.problemType.getName());
		int numberOfPipelinesFound = 0;
		int numberOfErrorsFound = 0;
		this.stringBuilder = new StringBuilder();

		for (ComponentInstance ciToInstantiate : this.allComponentInstances) {
			List<ComponentInstance> queue = new LinkedList<>();
			queue.add(ciToInstantiate);

			LOGGER.trace("Sample parameters for contained components.");
			while (!queue.isEmpty()) {
				ComponentInstance currentCI = queue.remove(0);
				if (!currentCI.getComponent().getParameters().isEmpty()) {
					ComponentInstance parametrization = null;
					while (parametrization == null) {
						try {
							parametrization = ComponentUtil.getDefaultParameterizationOfComponent(currentCI.getComponent());
						} catch (Exception e) {
							LOGGER.warn("Could not instantiate component instance {} with max parameters", ciToInstantiate, e);
						}
					}
					currentCI.getParameterValues().putAll(parametrization.getParameterValues());
				}
				if (!currentCI.getSatisfactionOfRequiredInterfaces().isEmpty()) {
					queue.addAll(currentCI.getSatisfactionOfRequiredInterfaces().values());
				}
			}
			if (!ciToInstantiate.getSatisfactionOfRequiredInterfaces().isEmpty()) {
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

		assertEquals(this.stringBuilder.toString(), 0, numberOfErrorsFound, 0.0001);
	}

	@Test
	public void testMinConfigs() throws Exception {
		LOGGER.info("Testing minimum configurations for {}", this.problemType.getName());
		int numberOfPipelinesFound = 0;
		int numberOfErrorsFound = 0;
		this.stringBuilder = new StringBuilder();

		for (ComponentInstance ciToInstantiate : this.allComponentInstances) {
			List<ComponentInstance> queue = new LinkedList<>();
			queue.add(ciToInstantiate);

			LOGGER.trace("Sample parameters for contained components.");
			while (!queue.isEmpty()) {
				ComponentInstance currentCI = queue.remove(0);
				if (!currentCI.getComponent().getParameters().isEmpty()) {
					ComponentInstance parametrization = null;
					while (parametrization == null) {
						try {
							parametrization = ComponentUtil.minParameterizationOfComponent(currentCI.getComponent());
						} catch (Exception e) {
							LOGGER.warn("Could not instantiate component instance {} with max parameters", ciToInstantiate, e);
						}
					}
					currentCI.getParameterValues().putAll(parametrization.getParameterValues());
				}
				if (!currentCI.getSatisfactionOfRequiredInterfaces().isEmpty()) {
					queue.addAll(currentCI.getSatisfactionOfRequiredInterfaces().values());
				}
			}
			if (!ciToInstantiate.getSatisfactionOfRequiredInterfaces().isEmpty()) {
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

		assertEquals(this.stringBuilder.toString(), 0, numberOfErrorsFound, 0.0001);
	}

	@Test
	public void testMaxConfigs() throws Exception {
		LOGGER.info("Testing maximum configurations for {}", this.problemType.getName());
		int numberOfPipelinesFound = 0;
		int numberOfErrorsFound = 0;
		this.stringBuilder = new StringBuilder();

		for (ComponentInstance ciToInstantiate : this.allComponentInstances) {
			List<ComponentInstance> queue = new LinkedList<>();
			queue.add(ciToInstantiate);

			LOGGER.trace("Sample parameters for contained components.");
			while (!queue.isEmpty()) {
				ComponentInstance currentCI = queue.remove(0);
				if (!currentCI.getComponent().getParameters().isEmpty()) {
					ComponentInstance parametrization = null;
					while (parametrization == null) {
						try {
							parametrization = ComponentUtil.maxParameterizationOfComponent(currentCI.getComponent());
						} catch (Exception e) {
							LOGGER.warn("Could not instantiate component instance {} with max parameters", ciToInstantiate, e);
						}
					}
					currentCI.getParameterValues().putAll(parametrization.getParameterValues());
				}
				if (!currentCI.getSatisfactionOfRequiredInterfaces().isEmpty()) {
					queue.addAll(currentCI.getSatisfactionOfRequiredInterfaces().values());
				}
			}
			if (!ciToInstantiate.getSatisfactionOfRequiredInterfaces().isEmpty()) {
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

		assertEquals(this.stringBuilder.toString(), 0, numberOfErrorsFound, 0.0001);
	}

	@Test
	public void testCatConfigs() throws Exception {
		LOGGER.info("Testing categorical configurations for {}", this.problemType.getName());
		int numberOfPipelinesFound = 0;
		int numberOfErrorsFound = 0;
		this.stringBuilder = new StringBuilder();

		for (ComponentInstance componentInstance : this.allComponentInstances) {
			List<ComponentInstance> componentInstanceClonesWithAllPosibleCategoricalParameters = new ArrayList<>();
			List<ComponentInstance> queue = new LinkedList<>();
			queue.add(componentInstance);
			while (!queue.isEmpty()) {
				ComponentInstance currentCI = queue.remove(0);
				if (!currentCI.getComponent().getParameters().isEmpty()) {
					List<ComponentInstance> parameterizedComponentInstances = new ArrayList<>();
					String currentComponentName = currentCI.getComponent().getName();
					try {
						parameterizedComponentInstances.addAll(ComponentUtil.categoricalParameterizationsOfComponent(currentCI.getComponent()));
					} catch (Exception e) {
						LOGGER.warn("Could not instantiate component instance {} with categorical parameters", componentInstance, e);
					}
					for (ComponentInstance parameterization : parameterizedComponentInstances) {
						ComponentInstance option = new ComponentInstance(componentInstance);
						List<ComponentInstance> optionQueue = new LinkedList<>();
						optionQueue.add(option);
						while (!optionQueue.isEmpty()) {
							ComponentInstance currentOption = optionQueue.remove(0);
							if (!currentOption.getComponent().getParameters().isEmpty() && currentOption.getComponent().getName().equals(currentComponentName)) {
								currentOption.getParameterValues().putAll(parameterization.getParameterValues());
							}
							if (!currentOption.getSatisfactionOfRequiredInterfaces().isEmpty()) {
								optionQueue.addAll(currentOption.getSatisfactionOfRequiredInterfaces().values());
							}

						}
						componentInstanceClonesWithAllPosibleCategoricalParameters.add(option);
					}
				}
				if (!currentCI.getSatisfactionOfRequiredInterfaces().isEmpty()) {
					queue.addAll(currentCI.getSatisfactionOfRequiredInterfaces().values());
				}
			}
			for (ComponentInstance instance : componentInstanceClonesWithAllPosibleCategoricalParameters.stream().distinct().collect(Collectors.toList())) {
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

		assertEquals(this.stringBuilder.toString(), 0, numberOfErrorsFound, 0.0001);
	}

	protected abstract boolean doesExecutionFail(final ComponentInstance componentInstance) throws Exception;

}
