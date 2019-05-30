package de.upb.crc901.mlplan.metamining.pipelinecharacterizing;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.math3.geometry.euclidean.oned.Interval;
import org.apache.commons.math3.geometry.partitioning.Region.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hasco.core.Util;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.model.NumericParameterDomain;
import hasco.model.Parameter;
import hasco.model.ParameterRefinementConfiguration;
import treeminer.util.TreeRepresentationUtils;

public class ComponentInstanceStringConverter extends Thread {

	private static final String WEKA_LABEL_FILE = "weka-labels.properties";

	private static final Logger log = LoggerFactory.getLogger(ComponentInstanceStringConverter.class);

	/**
	 * The name of the top node for all pipelines
	 */
	private String pipelineTreeName = "0";

	private IOntologyConnector ontologyConnector;

	private List<ComponentInstance> cIs;

	private Properties wekaLabels;

	private List<String> convertedPipelines;

	private Map<Component, Map<Parameter, ParameterRefinementConfiguration>> componentParameters;

	public ComponentInstanceStringConverter(final IOntologyConnector ontologyConnector, final List<ComponentInstance> cIs, final Map<Component, Map<Parameter, ParameterRefinementConfiguration>> componentParameters) {
		this.ontologyConnector = ontologyConnector;
		this.cIs = cIs;
		this.convertedPipelines = new ArrayList<>(cIs.size());
		this.componentParameters = componentParameters;
		InputStream fis = this.getClass().getClassLoader().getResourceAsStream(WEKA_LABEL_FILE);
		this.wekaLabels = new Properties();
		try {
			this.wekaLabels.load(fis);
		} catch (IOException e) {
			log.warn("Could not load weka labels.");
			throw new ComponentInstanceStringConverterIntializeException(e);
		}
	}

