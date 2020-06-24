package ai.libs.automl.mlplan.multiclass.wekamlplan.sklearn;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.hasco.model.CategoricalParameterDomain;
import ai.libs.hasco.model.Component;
import ai.libs.hasco.model.ComponentInstance;
import ai.libs.hasco.model.ComponentUtil;
import ai.libs.hasco.model.NumericParameterDomain;
import ai.libs.hasco.model.Parameter;
import ai.libs.hasco.serialization.ComponentLoader;
import ai.libs.jaicore.ml.core.dataset.serialization.ArffDatasetAdapter;
import ai.libs.jaicore.ml.core.evaluation.evaluator.MonteCarloCrossValidationEvaluator;
import ai.libs.jaicore.ml.core.evaluation.evaluator.factory.MonteCarloCrossValidationEvaluatorFactory;
import ai.libs.jaicore.ml.regression.loss.ERulPerformanceMeasure;
import ai.libs.jaicore.ml.regression.singlelabel.SingleTargetRegressionPrediction;
import ai.libs.jaicore.ml.regression.singlelabel.SingleTargetRegressionPredictionBatch;
import ai.libs.jaicore.ml.scikitwrapper.ScikitLearnWrapper;
import ai.libs.mlplan.multiclass.sklearn.EMLPlanSkLearnProblemType;
import ai.libs.mlplan.multiclass.sklearn.SKLearnClassifierFactory;

public class SearchSpaceConfigurationTest {

	private static final Logger LOGGER = LoggerFactory.getLogger("SearchSpaceConfigurationTest");

	private static final EMLPlanSkLearnProblemType PROBLEM_TYPE = EMLPlanSkLearnProblemType.RUL;
	private static final String DATA_FILE = "C:/Users/Kadiray/work_pdm/transformer/dataset-transformer/src/main/resources/data/Artificial/1-sensor/RUL/1sensor_100dense.arff";
	private static final Random RANDOM = new Random(42);

	@Test
	public void searchSpaceConfigurationTest() throws Exception {
		String requestedInterface = PROBLEM_TYPE.getRequestedInterface();
		String searchSpaceConfigFile = PROBLEM_TYPE.getResourceSearchSpaceConfigFile();
		ILabeledDataset<ILabeledInstance> dataset = readData(DATA_FILE);

		List<Component> components = new ArrayList<>(new ComponentLoader(new File(searchSpaceConfigFile)).getComponents());

		List<ComponentInstance> allComponentInstances = new ArrayList<>(ComponentUtil.getAllAlgorithmSelectionInstances(requestedInterface, components));

		MonteCarloCrossValidationEvaluator evaluator = new MonteCarloCrossValidationEvaluatorFactory().withData(dataset).withNumMCIterations(1).withTrainFoldSize(0.7).withMeasure(ERulPerformanceMeasure.ASYMMETRIC_LOSS).withRandom(RANDOM)
				.getLearnerEvaluator();

		SKLearnClassifierFactory<SingleTargetRegressionPrediction, SingleTargetRegressionPredictionBatch> factory = new SKLearnClassifierFactory<SingleTargetRegressionPrediction, SingleTargetRegressionPredictionBatch>();
		factory.setAnacondaEnvironment("pdm");
		factory.setProblemType(PROBLEM_TYPE.getBasicProblemType());

		for (ComponentInstance ciToInstantiate : allComponentInstances) {
			ComponentInstance componentInstance = instantiateWithDefaultParameters(ciToInstantiate);
			ScikitLearnWrapper<SingleTargetRegressionPrediction, SingleTargetRegressionPredictionBatch> model = factory.getComponentInstantiation(componentInstance);
			evaluator.evaluate(model);
		}

		for (ComponentInstance ciToInstantiate : allComponentInstances) {
			List<ComponentInstance> instances = instantiateWithCategoricalParameters(ciToInstantiate);
			for (ComponentInstance instance : instances) {
				ScikitLearnWrapper<SingleTargetRegressionPrediction, SingleTargetRegressionPredictionBatch> model = factory.getComponentInstantiation(instance);
				evaluator.evaluate(model);
			}
		}

		for (ComponentInstance ciToInstantiate : allComponentInstances) {
			List<ComponentInstance> instances = instantiateWithNumericParameters(ciToInstantiate);
			for (ComponentInstance instance : instances) {
				ScikitLearnWrapper<SingleTargetRegressionPrediction, SingleTargetRegressionPredictionBatch> model = factory.getComponentInstantiation(instance);
				evaluator.evaluate(model);
			}
		}

		assertTrue("All models of the search space configuration could be evaluated", true);
	}

