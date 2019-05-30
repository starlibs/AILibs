package hasco.core;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import hasco.model.Component;
import hasco.model.Parameter;
import hasco.model.ParameterRefinementConfiguration;
import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.Monom;
import jaicore.logic.fol.theories.EvaluablePredicate;

public class IsNotRefinable implements EvaluablePredicate {

	private final IsValidParameterRangeRefinementPredicate p;

	public IsNotRefinable(final Collection<Component> components, final Map<Component, Map<Parameter, ParameterRefinementConfiguration>> refinementConfiguration) {
		super();
		this.p = new IsValidParameterRangeRefinementPredicate(components, refinementConfiguration);
	}

	@Override
	public Collection<List<ConstantParam>> getParamsForPositiveEvaluation(final Monom state, final ConstantParam... partialGrounding) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isOracable() {
		return false;
	}

	@Override
	public Collection<List<ConstantParam>> getParamsForNegativeEvaluation(final Monom state, final ConstantParam... partialGrounding) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean test(final Monom state, final ConstantParam... params) {
		return this.p.getParamsForPositiveEvaluation(state, params[0], params[1], params[2], params[3], params[4], null).isEmpty();
	}

}