package hasco.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;

import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.model.NumericParameter;
import hasco.model.Parameter;
import hasco.model.ParameterRefinementConfiguration;
import jaicore.basic.SetUtil;
import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.Monom;
import jaicore.logic.fol.theories.EvaluablePredicate;

public class isRefinementCompletedPredicate implements EvaluablePredicate {

	private final Collection<Component> components;
	private final Map<Component,Map<Parameter, ParameterRefinementConfiguration>> refinementConfiguration;
//	private final Map<ComponentInstance,Double> knownCompositionsAndTheirScore = new HashMap<>();

	public isRefinementCompletedPredicate(Collection<Component> components, Map<Component,Map<Parameter, ParameterRefinementConfiguration>> refinementConfiguration) {
		super();
		this.components = components;
		this.refinementConfiguration = refinementConfiguration;
	}
	
	@Override
	public Collection<List<ConstantParam>> getParamsForPositiveEvaluation(Monom state, ConstantParam... partialGrounding) {
		throw new NotImplementedException("This is not an oracable predicate!");
	}

	@Override
	public boolean isOracable() {
		return false;
	}

	@Override
	public Collection<List<ConstantParam>> getParamsForNegativeEvaluation(Monom state, ConstantParam... partialGrounding) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean test(Monom state, ConstantParam... params) {
		
		/* initialize routine */
		if (params.length != 2) {
			throw new IllegalArgumentException("There should be exactly two parameters additional to the state but " + params.length +" were provided: " + Arrays.toString(params) + ". This parameters refer to the component name that is being configured and the object itself.");
		}
		if (params[0] == null)
			throw new IllegalArgumentException("The component name must not be null.");
		if (params[1] == null)
			throw new IllegalArgumentException("The component instance reference must not be null.");
//		final String componentName = params[0].getName();
		final String objectContainer = params[1].getName();

		/* determine current values for the params */
		ComponentInstance groundComponent = Util.getGroundComponentsFromState(state, components, false).get(objectContainer);
		Component component = groundComponent.getComponent();
		Map<String,String> componentParams = groundComponent.getParameterValues();
		for (Parameter param : component.getParameters()) {
			if (param instanceof NumericParameter) {
				ParameterRefinementConfiguration refinementConfig = refinementConfiguration.get(component).get(param);
				List<String> interval = SetUtil.unserializeList(componentParams.get(param.getName()));
				double min = Double.parseDouble(interval.get(0));
				double max = Double.parseDouble(interval.get(1));
				double length = max - min;
				if (length > refinementConfig.getIntervalLength())
					return false;
			}
//			System.out.println("\t" + param.getName() + " (" + componentParams.get(param.getName()) + ") is still refinable.");
		}
		return true;
	}
}
