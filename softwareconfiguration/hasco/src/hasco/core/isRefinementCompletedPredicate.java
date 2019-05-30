package hasco.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hasco.model.CategoricalParameterDomain;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.model.NumericParameterDomain;
import hasco.model.Parameter;
import hasco.model.ParameterRefinementConfiguration;
import jaicore.basic.sets.SetUtil;
import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.Monom;
import jaicore.logic.fol.theories.EvaluablePredicate;

public class isRefinementCompletedPredicate implements EvaluablePredicate {

	private final Logger logger = LoggerFactory.getLogger(isRefinementCompletedPredicate.class);
	private final Collection<Component> components;
	private final Map<Component, Map<Parameter, ParameterRefinementConfiguration>> refinementConfiguration;
	// private final Map<ComponentInstance,Double> knownCompositionsAndTheirScore =
	// new HashMap<>();

	public isRefinementCompletedPredicate(Collection<Component> components,
			Map<Component, Map<Parameter, ParameterRefinementConfiguration>> refinementConfiguration) {
		super();
		this.components = components;
		this.refinementConfiguration = refinementConfiguration;
	}

	@Override
	public Collection<List<ConstantParam>> getParamsForPositiveEvaluation(Monom state,
			ConstantParam... partialGrounding) {
		throw new NotImplementedException("This is not an oracable predicate!");
	}

	@Override
	public boolean isOracable() {
		return false;
	}

	@Override
	public Collection<List<ConstantParam>> getParamsForNegativeEvaluation(Monom state,
			ConstantParam... partialGrounding) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean test(Monom state, ConstantParam... params) {

		/* initialize routine */
		if (params.length != 2) {
			throw new IllegalArgumentException("There should be exactly two parameters additional to the state but "
					+ params.length + " were provided: " + Arrays.toString(params)
					+ ". This parameters refer to the component name that is being configured and the object itself.");
		}
		if (params[0] == null)
			throw new IllegalArgumentException("The component name must not be null.");
		if (params[1] == null)
			throw new IllegalArgumentException("The component instance reference must not be null.");
		// final String componentName = params[0].getName();
		final String objectContainer = params[1].getName();

		/* determine current values for the params */
		ComponentInstance groundComponent = Util.getGroundComponentsFromState(state, components, false)
				.get(objectContainer);
		Component component = groundComponent.getComponent();
		Map<String, String> componentParamContainers = Util.getParameterContainerMap(state, objectContainer);
		for (Parameter param : component.getParameters()) {
			String containerOfParam = componentParamContainers.get(param.getName());
			String currentValueOfParam = groundComponent.getParameterValue(param);
			boolean variableHasBeenSet = state.contains(new Literal("overwritten('" + containerOfParam + "')"));
			boolean variableHasBeenClosed = state.contains(new Literal("closed('" + containerOfParam + "')"));
			assert variableHasBeenSet == groundComponent.getParametersThatHaveBeenSetExplicitly().contains(param);
			assert !variableHasBeenClosed || variableHasBeenSet : "Parameter " + param.getName() + " of component "
					+ component.getName() + " with default domain " + param.getDefaultDomain()
					+ " has been closed but no value has been set.";

			if (param.isNumeric()) {
				double min = 0;
				double max = 0;
				if (currentValueOfParam != null) {
					List<String> interval = SetUtil.unserializeList(currentValueOfParam);
					min = Double.parseDouble(interval.get(0));
					max = Double.parseDouble(interval.get(1));
				} else {
					min = ((NumericParameterDomain) param.getDefaultDomain()).getMin();
					max = ((NumericParameterDomain) param.getDefaultDomain()).getMax();
				}
				double length = max - min;
				if (length > refinementConfiguration.get(component).get(param).getIntervalLength()) {
					logger.info(
							"Test for isRefinementCompletedPredicate({},{}) is negative. Interval length of [{},{}] is {}. Required length to consider an interval atomic is {}",
							params[0].getName(), objectContainer, min, max, length,
							refinementConfiguration.get(component).get(param).getIntervalLength());
					return false;
				}
			} else if (param.getDefaultDomain() instanceof CategoricalParameterDomain) { // categorical params can be
																							// refined iff the have not
																							// been set and closed
																							// before
				assert param.getDefaultValue() != null : "Param " + param.getName() + " has no default value!";
				if (!variableHasBeenSet && !variableHasBeenClosed) {
					logger.info("Test for isRefinementCompletedPredicate({},{}) is negative", params[0].getName(),
							objectContainer);
					return false;
				}
			} else
				throw new UnsupportedOperationException(
						"Currently no support for testing parameters of type " + param.getClass().getName());
			// System.out.println("\t" + param.getName() + " (" +
			// componentParams.get(param.getName()) + ") is still refinable.");
		}
		logger.info("Test for isRefinementCompletedPredicate({},{}) is positive", params[0].getName(), objectContainer);
		return true;
	}
}
