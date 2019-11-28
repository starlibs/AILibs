package ai.libs.hasco.core;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import ai.libs.hasco.model.Component;
import ai.libs.hasco.model.Parameter;
import ai.libs.hasco.model.ParameterRefinementConfiguration;
import ai.libs.jaicore.logic.fol.structure.ConstantParam;
import ai.libs.jaicore.logic.fol.structure.Monom;
import ai.libs.jaicore.logic.fol.theories.EvaluablePredicate;

public class isNotRefinable implements EvaluablePredicate {
	
	private final IsValidParameterRangeRefinementPredicate p;
	
	public isNotRefinable(Collection<Component> components, Map<Component, Map<Parameter, ParameterRefinementConfiguration>> refinementConfiguration) {
		super();
		this.p = new IsValidParameterRangeRefinementPredicate(components, refinementConfiguration);
	}
	
	@Override
	public Collection<List<ConstantParam>> getParamsForPositiveEvaluation(Monom state, ConstantParam... partialGrounding) {
		throw new UnsupportedOperationException();
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
		return p.getParamsForPositiveEvaluation(state, params[0], params[1], params[2], params[3], params[4], null).isEmpty();
	}

}
