package hasco.core;

import jaicore.basic.sets.SetUtil;
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
import java.util.stream.Collectors;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.math3.geometry.euclidean.oned.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hasco.model.CategoricalParameterDomain;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.model.NumericParameterDomain;
import hasco.model.Parameter;
import hasco.model.ParameterDomain;
import hasco.model.ParameterRefinementConfiguration;

public class isValidParameterRangeRefinementPredicate implements EvaluablePredicate {

	private final Logger logger = LoggerFactory.getLogger(isValidParameterRangeRefinementPredicate.class);

	private final Collection<Component> components;
	private final Map<Component, Map<Parameter, ParameterRefinementConfiguration>> refinementConfiguration;
	private final Map<ComponentInstance, Double> knownCompositionsAndTheirScore = new HashMap<>();

	public isValidParameterRangeRefinementPredicate(final Collection<Component> components,
			final Map<Component, Map<Parameter, ParameterRefinementConfiguration>> refinementConfiguration) {
		super();
		this.components = components;
		this.refinementConfiguration = refinementConfiguration;
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
		logger.info("Determining positive evaluations for isValidParameterRangeRefinementPredicate({},{},{},{},{},{})",
				componentName, componentIdentifier, parameterName, containerName, currentParamValue,
				partialGrounding[5]);
		boolean hasBeenSetBefore = state.contains(new Literal("overwritten('" + containerName + "')"));

		/* determine component instance and the true domain of parameter */
		ComponentInstance instance = Util.getComponentInstanceFromState(components, state, componentIdentifier, false);
		logger.debug(
				"Derived component instance to be refined: {}. Parameter to refine: {}. Current value of parameter: {}",
				instance, param, currentParamValue);
		try {
			Map<Parameter, ParameterDomain> paramDomains = Util.getUpdatedDomainsOfComponentParameters(instance);
			if (logger.isDebugEnabled()) {
				logger.debug("Parameter domains are: {}", paramDomains.keySet().stream()
						.map(k -> "\n\t" + k + ": " + paramDomains.get(k)).collect(Collectors.joining()));
			}

			/* determine refinements for numeric parameters */
			if (param.isNumeric()) {
				NumericParameterDomain currentlyActiveDomain = (NumericParameterDomain) paramDomains.get(param);
				Interval currentInterval = new Interval(currentlyActiveDomain.getMin(), currentlyActiveDomain.getMax());
				assert (!hasBeenSetBefore || (currentInterval.getInf() == Double
						.valueOf(SetUtil.unserializeList(currentParamValue).get(0))
						&& currentInterval.getSup() == Double.valueOf(SetUtil.unserializeList(currentParamValue).get(
								1)))) : "The derived currently active domain of an explicitly set parameter deviates from the domain specified in the state!";
				ParameterRefinementConfiguration refinementConfig = this.refinementConfiguration.get(component)
						.get(param);
				if (refinementConfig == null) {
					throw new IllegalArgumentException("No refinement configuration for parameter \"" + parameterName
							+ "\" of component \"" + componentName + "\" has been supplied!");
				}

				/*
				 * if the interval is under the distinction threshold, return an empty list of
				 * possible refinements (predicate will always be false here)
				 */
				if (currentInterval.getSup() - currentInterval.getInf() < refinementConfig.getIntervalLength()) {
					logger.info(
							"Returning an empty list as this is a numeric parameter that has been narrowed sufficiently. Required interval length is {}, and actual interval length is {}",
							refinementConfig.getIntervalLength(), currentInterval.getSup() - currentInterval.getInf());
					if (!hasBeenSetBefore) {
						List<Interval> unmodifiedRefinement = new ArrayList<>();
						unmodifiedRefinement.add(currentInterval);
						return this.getGroundingsForIntervals(unmodifiedRefinement, partialGroundingAsList);
					}
					return new ArrayList<>();
				}

				/*
				 * if this is an integer and the number of comprised integers are at most as
				 * many as the branching factor, enumerate them
				 */
				if (currentlyActiveDomain.isInteger() && (Math.floor(currentInterval.getSup())
						- Math.ceil(currentInterval.getInf()) + 1 <= refinementConfig.getRefinementsPerStep())) {
					List<Interval> proposedRefinements = new ArrayList<>();
					for (int i = (int) Math.ceil(currentInterval.getInf()); i <= (int) Math
							.floor(currentInterval.getSup()); i++) {
						proposedRefinements.add(new Interval(i, i));
					}
					logger.info("Ultimate level of integer refinement reached. Returning refinements: {}.",
							proposedRefinements);
					return this.getGroundingsForIntervals(proposedRefinements, partialGroundingAsList);
				}
				if (hasBeenSetBefore || !refinementConfig.isInitRefinementOnLogScale()) {
					List<Interval> proposedRefinements = this.refineOnLinearScale(currentInterval,
							refinementConfig.getRefinementsPerStep(), refinementConfig.getIntervalLength());
					for (Interval proposedRefinement : proposedRefinements) {
						assert proposedRefinement.getInf() >= currentInterval.getInf()
								&& proposedRefinement.getSup() <= currentInterval.getSup() : "The proposed refinement ["
										+ proposedRefinement.getInf() + ", " + proposedRefinement.getSup()
										+ "] is not a sub-interval of " + currentParamValue + "].";
						assert !proposedRefinement
								.equals(currentInterval) : "No real refinement! Intervals are identical.";
					}
					logger.info("Returning linear refinements: {}.", proposedRefinements);
					return this.getGroundingsForIntervals(proposedRefinements, partialGroundingAsList);
				}
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
				double focus = Double.parseDouble(focusPredicate.get().getParameters().get(2).getName());
				List<Interval> proposedRefinements = this.refineOnLogScale(currentInterval,
						refinementConfig.getRefinementsPerStep(), 2, focus);
				for (Interval proposedRefinement : proposedRefinements) {
					double epsilon = 1E-7;
					assert proposedRefinement.getInf() + epsilon >= currentInterval.getInf() && proposedRefinement
							.getSup() <= currentInterval.getSup() + epsilon : "The proposed refinement ["
									+ proposedRefinement.getInf() + ", " + proposedRefinement.getSup()
									+ "] is not a sub-interval of " + currentParamValue + "].";
					assert !proposedRefinement.equals(currentInterval) : "No real refinement! Intervals are identical.";
				}
				logger.info("Returning default refinements: {}.", proposedRefinements);
				return this.getGroundingsForIntervals(proposedRefinements, partialGroundingAsList);
			} else if (param.isCategorical()) {
				List<String> possibleValues = new ArrayList<>();
				if (hasBeenSetBefore) {
					logger.info("Returning empty list since param has been set before.");
					return new ArrayList<>();
				}
				for (Object valAsObject : ((CategoricalParameterDomain) paramDomains.get(param)).getValues()) {
					possibleValues.add(valAsObject.toString());
				}
				logger.info("Returning possible values {}.", possibleValues);
				return this.getGroundingsForOracledValues(possibleValues, partialGroundingAsList);
			} else {
				throw new UnsupportedOperationException(
						"Currently no support for parameters of class \"" + param.getClass().getName() + "\"");
			}
		} catch (Throwable e) {
			e.printStackTrace();
			System.exit(1);
			return null;
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

	public List<Interval> refineOnLinearScale(final Interval interval, final int maxNumberOfSubIntervals,
			final double minimumLengthOfIntervals) {
		double min = interval.getInf();
		double max = interval.getSup();
		double length = max - min;
		List<Interval> intervals = new ArrayList<>();
		/* if no refinement is possible, return just the interval itself */
		if (length <= minimumLengthOfIntervals) {
			intervals.add(interval);
			return intervals;
		}
		/* otherwise compute the sub-intervals */
		int numberOfIntervals = Math.min((int) Math.ceil(length / minimumLengthOfIntervals), maxNumberOfSubIntervals);
		double stepSize = length / numberOfIntervals;
		for (int i = 0; i < numberOfIntervals; i++) {
			intervals.add(new Interval(min + i * stepSize, min + ((i + 1) * stepSize)));
		}
		return intervals;
	}

	public List<Interval> refineOnLogScale(final Interval interval, final int n, final double basis,
			final double pointOfConcentration) {
		List<Interval> list = new ArrayList<>();
		double min = interval.getInf();
		double max = interval.getSup();
		double length = max - min;
		/*
		 * if the point of concentration is exactly on the left or the right of the
		 * interval, conduct the standard technique
		 */
		if (pointOfConcentration <= min || pointOfConcentration >= max) {
			double lengthOfShortestInterval = length * (1 - basis) / (1 - Math.pow(basis, n));
			if (pointOfConcentration <= min) {
				double endOfLast = min;
				for (int i = 0; i < n; i++) {
					double start = endOfLast;
					endOfLast = start + Math.pow(basis, i) * lengthOfShortestInterval;
					list.add(new Interval(start, endOfLast));
				}
			} else {
				double endOfLast = max;
				for (int i = 0; i < n; i++) {
					double start = endOfLast;
					endOfLast = start - Math.pow(basis, i) * lengthOfShortestInterval;
					list.add(new Interval(endOfLast, start));
				}
				Collections.reverse(list);
			}
			return list;
		}
		/*
		 * if the point of concentration is in the inner of the interval, split the
		 * interval correspondingly and recursively solve the problem
		 */
		double distanceFromMinToFocus = Math.abs(interval.getInf() - pointOfConcentration);
		int segmentsForLeft = (int) Math.max(1, Math.floor(n * distanceFromMinToFocus / length));
		int segmentsForRight = n - segmentsForLeft;
		list.addAll(this.refineOnLogScale(new Interval(min, pointOfConcentration), segmentsForLeft, basis,
				pointOfConcentration));
		list.addAll(this.refineOnLogScale(new Interval(pointOfConcentration, max), segmentsForRight, basis,
				pointOfConcentration));
		return list;
	}

	public void refineRecursively(final Interval interval, final int maxNumberOfSubIntervalsPerRefinement,
			final double basis, final double pointOfConcentration,
			final double factorForMaximumLengthOfFinestIntervals) {
		/* first, do a logarithmic refinement */
		List<Interval> initRefinement = this.refineOnLogScale(interval, maxNumberOfSubIntervalsPerRefinement, basis,
				pointOfConcentration);
		Collections.reverse(initRefinement);
		Stack<Interval> openRefinements = new Stack<>();
		openRefinements.addAll(initRefinement);
		int depth = 0;
		do {
			Interval intervalToRefine = openRefinements.pop();
			for (int i = 0; i < depth; i++) {
				System.out.print("\t");
			}
			System.out.println("[" + intervalToRefine.getInf() + ", " + intervalToRefine.getSup() + "]");
			/* compute desired granularity for this specific interval */
			double distanceToPointOfContentration = Math.min(Math.abs(intervalToRefine.getInf() - pointOfConcentration),
					Math.abs(intervalToRefine.getSup() - pointOfConcentration));
			double maximumLengthOfFinestIntervals = Math.pow(distanceToPointOfContentration + 1, 2)
					* factorForMaximumLengthOfFinestIntervals;
			System.out.println(Math.pow(distanceToPointOfContentration + 1, 2) + " * "
					+ factorForMaximumLengthOfFinestIntervals + " = " + maximumLengthOfFinestIntervals);
			List<Interval> refinements = this.refineOnLinearScale(intervalToRefine,
					maxNumberOfSubIntervalsPerRefinement, maximumLengthOfFinestIntervals);
			depth++;
			if (refinements.size() == 1 && refinements.get(0).equals(intervalToRefine)) {
				depth--;
			} else {
				Collections.reverse(refinements);
				openRefinements.addAll(refinements);
			}
		} while (!openRefinements.isEmpty());
	}

}