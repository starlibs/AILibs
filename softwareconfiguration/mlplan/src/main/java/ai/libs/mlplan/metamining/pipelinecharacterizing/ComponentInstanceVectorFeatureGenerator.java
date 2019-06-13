package ai.libs.mlplan.metamining.pipelinecharacterizing;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.hasco.model.CategoricalParameterDomain;
import ai.libs.hasco.model.Component;
import ai.libs.hasco.model.ComponentInstance;
import ai.libs.hasco.model.Parameter;
import ai.libs.jaicore.math.linearalgebra.DenseDoubleVector;
import ai.libs.jaicore.math.linearalgebra.Vector;

/**
 * Characterizes a pipelines by the components that occur in it and the
 * parameters that are set for it.
 *
 * @author Mirko Jürgens
 *
 */
public class ComponentInstanceVectorFeatureGenerator implements IPipelineCharacterizer {

	private static final Logger logger = LoggerFactory.getLogger(ComponentInstanceVectorFeatureGenerator.class);

	/**
	 * Maps the name of a component to a map that maps the name of the hyper
	 * parameter to its index in the dyad vector.
	 */
	private Map<String, Map<String, Integer>> componentNameToParameterDyadIndex = new HashMap<>();

	/**
	 * Maps the name of a component to
	 */
	private Map<String, Integer> componentNameToDyadIndex = new HashMap<>();

	/**
	 * Number of found patterns.
	 */
	private int patternCount;

	/**
	 * Construct a ComponentInstanceVectorFeatureGenerator that is able to
	 * characterize pipelines consisting of the given components and parameters.
	 *
	 * @param collection
	 *            the components to use
	 */
	public ComponentInstanceVectorFeatureGenerator(final Collection<Component> collection) {
		int counter = 0;
		logger.debug("Got {} components as input.", collection.size());
		for (Component component : collection) {
			logger.debug("Inserting {} at position {}", component.getName(), counter);
			this.componentNameToDyadIndex.put(component.getName(), counter++);
			Map<String, Integer> parameterIndices = new HashMap<>();
			logger.debug("{} has {} parameters.", component.getName(), component.getParameters().size());
			for (Parameter param : component.getParameters()) {
				if (param.isNumeric()) {
					parameterIndices.put(param.getName(), counter++);
				} else if (param.isCategorical()) {
					parameterIndices.put(param.getName(), counter);
					CategoricalParameterDomain domain = (CategoricalParameterDomain) param.getDefaultDomain();
					counter += domain.getValues().length;
				}
			}
			this.componentNameToParameterDyadIndex.put(component.getName(), parameterIndices);
		}
		this.patternCount = counter;
	}

	/**
	 * Recursively resolves the components.
	 *
	 * @param cI
	 *            the component instance to resolve
	 * @param patterns
	 *            the patterns found so far
	 * @return the characterization
	 */
	public double[] characterize(final ComponentInstance cI, final Vector patterns) {
		// first: get the encapsulated component
		Component c = cI.getComponent();
		String componentName = c.getName();

		// set the used algorithm to '1'
		int index = this.componentNameToDyadIndex.get(componentName);
		patterns.setValue(index, 1.0d);

		// now resolve the parameters
		Map<String, Integer> parameterIndices = this.componentNameToParameterDyadIndex.get(componentName);

		// assumption: the values is always set in the parameters vector
		for (Parameter param : c.getParameters()) {
			String parameterName = param.getName();
			int parameterIndex = parameterIndices.get(parameterName);
			if (param.isNumeric()) {
				this.handleNumericalParameter(cI, patterns, param, parameterIndex);
			} else if (param.isCategorical()) {
				this.handleCatergoricalParameter(cI, patterns, param, parameterIndex);
			}
		}

		// recursively resolve the patterns for the requiredInterfaces
		for (ComponentInstance requiredInterface : cI.getSatisfactionOfRequiredInterfaces().values()) {
			this.characterize(requiredInterface, patterns);
		}

		return patterns.asArray();
	}

	private void handleNumericalParameter(final ComponentInstance cI, final Vector patterns, final Parameter param, final int parameterIndex) {
		if (cI.getParameterValue(param) != null) {
			double value = Double.parseDouble(cI.getParameterValue(param));
			patterns.setValue(parameterIndex, value);
		} else {
			double value = (double) param.getDefaultValue();
			patterns.setValue(parameterIndex, value);
		}
	}

	private void handleCatergoricalParameter(final ComponentInstance cI, final Vector patterns, final Parameter param,
			final int parameterIndex) {
		// the parameters are one-hot-encoded, where the parameterIndex specifies the
		// one hot index for the first categorical parameter, parameterIndex+1 is the
		// one-hot index for the second parameter etc.
		String parameterValue = cI.getParameterValue(param);
		if (parameterValue == null) {
			if (param.getDefaultValue() instanceof String) {
				parameterValue = (String) param.getDefaultValue();
			} else {
				parameterValue = String.valueOf(param.getDefaultValue());
			}
		}
		CategoricalParameterDomain domain = (CategoricalParameterDomain) param.getDefaultDomain();
		for (int i = 0; i < domain.getValues().length; i++) {
			if (domain.getValues()[i].equals(parameterValue)) {
				patterns.setValue(parameterIndex + i, 1);
			} else {
				patterns.setValue(parameterIndex + i, 0);
			}
		}
	}

	@Override
	public void build(final List<ComponentInstance> pipelines) throws InterruptedException {
		throw new UnsupportedOperationException("This characterizer is not trained!");
	}

	@Override
	public double[] characterize(final ComponentInstance pipeline) {
		return this.characterize(pipeline, new DenseDoubleVector(this.patternCount, 0.0d));
	}

	@Override
	public double[][] getCharacterizationsOfTrainingExamples() {
		throw new UnsupportedOperationException("This characterizer is not trained!");
	}

	@Override
	public int getLengthOfCharacterization() {
		return this.patternCount;
	}

}