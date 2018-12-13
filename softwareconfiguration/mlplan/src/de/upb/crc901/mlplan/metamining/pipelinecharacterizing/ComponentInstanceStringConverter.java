package de.upb.crc901.mlplan.metamining.pipelinecharacterizing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.geometry.euclidean.oned.Interval;
import org.apache.commons.math3.geometry.partitioning.Region.Location;

import hasco.core.Util;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.model.NumericParameterDomain;
import hasco.model.Parameter;
import hasco.model.ParameterRefinementConfiguration;
import treeminer.TreeRepresentationUtils;

public class ComponentInstanceStringConverter extends Thread {
	
	/**
	 * The name of the top node for all pipelines
	 */
	private String pipelineTreeName = "Pipeline";
	
	IOntologyConnector ontologyConnector;
	
	List<ComponentInstance> cIs;
	
	List<String> convertedPipelines;
	
	private Map<Component, Map<Parameter, ParameterRefinementConfiguration>> componentParameters;

	public ComponentInstanceStringConverter(IOntologyConnector ontologyConnector, List<ComponentInstance> cIs, Map<Component, Map<Parameter, ParameterRefinementConfiguration>> componentParameters) {
		this.ontologyConnector = ontologyConnector;
		this.cIs = cIs;
		this.convertedPipelines = new ArrayList<>(cIs.size());
		this.componentParameters = componentParameters;
	}
	
	@Override
	public void run() {
		for (ComponentInstance cI : cIs) {
			String pipeline = makeStringTreeRepresentation(cI);
			convertedPipelines.add(pipeline);
		}
	}
	
	/**
	 * Converts the given MLPipeline to a String representation of its components
	 * using the ontology.
	 * 
	 * @param pipeline
	 *            The pipeline to convert
	 * @return The string representation of the tree deduced from the pipeline
	 * 
	 */
	public String makeStringTreeRepresentation(ComponentInstance pipeline) {
		List<String> pipelineBranches = new ArrayList<String>();
		ComponentInstance classifierCI;

		// Component is pipeline
		if (pipeline == null) {
			System.out.println("Try to characterize a null pipeline");
			return "";
		}

		if (pipeline.getComponent().getName().equals("pipeline")) {
			ComponentInstance preprocessorCI = pipeline.getSatisfactionOfRequiredInterfaces().get("preprocessor");

			if (preprocessorCI != null) {
				// Characterize searcher
				addCharacterizationOfPipelineElement(pipelineBranches,
						preprocessorCI.getSatisfactionOfRequiredInterfaces().get("search"));

				// Characterize evaluator
				addCharacterizationOfPipelineElement(pipelineBranches,
						preprocessorCI.getSatisfactionOfRequiredInterfaces().get("eval"));
			}

			classifierCI = pipeline.getSatisfactionOfRequiredInterfaces().get("classifier");

			// Component is just a classifier
		} else {
			classifierCI = pipeline;
		}

		// Characterize classifier
		addCharacterizationOfPipelineElement(pipelineBranches, classifierCI);

		// Put tree together
		return TreeRepresentationUtils.addChildrenToNode(pipelineTreeName, pipelineBranches);
	}

	/**
	 * Gets the ontology characterization and selected parameters of the given
	 * ComponentInstance and adds its characterization (the branch of a tree that is
	 * the current pipeline) to the pipeline tree by adding its branch
	 * representation as a last element of the list of branches.
	 * 
	 * @param pipelineBranches
	 *            The current branches of the pipeline.
	 * @param componentInstance
	 *            The pipeline element to be characterized
	 */
	protected void addCharacterizationOfPipelineElement(List<String> pipelineBranches,
			ComponentInstance componentInstance) {
		if (componentInstance != null) {
			// Get generalization
			List<String> branchComponents = ontologyConnector
					.getAncestorsOfAlgorithm(componentInstance.getComponent().getName());

			// Get parameters
			branchComponents.set(branchComponents.size() - 1,
					TreeRepresentationUtils.addChildrenToNode(branchComponents.get(branchComponents.size() - 1),
							getParametersForComponentInstance(componentInstance)));

			// Serialize
			String branch = TreeRepresentationUtils.makeRepresentationForBranch(branchComponents);
			pipelineBranches.add(branch);
		}
	}

