package hasco.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import hasco.knowledgebase.IParameterImportanceEstimator;
import hasco.knowledgebase.PerformanceKnowledgeBase;
import hasco.model.Component;
import hasco.model.Parameter;
import hasco.model.ParameterRefinementConfiguration;
import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.Monom;
import jaicore.logic.fol.theories.EvaluablePredicate;

public class isNotRefinableJ implements EvaluablePredicate {

	private final Collection<Component> components;
	private final Map<Component, Map<Parameter, ParameterRefinementConfiguration>> refinementConfiguration;
	private final isValidParameterRangeRefinementPredicateJ p;

	public isNotRefinableJ(Collection<Component> components,
			Map<Component, Map<Parameter, ParameterRefinementConfiguration>> refinementConfiguration,
			PerformanceKnowledgeBase performanceKB, IParameterImportanceEstimator parameterImportanceEstimator,
			double importanceThreshold, int minNumSamplesForImportanceEstimation, boolean useImportanceEstimation) {
		super();
		this.components = components;
		this.refinementConfiguration = refinementConfiguration;
		// TODO
		this.p = new isValidParameterRangeRefinementPredicateJ(components, refinementConfiguration, performanceKB,
				parameterImportanceEstimator, importanceThreshold, minNumSamplesForImportanceEstimation, useImportanceEstimation);
	}

	@Override
	public Collection<List<ConstantParam>> getParamsForPositiveEvaluation(Monom state,
			ConstantParam... partialGrounding) {
		throw new UnsupportedOperationException();
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
		return p.getParamsForPositiveEvaluation(state, params[0], params[1], params[2], params[3], params[4], null)
				.isEmpty();
	}

}
