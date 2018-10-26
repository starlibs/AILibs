package de.upb.crc901.mlplan.multiclass.wekamlplan.weka;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.aeonbits.owner.util.Collections;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.model.MLPipeline;
import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.model.SupervisedFilterSelector;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.serialization.ComponentNotFoundException;
import hasco.serialization.ComponentUtils;
import weka.core.OptionHandler;

/**
 * A factory that provides the ability to wrap given MLPipelines to a
 * ComponentInstance
 * 
 * @author Helena Graf
 *
 */
public class MLPipelineComponentInstanceFactory {

	private Collection<Component> components;

	/**
	 * Creates a new factory object using the given configuration file
	 * 
	 * @param components
	 * @throws IOException
	 */
	public MLPipelineComponentInstanceFactory(Collection<Component> components) throws IOException {
		// TODO possibly change this to get loaded components so that components don't
		// have to be loaded twice
		this.components = components;
	}

	/**
	 * Converts the given MLPipelines object to a ComponentInstance.
	 * 
	 * @param pipeline
	 *            The pipelines to convert
	 * @return The converted pipelines as a ComponentInstance
	 * @throws ComponentNotFoundException
	 *             When the pipelines contains elements that are not in the loaded
	 *             configuration
	 */
	@SuppressWarnings("unchecked")
	public ComponentInstance convertToComponentInstance(MLPipeline pipeline) throws ComponentNotFoundException {
		if (pipeline.getPreprocessors() != null && pipeline.getPreprocessors().size() > 0) {
			// Pipeline has preprocessor
			SupervisedFilterSelector preprocessor = pipeline.getPreprocessors().get(0);

			// CI for searcher
			ComponentInstance searcherCI = getComponentInstanceForPipelineElement(preprocessor.getSearcher());

			// CI for evaluator
			ComponentInstance evaluatorCI = getComponentInstanceForPipelineElement(preprocessor.getEvaluator());

			// CI for preprocessor
			ComponentInstance preprocessorCI = getComponentInstanceForPipelineElement(preprocessor.getSelector(),
					new ImmutablePair<>("eval", evaluatorCI), new ImmutablePair<>("search", searcherCI));

			// CI for classifier
			ComponentInstance classifierCI = getComponentInstanceForPipelineElement(pipeline.getBaseClassifier());

			// Pipeline
			HashMap<String, ComponentInstance> satisfactionOfRequiredInterfaces = new HashMap<String, ComponentInstance>();
			satisfactionOfRequiredInterfaces.put("preprocessor", preprocessorCI);
			satisfactionOfRequiredInterfaces.put("classifier", classifierCI);
			return new ComponentInstance(ComponentUtils.getComponentByName("pipeline", components), new HashMap<String, String>(),
					satisfactionOfRequiredInterfaces);

		} else {
			// Pipeline is only classifier
			ComponentInstance classifierCI = new ComponentInstance(
					ComponentUtils.getComponentByName(pipeline.getBaseClassifier().getClass().getName(), components),
					getParametersForPipelineElement(pipeline.getBaseClassifier()),
					new HashMap<String, ComponentInstance>());
			return classifierCI;
		}
	}

	/**
	 * Converts a single element of the pipeline to a ComponentInstance, e.g. a
	 * classifier.
	 * 
	 * @param pipelineElement
	 *            The pipeline element to convert
	 * @param satisfactionOfRegquiredInterfaces
	 *            If the elements has this component, it must be included, otherwise
	 *            it is left out
	 * @return The converted ComponentInstance
	 * @throws ComponentNotFoundException
	 *             If the pipeline element contains elements that are not in the
	 *             loaded configuration
	 */
	private ComponentInstance getComponentInstanceForPipelineElement(Object pipelineElement,
			@SuppressWarnings("unchecked") Pair<String, ComponentInstance>... satisfactionOfRegquiredInterfaces)
			throws ComponentNotFoundException {
		HashMap<String, ComponentInstance> satisfactionOfRequiredInterfaces = new HashMap<String, ComponentInstance>();
		Arrays.stream(satisfactionOfRegquiredInterfaces).forEach(entry -> {
			satisfactionOfRequiredInterfaces.put(entry.getKey(), entry.getValue());
		});
		return new ComponentInstance(ComponentUtils.getComponentByName(pipelineElement.getClass().getName(), components),
				getParametersForPipelineElement(pipelineElement), satisfactionOfRequiredInterfaces);
	}

	/**
	 * Gets the parameters for the given pipeline element as a map from parameter
	 * name to value
	 * 
	 * @param classifier
	 *            The classifier for which to get the parameters
	 * @return The parameter map
	 */
	private Map<String, String> getParametersForPipelineElement(Object classifier) {
		if (classifier instanceof OptionHandler) {
			OptionHandler handler = (OptionHandler) classifier;
			HashMap<String, String> parametersWithValues = new HashMap<String, String>(handler.getOptions().length);

			String optionName = null;
			boolean previousStringWasAValue = true;

			for (String option : handler.getOptions()) {
				if (option.equals("--")) {
					// TODO here all classifier parameters (i.e. for meta classifiers and such) are
					// skipped! Might want to include that in the future
					break;
				}

				if (previousStringWasAValue || (!(NumberUtils.isCreatable(option) || NumberUtils.isParsable(option))
						&& option.startsWith("-"))) {
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
				parametersWithValues.put(optionName,
						Collections.list(handler.getOptions()).get(handler.getOptions().length - 1));
			}

			return parametersWithValues;
		}

		return new HashMap<String, String>(0);
	}
}
