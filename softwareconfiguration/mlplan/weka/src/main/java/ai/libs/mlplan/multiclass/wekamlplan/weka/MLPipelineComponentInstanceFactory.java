package ai.libs.mlplan.multiclass.wekamlplan.weka;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aeonbits.owner.util.Collections;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import ai.libs.jaicore.components.api.IComponent;
import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.exceptions.ComponentNotFoundException;
import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.jaicore.components.model.ComponentUtil;
import ai.libs.jaicore.ml.weka.classification.pipeline.MLPipeline;
import ai.libs.jaicore.ml.weka.classification.pipeline.SupervisedFilterSelector;
import weka.core.OptionHandler;

/**
 * A factory that provides the ability to wrap given MLPipelines to a ComponentInstance
 *
 * @author Helena Graf
 *
 */
public class MLPipelineComponentInstanceFactory {

	private Collection<IComponent> components;

	/**
	 * Creates a new factory object using the given configuration file
	 *
	 * @param components
	 */
	public MLPipelineComponentInstanceFactory(final Collection<? extends IComponent> components) {
		this.components = new ArrayList<>(components);
	}

	/**
	 * Converts the given MLPipelines object to a ComponentInstance.
	 *
	 * @param pipeline
	 *            The pipelines to convert
	 * @return The converted pipelines as a ComponentInstance
	 * @throws ComponentNotFoundException
	 *             When the pipelines contains elements that are not in the loaded configuration
	 */
	@SuppressWarnings("unchecked")
	public ComponentInstance convertToComponentInstance(final MLPipeline pipeline) throws ComponentNotFoundException {
		if (pipeline.getPreprocessors() != null && !pipeline.getPreprocessors().isEmpty()) {
			// Pipeline has preprocessor
			SupervisedFilterSelector preprocessor = pipeline.getPreprocessors().get(0);

			// CI for searcher
			ComponentInstance searcherCI = this.getComponentInstanceForPipelineElement(preprocessor.getSearcher());

			// CI for evaluator
			ComponentInstance evaluatorCI = this.getComponentInstanceForPipelineElement(preprocessor.getEvaluator());

			// CI for preprocessor
			ComponentInstance preprocessorCI = this.getComponentInstanceForPipelineElement(preprocessor.getSelector(), new ImmutablePair<>("eval", evaluatorCI), new ImmutablePair<>("search", searcherCI));

			// CI for classifier
			ComponentInstance classifierCI = this.getComponentInstanceForPipelineElement(pipeline.getBaseClassifier());

			// Pipeline
			HashMap<String, List<IComponentInstance>> satisfactionOfRequiredInterfaces = new HashMap<>();
			satisfactionOfRequiredInterfaces.put("preprocessor", Arrays.asList(preprocessorCI));
			satisfactionOfRequiredInterfaces.put("classifier", Arrays.asList(classifierCI));
			return new ComponentInstance(ComponentUtil.getComponentByName("pipeline", this.components), new HashMap<>(), satisfactionOfRequiredInterfaces);

		} else {
			// Pipeline is only classifier
			return new ComponentInstance(ComponentUtil.getComponentByName(pipeline.getBaseClassifier().getClass().getName(), this.components), this.getParametersForPipelineElement(pipeline.getBaseClassifier()), new HashMap<>());
		}
	}

	/**
	 * Converts a single element of the pipeline to a ComponentInstance, e.g. a classifier.
	 *
	 * @param pipelineElement
	 *            The pipeline element to convert
	 * @param satisfactionOfRegquiredInterfaces
	 *            If the elements has this component, it must be included, otherwise it is left out
	 * @return The converted ComponentInstance
	 * @throws ComponentNotFoundException
	 *             If the pipeline element contains elements that are not in the loaded configuration
	 */
	private ComponentInstance getComponentInstanceForPipelineElement(final Object pipelineElement, @SuppressWarnings("unchecked") final Pair<String, ComponentInstance>... satisfactionOfRegquiredInterfaces)
			throws ComponentNotFoundException {
		HashMap<String, List<IComponentInstance>> satisfactionOfRequiredInterfaces = new HashMap<>();
		Arrays.stream(satisfactionOfRegquiredInterfaces).forEach(entry -> satisfactionOfRequiredInterfaces.put(entry.getKey(), Arrays.asList(entry.getValue())));
		return new ComponentInstance(ComponentUtil.getComponentByName(pipelineElement.getClass().getName(), this.components), this.getParametersForPipelineElement(pipelineElement), satisfactionOfRequiredInterfaces);
	}

	/**
	 * Gets the parameters for the given pipeline element as a map from parameter name to value
	 *
	 * @param classifier
	 *            The classifier for which to get the parameters
	 * @return The parameter map
	 */
	private Map<String, String> getParametersForPipelineElement(final Object classifier) {
		if (classifier instanceof OptionHandler) {
			OptionHandler handler = (OptionHandler) classifier;
			HashMap<String, String> parametersWithValues = new HashMap<>(handler.getOptions().length);

			String optionName = null;
			boolean previousStringWasAValue = true;

			for (String option : handler.getOptions()) {
				if (option.equals("--")) {
					break;
				}

				if (previousStringWasAValue || (!(NumberUtils.isCreatable(option) || NumberUtils.isParsable(option)) && option.startsWith("-"))) {
					// Current String is option
					if (!previousStringWasAValue) {
						parametersWithValues.put(optionName, "true");
					}

					previousStringWasAValue = false;
					optionName = option.equals("") ? option : option.substring(1, option.length());
				} else {
					// Current String is value
					previousStringWasAValue = true;
					parametersWithValues.put(optionName, option);
				}

			}
			if (!previousStringWasAValue) {
				parametersWithValues.put(optionName, Collections.list(handler.getOptions()).get(handler.getOptions().length - 1));
			}

			return parametersWithValues;
		}

		return new HashMap<>(0);
	}
}