	private static ComponentInstance instantiateWithDefaultParameters(final ComponentInstance ciToInstantiate) {
		LOGGER.trace("Instantiating component instance with default parameters.");
		List<ComponentInstance> queue = new LinkedList<>();
		queue.add(ciToInstantiate);
		LOGGER.trace("default parameters for contained components.");
		while (!queue.isEmpty()) {
			ComponentInstance currentCI = queue.remove(0);
			if (!currentCI.getComponent().getParameters().isEmpty()) {
				ComponentInstance parameterization = null;
				while (parameterization == null) {
					try {
						parameterization = ComponentUtil.getDefaultParameterizationOfComponent(currentCI.getComponent());
					} catch (Exception e) {
						LOGGER.warn("Could not instantiate component instance {} with default parameters", ciToInstantiate, e);
					}
				}
				currentCI.getParameterValues().putAll(parameterization.getParameterValues());
			}
			if (!currentCI.getSatisfactionOfRequiredInterfaces().isEmpty()) {
				queue.addAll(currentCI.getSatisfactionOfRequiredInterfaces().values());
			}
		}
		LOGGER.trace("Return default parameterized component instance {}", ciToInstantiate);
		return ciToInstantiate;
	}

	private static List<ComponentInstance> instantiateWithCategoricalParameters(final ComponentInstance ciToInstantiate) {
		LOGGER.trace("Instantiating component instance with categorical parameters.");
		List<ComponentInstance> componentInstanceClonesWithAllPosibleCategoricalParameters = new ArrayList<>();
		List<ComponentInstance> queue = new LinkedList<>();
		queue.add(ciToInstantiate);
		LOGGER.trace("categorical parameters for contained components.");
		while (!queue.isEmpty()) {
			ComponentInstance currentCI = queue.remove(0);
			if (!currentCI.getComponent().getParameters().isEmpty()) {
				List<ComponentInstance> parameterizedComponentInstances = new ArrayList<>();
				String currentComponentName = currentCI.getComponent().getName();
				try {
					parameterizedComponentInstances.addAll(categoricalParameterizationOfComponent(currentCI.getComponent()));
				} catch (Exception e) {
					LOGGER.warn("Could not instantiate component instance {} with categorical parameters", ciToInstantiate, e);
				}
				for (ComponentInstance parameterization : parameterizedComponentInstances) {
					ComponentInstance option = new ComponentInstance(ciToInstantiate);
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
		LOGGER.trace("Return categorical parameterized component instance {}", ciToInstantiate);
		// remove duplicates
		return componentInstanceClonesWithAllPosibleCategoricalParameters.stream().distinct().collect(Collectors.toList());
	}

	/**
	 * For a given component, returns multiple instances each with a different value for categorical parameters
	 *
	 * @param component
	 * @return
	 */
	private static List<ComponentInstance> categoricalParameterizationOfComponent(final Component component) {
		Map<String, String> parameterValues = new HashMap<>();
		List<ComponentInstance> parameterizedInstances = new ArrayList<>();
		List<Parameter> categoricalParameters = new ArrayList<>();
		int maxParameterIndex = 0;
		for (Parameter p : component.getParameters()) {
			if (p.getDefaultDomain() instanceof CategoricalParameterDomain) {
				String[] values = ((CategoricalParameterDomain) p.getDefaultDomain()).getValues();
				if (values.length > maxParameterIndex) {
					maxParameterIndex = values.length;
				}
				categoricalParameters.add(p);
			} else {
				parameterValues.put(p.getName(), p.getDefaultValue() + "");
			}
		}
		for (int parameterIndex = 0; parameterIndex < maxParameterIndex; parameterIndex++) {
			Map<String, String> categoricalParameterValues = new HashMap<>();
			for (int i = 0; i < categoricalParameters.size(); i++) {
				String parameterValue = null;
				String[] values = ((CategoricalParameterDomain) categoricalParameters.get(i).getDefaultDomain()).getValues();
				if (parameterIndex < values.length) {
					parameterValue = values[parameterIndex];
				} else {
					parameterValue = categoricalParameters.get(i).getDefaultValue() + "";
				}
				categoricalParameterValues.put(categoricalParameters.get(i).getName(), parameterValue);
			}
			categoricalParameterValues.putAll(parameterValues);
			parameterizedInstances.add(new ComponentInstance(component, categoricalParameterValues, new HashMap<>()));
		}

		return parameterizedInstances;
	}

	private static List<ComponentInstance> instantiateWithNumericParameters(final ComponentInstance ciToInstantiate) {
		LOGGER.trace("Instantiating component instance with numeric parameters.");
		List<ComponentInstance> componentInstanceClonesWithAllPosibleCategoricalParameters = new ArrayList<>();
		List<ComponentInstance> queue = new LinkedList<>();
		queue.add(ciToInstantiate);
		LOGGER.trace("numeric parameters for contained components.");
		while (!queue.isEmpty()) {
			ComponentInstance currentCI = queue.remove(0);
			if (!currentCI.getComponent().getParameters().isEmpty()) {
				List<ComponentInstance> parameterizedComponentInstances = new ArrayList<>();
				String currentComponentName = currentCI.getComponent().getName();
				try {
					parameterizedComponentInstances.addAll(numericParameterizationOfComponent(currentCI.getComponent()));
				} catch (Exception e) {
					LOGGER.warn("Could not instantiate component instance {} with numeric parameters", ciToInstantiate, e);
				}
				for (ComponentInstance parameterization : parameterizedComponentInstances) {
					ComponentInstance option = new ComponentInstance(ciToInstantiate);
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
		LOGGER.trace("Return numeric parameterized component instance {}", ciToInstantiate);
		// remove duplicates
		return componentInstanceClonesWithAllPosibleCategoricalParameters.stream().distinct().collect(Collectors.toList());
	}

	/**
	 * For a given component returns 2 component instances; one with min values, and one with max values
	 *
	 * @param component
	 * @return
	 */
	public static List<ComponentInstance> numericParameterizationOfComponent(final Component component) {
		List<ComponentInstance> parameterizedInstances = new ArrayList<>();
		Map<String, String> parameterValues = new HashMap<>();
		List<Parameter> numericParameters = new ArrayList<>();
		for (Parameter p : component.getParameters()) {
			if (p.getDefaultDomain() instanceof NumericParameterDomain) {
				numericParameters.add(p);
			} else {
				parameterValues.put(p.getName(), p.getDefaultValue() + "");
			}
		}

		// create a component that consists of min values only
		Map<String, String> numericParameterValues = new HashMap<>();
		for (int i = 0; i < numericParameters.size(); i++) {
			String minValue = ((NumericParameterDomain) numericParameters.get(i).getDefaultDomain()).getMin() + "";
			numericParameterValues.put(numericParameters.get(i).getName(), minValue);
		}
		numericParameterValues.putAll(parameterValues);
		parameterizedInstances.add(new ComponentInstance(component, numericParameterValues, new HashMap<>()));

		// create a component that consists of max values only
		numericParameterValues = new HashMap<>();
		for (int i = 0; i < numericParameters.size(); i++) {
			String maxValue = ((NumericParameterDomain) numericParameters.get(i).getDefaultDomain()).getMax() + "";
			numericParameterValues.put(numericParameters.get(i).getName(), maxValue);
		}
		numericParameterValues.putAll(parameterValues);
		parameterizedInstances.add(new ComponentInstance(component, numericParameterValues, new HashMap<>()));

		return parameterizedInstances;
	}

	private static ILabeledDataset<ILabeledInstance> readData(final String path) throws DatasetDeserializationFailedException {
		long start = System.currentTimeMillis();
		ILabeledDataset<ILabeledInstance> dataset = ArffDatasetAdapter.readDataset(new File(path));
		LOGGER.info("Data read. Time to create dataset object was {}ms", System.currentTimeMillis() - start);
		return dataset;
	}

}