	@Override
	public void run() {
		for (ComponentInstance cI : this.cIs) {
			String pipeline = this.makeStringTreeRepresentation(cI);
			this.convertedPipelines.add(pipeline);
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
	public String makeStringTreeRepresentation(final ComponentInstance pipeline) {
		List<String> pipelineBranches = new ArrayList<>();
		ComponentInstance classifierCI;

		// Component is pipeline
		if (pipeline == null) {
			log.warn("Try to characterize a null pipeline");
			return null;
		}

		if (pipeline.getComponent().getName().equals("pipeline")) {
			ComponentInstance preprocessorCI = pipeline.getSatisfactionOfRequiredInterfaces().get("preprocessor");

			if (preprocessorCI != null) {
				// Characterize searcher
				this.addCharacterizationOfPipelineElement(pipelineBranches, preprocessorCI.getSatisfactionOfRequiredInterfaces().get("search"));

				// Characterize evaluator
				this.addCharacterizationOfPipelineElement(pipelineBranches, preprocessorCI.getSatisfactionOfRequiredInterfaces().get("eval"));
			}

			classifierCI = pipeline.getSatisfactionOfRequiredInterfaces().get("classifier");

			// Component is just a classifier
		} else {
			classifierCI = pipeline;
		}

		// Characterize classifier
		this.addCharacterizationOfPipelineElement(pipelineBranches, classifierCI);

		// Put tree together
		String toReturn = TreeRepresentationUtils.addChildrenToNode(this.pipelineTreeName, pipelineBranches);
		// if we have a properties file which maps our weka label to integers; use it
		if (this.wekaLabels != null) {
			Pattern p = Pattern.compile(" ");
			return p.splitAsStream(toReturn).filter(s -> !"".equals(s)).map(s -> this.wekaLabels.getProperty(s, s)).collect(Collectors.joining(" "));
		} else {
			log.error("Did not find label property mapper.");
			throw new IllegalStateException();
		}
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
	protected void addCharacterizationOfPipelineElement(final List<String> pipelineBranches, final ComponentInstance componentInstance) {
		if (componentInstance != null) {
			String wekaName = componentInstance.getComponent().getName();
			// Get generalization
			List<String> branchComponents = this.ontologyConnector.getAncestorsOfAlgorithm(wekaName);

			// Get parameters
			branchComponents.set(branchComponents.size() - 1, TreeRepresentationUtils.addChildrenToNode(branchComponents.get(branchComponents.size() - 1), this.getParametersForComponentInstance(componentInstance)));

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
	protected List<String> getParametersForComponentInstance(final ComponentInstance componentInstance) {
		List<String> parameters = new ArrayList<>();

		// Get Parameters of base classifier if this is a meta classifier
		if (componentInstance.getSatisfactionOfRequiredInterfaces() != null && componentInstance.getSatisfactionOfRequiredInterfaces().size() > 0) {
			componentInstance.getSatisfactionOfRequiredInterfaces().forEach((requiredInterface, component) -> {
				// so far, only have the "K" interface & this has no param so can directly get
				List<String> kernelFunctionCharacterisation = new ArrayList<>();
				kernelFunctionCharacterisation.add(requiredInterface);
				kernelFunctionCharacterisation.addAll(this.ontologyConnector.getAncestorsOfAlgorithm(component.getComponent().getName()));
				parameters.add(TreeRepresentationUtils.addChildrenToNode(requiredInterface, Arrays.asList(TreeRepresentationUtils.makeRepresentationForBranch(kernelFunctionCharacterisation))));
			});
		}

		// Get other parameters

		for (Parameter parameter : componentInstance.getComponent().getParameters()) {
			// Check if the parameter even has a value!
			String parameterName = parameter.getName();
			if (!componentInstance.getParameterValues().containsKey(parameterName)) {
				continue;

			}

			List<String> parameterRefinement = new ArrayList<>();
			parameterRefinement.add(parameterName);

			// Numeric parameter - needs to be refined
			if (parameter.isNumeric()) {
				this.resolveNumericParameter(componentInstance, parameter, parameterName, parameterRefinement);

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

	private void resolveNumericParameter(final ComponentInstance componentInstance, final Parameter parameter, final String parameterName, final List<String> parameterRefinement) {
		ParameterRefinementConfiguration parameterRefinementConfiguration = this.componentParameters.get(componentInstance.getComponent()).get(parameter);
		NumericParameterDomain parameterDomain = ((NumericParameterDomain) parameter.getDefaultDomain());
		Interval currentInterval = null;
		Interval nextInterval = new Interval(parameterDomain.getMin(), parameterDomain.getMax());
		double parameterValue = Double.parseDouble(componentInstance.getParameterValues().get(parameterName));
		double precision = parameterValue == 0 ? 0 : Math.ulp(parameterValue);

		while (true) {
			currentInterval = nextInterval;
			parameterRefinement.add(this.serializeInterval(currentInterval));

			List<Interval> refinement = Util.getNumericParameterRefinement(nextInterval, parameterValue, parameterDomain.isInteger(), parameterRefinementConfiguration);

			if (refinement.isEmpty()) {
				break;
			}

			for (Interval interval : refinement) {
				if (interval.checkPoint(parameterValue, precision) == Location.INSIDE || interval.checkPoint(parameterValue, precision) == Location.BOUNDARY) {
					nextInterval = interval;
					break;
				}
			}
		}

		parameterRefinement.add(String.valueOf(parameterValue));
	}

	/**
	 * Helper method for serializing an interval so that it can be used in String
	 * representations of parameters of pipeline elements.
	 *
	 * @param interval
	 *            The interval to be serialized
	 * @return The String representation of the interval
	 */
	protected String serializeInterval(final Interval interval) {
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		builder.append(interval.getInf());
		builder.append(",");
		builder.append(interval.getSup());
		builder.append("]");
		return builder.toString();
	}

	public List<String> getConvertedPipelines() {
		return this.convertedPipelines;
	}
}
