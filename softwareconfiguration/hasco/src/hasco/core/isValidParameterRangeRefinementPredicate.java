package hasco.core;

import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.Monom;
import jaicore.logic.fol.theories.EvaluablePredicate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.math3.geometry.euclidean.oned.Interval;

import hasco.knowledgebase.PerformanceKnowledgeBase;
import hasco.model.CategoricalParameterDomain;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.model.NumericParameterDomain;
import hasco.model.Parameter;
import hasco.model.ParameterDomain;
import hasco.model.ParameterRefinementConfiguration;

public class isValidParameterRangeRefinementPredicate implements EvaluablePredicate {

	private final Collection<Component> components;
	private final Map<Component, Map<Parameter, ParameterRefinementConfiguration>> refinementConfiguration;
	private final Map<ComponentInstance, Double> knownCompositionsAndTheirScore = new HashMap<>();
	private final PerformanceKnowledgeBase performanceKB;
	private double importanceThreshold;

	public isValidParameterRangeRefinementPredicate(final Collection<Component> components,
			final Map<Component, Map<Parameter, ParameterRefinementConfiguration>> refinementConfiguration,
			final PerformanceKnowledgeBase performanceKB) {
		super();
		this.components = components;
		this.refinementConfiguration = refinementConfiguration;
		this.performanceKB = performanceKB;
	}

	@Override
	public Collection<List<ConstantParam>> getParamsForPositiveEvaluation(final Monom state,
			final ConstantParam... partialGrounding) {

		/* determine the context for which the interval refinement should be oracled */
		if (partialGrounding.length != 6) {
			throw new IllegalArgumentException("The interpreted predicate " + this.getClass().getName()
					+ " requires 6 arguments when oracled but " + partialGrounding.length + " have been provided!");
		}
		String componentName = partialGrounding[0].getName();
		String componentIdentifier = partialGrounding[1].getName();
		String parameterName = partialGrounding[2].getName();
		Component component = this.components.stream().filter(c -> c.getName().equals(componentName)).findAny().get();
		Parameter param = component.getParameters().stream().filter(p -> p.getName().equals(parameterName)).findAny()
				.get();
		List<ConstantParam> partialGroundingAsList = Arrays.asList(partialGrounding);
		String containerName = partialGrounding[3].getName();
		String currentParamValue = partialGrounding[4].getName(); // this is not really used, because the current value
																	// is again read from the state

		/* determine true domain of parameter */
		Map<Parameter, ParameterDomain> paramDomains = Util.getUpdatedDomainsOfComponentParameters(state, component,
				componentIdentifier);

		/*
		 * For jmhansel fANOVA feature: if the parameters importance value is below
		 * threshold epsilon, no more refinements will be allowed
		 */
//		if (performanceKB.getImportanceOfParam(param) < importanceThreshold) {
//			return new ArrayList<>();
//		}

		/* determine refinements for numeric parameters */
		if (param.isNumeric()) {
			NumericParameterDomain currentlyActiveDomain = (NumericParameterDomain) paramDomains.get(param);
			Interval currentInterval = new Interval(currentlyActiveDomain.getMin(), currentlyActiveDomain.getMax());
			final ParameterRefinementConfiguration refinementConfig = this.refinementConfiguration.get(component)
					.get(param);
			if (refinementConfig == null) {
				throw new IllegalArgumentException("No refinement configuration for parameter \"" + parameterName
						+ "\" of component \"" + componentName + "\" has been supplied!");
			}

			/* determine focus if log refinement */
			double focus = -1;
			if (refinementConfig.isInitRefinementOnLogScale()) {
				Optional<Literal> focusPredicate = state.stream()
						.filter(l -> l.getPropertyName().equals("parameterFocus")
								&& l.getParameters().get(0).getName().equals(componentIdentifier)
								&& l.getParameters().get(1).getName().equals(parameterName))
						.findAny();
				if (!focusPredicate.isPresent()) {
					throw new IllegalArgumentException(
							"The given state does not specify a parameter focus for the log-scale parameter "
									+ parameterName + " on object \"" + componentIdentifier + "\"");
				}
				focus = Double.parseDouble(focusPredicate.get().getParameters().get(2).getName());
				
			}

			return this.getGroundingsForIntervals(Util.getNumericParameterRefinement(currentInterval,
					focus, currentlyActiveDomain.isInteger(), refinementConfig), partialGroundingAsList);

		} else if (param.isCategorical()) {
			List<String> possibleValues = new ArrayList<>();
			boolean hasBeenSetBefore = state.contains(new Literal("overwritten('" + containerName + "')"));
			if (hasBeenSetBefore) {
				return new ArrayList<>();
			}
			for (Object valAsObject : ((CategoricalParameterDomain) paramDomains.get(param)).getValues()) {
				possibleValues.add(valAsObject.toString());
			}
			return this.getGroundingsForOracledValues(possibleValues, partialGroundingAsList);
		} else {
			throw new UnsupportedOperationException(
					"Currently no support for parameters of class \"" + param.getClass().getName() + "\"");
		}

		// throw new NotImplementedException("Apparentely, there is an unimplemented
		// case!");
	}

	private Collection<List<ConstantParam>> getGroundingsForIntervals(final List<Interval> refinements,
			final List<ConstantParam> partialGrounding) {
		List<String> paramValues = new ArrayList<>();
		for (Interval oracledInterval : refinements) {
			paramValues.add("[" + oracledInterval.getInf() + ", " + oracledInterval.getSup() + "]");
		}
		return this.getGroundingsForOracledValues(paramValues, partialGrounding);
	}

	private Collection<List<ConstantParam>> getGroundingsForOracledValues(final List<String> refinements,
			final List<ConstantParam> partialGrounding) {
		Collection<List<ConstantParam>> groundings = new ArrayList<>();
		for (String oracledValue : refinements) {
			List<ConstantParam> grounding = new ArrayList<>(partialGrounding);
			grounding.set(5, new ConstantParam(oracledValue));
			groundings.add(grounding);
		}
		return groundings;
	}

	public void informAboutNewSolution(final ComponentInstance solution, final double score) {
		this.knownCompositionsAndTheirScore.put(solution, score);
	}

	@Override
	public boolean isOracable() {
		return true;
	}

	@Override
	public Collection<List<ConstantParam>> getParamsForNegativeEvaluation(final Monom state,
			final ConstantParam... partialGrounding) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean test(final Monom state, final ConstantParam... params) {
		throw new NotImplementedException(
				"Testing the validity-predicate is currently not supported. This is indirectly possible using the oracle.");
	}

}