	/**
	 * Get String representations of the parameters of the given ComponentInstance
	 * (representing a pipeline element). Numerical parameters are refined.
	 * 
	 * @param componentInstance
	 *            The ComponentInstance for which to get the parameters
	 * @return A list of parameter descriptions represented as Strings
	 */
	protected List<String> getParametersForComponentInstance(ComponentInstance componentInstance) {
		List<String> parameters = new ArrayList<String>();

		// Get Parameters of base classifier if this is a meta classifier
		if (componentInstance.getSatisfactionOfRequiredInterfaces() != null
				&& componentInstance.getSatisfactionOfRequiredInterfaces().size() > 0) {
			componentInstance.getSatisfactionOfRequiredInterfaces().forEach((requiredInterface, component) -> {
				// so far, only have the "K" interface & this has no param so can directly get

				List<String> kernelFunctionCharacterisation = Arrays.asList(requiredInterface);
				kernelFunctionCharacterisation
						.addAll(ontologyConnector.getAncestorsOfAlgorithm(component.getComponent().getName()));
				parameters.add(TreeRepresentationUtils.addChildrenToNode(requiredInterface, Arrays
						.asList(TreeRepresentationUtils.makeRepresentationForBranch(kernelFunctionCharacterisation))));
			});
		}

		// Get other parameters

		for (Parameter parameter : componentInstance.getComponent().getParameters()) {
			// Check if the parameter even has a value!
			String parameterName = parameter.getName();
			if (!componentInstance.getParameterValues().containsKey(parameterName)) {
				continue;

			}

			List<String> parameterRefinement = new ArrayList<String>();
			parameterRefinement.add(parameterName);

			// Numeric parameter - needs to be refined
			if (parameter.isNumeric()) {
				ParameterRefinementConfiguration parameterRefinementConfiguration = componentParameters
						.get(componentInstance.getComponent()).get(parameter);
				NumericParameterDomain parameterDomain = ((NumericParameterDomain) parameter.getDefaultDomain());
				Interval currentInterval = null;
				Interval nextInterval = new Interval(parameterDomain.getMin(), parameterDomain.getMax());
				double parameterValue = Double.parseDouble(componentInstance.getParameterValues().get(parameterName));
				double precision = parameterValue == 0 ? 0 : Math.ulp(parameterValue);

				while (nextInterval != null) {
					currentInterval = nextInterval;
					parameterRefinement.add(serializeInterval(currentInterval));

					List<Interval> refinement = Util.getNumericParameterRefinement(nextInterval, parameterValue,
							parameterDomain.isInteger(), parameterRefinementConfiguration);

					if (refinement.size() == 0) {
						nextInterval = null;
						break;
					}

					for (Interval interval : refinement) {
						if (interval.checkPoint(parameterValue, precision) == Location.INSIDE
								|| interval.checkPoint(parameterValue, precision) == Location.BOUNDARY) {
							nextInterval = interval;
							break;
						}
					}
				}

				parameterRefinement.add(String.valueOf(parameterValue));

				// Categorical parameter
			} else {
				if (parameter.isCategorical()) {
					parameterRefinement.add(componentInstance.getParameterValues().get(parameterName));
				}
			}
			parameters.add(TreeRepresentationUtils.makeRepresentationForBranch(parameterRefinement));
		}

		return parameters;
	}

	/**
	 * Helper method for serializing an interval so that it can be used in String
	 * representations of parameters of pipeline elements.
	 * 
	 * @param interval
	 *            The interval to be serialized
	 * @return The String representation of the interval
	 */
	protected String serializeInterval(Interval interval) {
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		builder.append(interval.getInf());
		builder.append(",");
		builder.append(interval.getSup());
		builder.append("]");
		return builder.toString();
	}

	public List<String> getConvertedPipelines() {
		return convertedPipelines;
	}
}
