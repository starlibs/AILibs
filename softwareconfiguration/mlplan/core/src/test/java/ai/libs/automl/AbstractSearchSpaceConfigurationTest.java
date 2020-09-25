package ai.libs.automl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.learner.ISupervisedLearner;
import org.api4.java.algorithm.Timeout;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.hasco.builder.HASCOBuilder;
import ai.libs.hasco.builder.forwarddecomposition.HASCOViaFD;
import ai.libs.jaicore.basic.FileUtil;
import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.exceptions.ComponentInstantiationFailedException;
import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.jaicore.components.model.ComponentUtil;
import ai.libs.jaicore.components.model.RefinementConfiguredSoftwareConfigurationProblem;
import ai.libs.jaicore.components.serialization.ComponentSerialization;
import ai.libs.jaicore.logging.LoggerUtil;
import ai.libs.mlplan.core.IProblemType;

public abstract class AbstractSearchSpaceConfigurationTest {

	private static final boolean FAIL_IMMEDIATELY = true;

	protected Logger LOGGER = LoggerFactory.getLogger(LoggerUtil.LOGGER_NAME_TESTER);

	protected final IProblemType<?> problemType;
	protected final List<ComponentInstance> allComponentInstances;
	protected StringBuilder stringBuilder;
	protected final File searchSpaceFile;
	protected final ComponentSerialization compSerializer = new ComponentSerialization();

	public AbstractSearchSpaceConfigurationTest(final IProblemType<?> problemType) throws IOException {
		this.problemType = problemType;
		this.searchSpaceFile = FileUtil.getExistingFileWithHighestPriority(this.problemType.getSearchSpaceConfigFileFromResource(), this.problemType.getSearchSpaceConfigFromFileSystem());
		this.allComponentInstances = new ArrayList<>(ComponentUtil.getAllAlgorithmSelectionInstances(this.problemType.getRequestedInterface(), this.compSerializer.deserializeRepository(this.searchSpaceFile)));

	}

	@Test
	public void testNoExceptionsInGraphGeneration() throws Exception {
		RefinementConfiguredSoftwareConfigurationProblem<Double> problem = new RefinementConfiguredSoftwareConfigurationProblem<>(this.searchSpaceFile, this.problemType.getRequestedInterface(), ci -> {
			this.LOGGER.info("Evaluating ci {}", this.compSerializer.serialize(ci));
			try {
				ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> learner = this.problemType.getLearnerFactory().getComponentInstantiation(ci);
			} catch (ComponentInstantiationFailedException e) {
				e.printStackTrace();
				return Double.MAX_VALUE;
			}
			return 0.0;
		});
		HASCOViaFD<Double> hasco = HASCOBuilder.get(problem).withBestFirst().viaRandomCompletions().getAlgorithm();
		hasco.setLoggerName(LoggerUtil.LOGGER_NAME_TESTEDALGORITHM);
		hasco.setTimeout(new Timeout(30, TimeUnit.SECONDS));
		try {
			hasco.call();
		}
		catch (AlgorithmTimeoutedException e) {
			/* expected behavior */
		}
		assertTrue(true);
	}

	@Test
	public void testExecutabilityOfDefaultConfigs() throws Exception {
		this.LOGGER.info("Testing default configurations for {}", this.problemType.getName());
		int numberOfPipelinesFound = 0;
		int numberOfErrorsFound = 0;
		this.stringBuilder = new StringBuilder();

		int n = this.allComponentInstances.size();
		int i = 0;
		for (ComponentInstance ciToInstantiate : this.allComponentInstances) {
			i++;
			this.LOGGER.info("Considering composition {}/{}", i, n);

			List<IComponentInstance> queue = new LinkedList<>();
			queue.add(ciToInstantiate);
			this.LOGGER.trace("Sample parameters for contained components.");
			while (!queue.isEmpty()) {
				IComponentInstance currentCI = queue.remove(0);
				if (!currentCI.getComponent().getParameters().isEmpty()) {
					ComponentInstance parametrization = null;
					try {
						parametrization = ComponentUtil.getDefaultParameterizationOfComponent(currentCI.getComponent());
						currentCI.getParameterValues().putAll(parametrization.getParameterValues());
					} catch (Exception e) {
						this.LOGGER.warn("Could not instantiate component instance {} with max parameters", ciToInstantiate, e);
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

		assertEquals(this.stringBuilder.toString(), 0, numberOfErrorsFound, 0.0001);
	}

	@Test
	public void testExecutabilityOfMinConfigs() throws Exception {
		this.LOGGER.info("Testing minimum configurations for {}", this.problemType.getName());
		int numberOfPipelinesFound = 0;
		int numberOfErrorsFound = 0;
		this.stringBuilder = new StringBuilder();

		int n = this.allComponentInstances.size();
		int i = 0;
		for (ComponentInstance ciToInstantiate : this.allComponentInstances) {

			/* compute version of composition in which all parameters are set to their minimum */
			i++;
			this.LOGGER.info("Considering composition {}/{}", i, n);
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
						this.LOGGER.error("Could not instantiate component instance {} with min parameters", ciToInstantiate, e);
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

		assertEquals(this.stringBuilder.toString(), 0, numberOfErrorsFound, 0.0001);
	}

	@Test
	public void testExecutabilityOfMaxConfigs() throws Exception {
		this.LOGGER.info("Testing maximum configurations for {}", this.problemType.getName());
		int numberOfPipelinesFound = 0;
		int numberOfErrorsFound = 0;
		this.stringBuilder = new StringBuilder();

		int n = this.allComponentInstances.size();
		int i = 0;
		for (ComponentInstance ciToInstantiate : this.allComponentInstances) {
			i++;
			this.LOGGER.info("Considering composition {}/{}", i, n);

			List<IComponentInstance> queue = new LinkedList<>();
			queue.add(ciToInstantiate);

			this.LOGGER.trace("Sample parameters for contained components.");
			while (!queue.isEmpty()) {
				IComponentInstance currentCI = queue.remove(0);
				if (!currentCI.getComponent().getParameters().isEmpty()) {
					IComponentInstance parametrization = null;
					while (parametrization == null) {
						try {
							parametrization = ComponentUtil.maxParameterizationOfComponent(currentCI.getComponent());
						} catch (Exception e) {
							this.LOGGER.warn("Could not instantiate component instance {} with max parameters", ciToInstantiate, e);
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

		assertEquals(this.stringBuilder.toString(), 0, numberOfErrorsFound, 0.0001);
	}

	@Test
	public void testExecutabilityOfCatConfigs() throws Exception {
		this.LOGGER.info("Testing categorical configurations for {}", this.problemType.getName());
		int numberOfPipelinesFound = 0;
		int numberOfErrorsFound = 0;
		this.stringBuilder = new StringBuilder();

		for (ComponentInstance componentInstance : this.allComponentInstances) {
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
						this.LOGGER.warn("Could not instantiate component instance {} with categorical parameters", componentInstance, e);
					}
					for (IComponentInstance parameterization : parameterizedComponentInstances) {
						IComponentInstance option = new ComponentInstance(componentInstance);
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

		assertEquals(this.stringBuilder.toString(), 0, numberOfErrorsFound, 0.0001);
	}

	private boolean doesExecutionFail(final IComponentInstance componentInstance) throws Exception {
		try {
			this.execute(componentInstance);
			return false;
		}
		catch (Exception e) {
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
