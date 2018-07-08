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
		if (performanceKB.getImportanceOfParam(param) < importanceThreshold) {
			return new ArrayList<>();
		}

		/* determine refinements for numeric parameters */
		if (param.isNumeric()) {
			NumericParameterDomain currentlyActiveDomain = (NumericParameterDomain) paramDomains.get(param);
			Interval currentInterval = new Interval(currentlyActiveDomain.getMin(), currentlyActiveDomain.getMax());

			/* if there is nothing to refine anymore */
			if (currentInterval.getInf() == currentInterval.getSup()) {
				return new ArrayList<>();
			}

			ParameterRefinementConfiguration refinementConfig = this.refinementConfiguration.get(component).get(param);
			if (refinementConfig == null) {
				throw new IllegalArgumentException("No refinement configuration for parameter \"" + parameterName
						+ "\" of component \"" + componentName + "\" has been supplied!");
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
				return this.getGroundingsForIntervals(proposedRefinements, partialGroundingAsList);
			}

			/*
			 * if the interval is already below the threshold for this parameter, no more
			 * refinements will be allowed
			 */
			if (currentInterval.getSup() - currentInterval.getInf() < refinementConfig.getIntervalLength()) {
				return new ArrayList<>();
			}

			if (!refinementConfig.isInitRefinementOnLogScale()) {
				List<Interval> proposedRefinements = this.refineOnLinearScale(currentInterval,
						refinementConfig.getRefinementsPerStep(), refinementConfig.getIntervalLength());
				for (Interval proposedRefinement : proposedRefinements) {
					assert proposedRefinement.getInf() >= currentInterval.getInf()
							&& proposedRefinement.getSup() <= currentInterval.getSup() : "The proposed refinement ["
									+ proposedRefinement.getInf() + ", " + proposedRefinement.getSup()
									+ "] is not a sub-interval of " + currentParamValue + "].";
					assert !proposedRefinement.equals(currentInterval) : "No real refinement! Intervals are identical.";
				}
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
			return this.getGroundingsForIntervals(proposedRefinements, partialGroundingAsList);

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

	// private double expM(double x, double basis, double center) {
	// x -= center;
	// if (x == 0)
	// return 0;
	// if (x > 0)
	// return Math.pow(basis, x) - 1;
	// else
	// return -1 * Math.pow(basis, -1 * x) + 1;
	// }
	//
	// private double logM(double x, double basis, double center) {
	// if (x == 0)
	// return 0;
	// if (x > 0)
	// return Math.log(x + 1) / Math.log(basis);
	// else
	// return -1 * Math.log(-1 * x + 1) / Math.log(basis);
	// }

	// private double log2lin(double point, double basis, double center) {
	// point += -center + 1;
	// if (point > 1)
	// point = Math.log(point) / Math.log(basis);
	// else if (point < 1)
	// point = -1 * Math.log(-1 * point) / Math.log(basis);
	// else
	// throw new UnsupportedOperationException();
	// return point;
	// }
	//
	// private double lin2log(double point, double basis, double center) {
	// return (point < 0 ? (-1 * Math.pow(basis, -1 * point)) : Math.pow(basis,
	// point));
	// }

	// public List<Interval> refineOnLogScale(Interval interval, int
	// numberOfSubIntervals, double basis,
	// double center) {
	//
	// /* adjust borders by center value */
	// double min = logM(interval.getInf(), basis, center);
	// double max = logM(interval.getSup(), basis, center);
	//
	// /* perform the refinement on the linear scale */
	// System.out.println("Now refining on linearized log-scale [" + min + ", " +
	// max + "]");
	// Interval modifiedInterval = new Interval(min, max);
	// List<Interval> linearRefinements = refineOnLinearScale(modifiedInterval,
	// numberOfSubIntervals,
	// .0001);
	// System.out.println("The linear refinements are:");
	// linearRefinements.forEach(i -> System.out.println("\t" + "[" + i.getInf() +
	// ", " + i.getSup() +
	// "]"));
	// System.out.println();
	//
	// /* recover the log-scale intervals */
	// List<Interval> logScaleRefinements = new ArrayList<>();
	// for (Interval i : linearRefinements) {
	// double recoveredInf = expM(i.getInf(), basis, center);
	// double recoveredSup = expM(i.getSup(), basis, center);
	// // System.out.println(recoveredInf + ", ");
	// System.out.println("[" + i.getInf() + ", " + i.getSup() + "] -> [" +
	// recoveredInf + ", " +
	// recoveredSup + "]");
	// // logScaleRefinements.add(new Interval(Math.pow(basis, i.getInf()) + center,
	// Math.pow(basis,
	// i.getSup()) + center));
	// }
	// return logScaleRefinements;
	// }

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
