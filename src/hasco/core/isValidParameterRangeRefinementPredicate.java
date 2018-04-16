package hasco.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;

import org.apache.commons.math3.geometry.euclidean.oned.Interval;

import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.model.NumericParameter;
import hasco.model.Parameter;
import hasco.model.ParameterRefinementConfiguration;
import jaicore.basic.SetUtil;
import jaicore.logic.fol.structure.ConstantParam;
import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.Monom;
import jaicore.planning.graphgenerators.task.ceociptfd.EvaluablePredicate;

public class isValidParameterRangeRefinementPredicate implements EvaluablePredicate {

	private final Collection<Component> components;
	private final Map<Component,Map<Parameter, ParameterRefinementConfiguration>> refinementConfiguration;
	private final Map<ComponentInstance,Double> knownCompositionsAndTheirScore = new HashMap<>();

	public isValidParameterRangeRefinementPredicate(Collection<Component> components, Map<Component,Map<Parameter, ParameterRefinementConfiguration>> refinementConfiguration) {
		super();
		this.components = components;
		this.refinementConfiguration = refinementConfiguration;
	}
	
	@Override
	public Collection<List<ConstantParam>> getParamsForPositiveEvaluation(Monom state, ConstantParam... partialGrounding) {
		
		/* determine the context for which the interval refinement should be oracled */
		if (partialGrounding.length != 5) {
			throw new IllegalArgumentException("The interpreted predicate " + this.getClass().getName() + " requires 5 arguments when oracled but " + partialGrounding.length + " have been provided!");
		}
		String componentName = partialGrounding[0].getName();
		String componentIdentifier = partialGrounding[1].getName();
		String parameterName = partialGrounding[2].getName();
		Component component = components.stream().filter(c -> c.getName().equals(componentName)).findAny().get();
		Parameter param = component.getParameters().stream().filter(p -> p.getName().equals(parameterName)).findAny().get();
		
		/* determine refinements for numeric parameters */
		if (param instanceof NumericParameter) {
			List<String> currentIntervalAsString = SetUtil.unserializeList(partialGrounding[3].getName());
			Interval currentInterval = new Interval(Double.parseDouble(currentIntervalAsString.get(0)), Double.parseDouble(currentIntervalAsString.get(1)));
			
			ParameterRefinementConfiguration refinementConfig = refinementConfiguration.get(component).get(param);
			if (refinementConfig == null)
				throw new IllegalArgumentException("No refinement configuration for parameter \"" + parameterName + "\" of component \"" + componentName + "\" has been supplied!");
			
			/* if the interval is already below the threshold for this parameter, no more refinements will be allowed */
			if (currentInterval.getSup() - currentInterval.getInf() < refinementConfig.getIntervalLength())
				return new ArrayList<>();
			
			if (!refinementConfig.isInitRefinementOnLogScale()) {
				return getGroundingsForIntervals(refineOnLinearScale(currentInterval, refinementConfig.getRefinementsPerStep(), refinementConfig.getIntervalLength()), Arrays.asList(partialGrounding));
			}
			
			Optional<Literal> focusPredicate = state.stream().filter(l -> l.getPropertyName().equals("parameterFocus") && l.getParameters().get(0).getName().equals(componentIdentifier) && l.getParameters().get(1).getName().equals(parameterName)).findAny();
			if (!focusPredicate.isPresent())
				throw new IllegalArgumentException("The given state does not specify a parameter focus for the log-scale parameter " + parameterName + " on object \"" + componentIdentifier + "\"");
			double focus = Double.parseDouble(focusPredicate.get().getParameters().get(2).getName());
			
			return getGroundingsForIntervals(refineOnLogScale(currentInterval, refinementConfig.getRefinementsPerStep(), 2, focus), Arrays.asList(partialGrounding));
			
		} else
			throw new UnsupportedOperationException("Currently no support for parameters of class \"" + param.getClass().getName() + "\"");
		
//		throw new NotImplementedException("Apparentely, there is an unimplemented case!");
	}
	
	private Collection<List<ConstantParam>> getGroundingsForIntervals(List<Interval> refinements, List<ConstantParam> partialGrounding) {
		Collection<List<ConstantParam>> groundings = new ArrayList<>();
		for (Interval oracledInterval : refinements) {
			List<ConstantParam> grounding = new ArrayList<>(partialGrounding);
			grounding.set(4, new ConstantParam("[" + oracledInterval.getInf() + ", " + oracledInterval.getSup() + "]"));
			groundings.add(grounding);
		}
//		System.out.println("[" + currentInterval.getInf() + "," + currentInterval.getSup() + "] with " + refinementConfig.getRefinementsPerStep() + " steps and min interval length " + refinementConfig.getIntervalLength() + " -> " + groundings);
		return groundings;
	}
	
	public void informAboutNewSolution(ComponentInstance solution, double score) {
		this.knownCompositionsAndTheirScore.put(solution, score);
	}

	@Override
	public boolean isOracable() {
		return true;
	}

	@Override
	public Collection<List<ConstantParam>> getParamsForNegativeEvaluation(Monom state, ConstantParam... partialGrounding) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean test(Monom state, ConstantParam... params) {
		return false;
	}

	public List<Interval> refineOnLinearScale(Interval interval, int maxNumberOfSubIntervals, double minimumLengthOfIntervals) {
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
	// return (point < 0 ? (-1 * Math.pow(basis, -1 * point)) : Math.pow(basis, point));
	// }

	// public List<Interval> refineOnLogScale(Interval interval, int numberOfSubIntervals, double basis, double center) {
	//
	// /* adjust borders by center value */
	// double min = logM(interval.getInf(), basis, center);
	// double max = logM(interval.getSup(), basis, center);
	//
	// /* perform the refinement on the linear scale */
	// System.out.println("Now refining on linearized log-scale [" + min + ", " + max + "]");
	// Interval modifiedInterval = new Interval(min, max);
	// List<Interval> linearRefinements = refineOnLinearScale(modifiedInterval, numberOfSubIntervals, .0001);
	// System.out.println("The linear refinements are:");
	// linearRefinements.forEach(i -> System.out.println("\t" + "[" + i.getInf() + ", " + i.getSup() + "]"));
	// System.out.println();
	//
	// /* recover the log-scale intervals */
	// List<Interval> logScaleRefinements = new ArrayList<>();
	// for (Interval i : linearRefinements) {
	// double recoveredInf = expM(i.getInf(), basis, center);
	// double recoveredSup = expM(i.getSup(), basis, center);
	// // System.out.println(recoveredInf + ", ");
	// System.out.println("[" + i.getInf() + ", " + i.getSup() + "] -> [" + recoveredInf + ", " + recoveredSup + "]");
	// // logScaleRefinements.add(new Interval(Math.pow(basis, i.getInf()) + center, Math.pow(basis, i.getSup()) + center));
	// }
	// return logScaleRefinements;
	// }

	public List<Interval> refineOnLogScale(Interval interval, int n, double basis, double pointOfConcentration) {
		List<Interval> list = new ArrayList<>();
		double min = interval.getInf();
		double max = interval.getSup();
		double length = max - min;

		/* if the point of concentration is exactly on the left or the right of the interval, conduct the standard technique */
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

		/* if the point of concentration is in the inner of the interval, split the interval correspondingly and recursively solve the problem */
		double distanceFromMinToFocus = Math.abs(interval.getInf() - pointOfConcentration);
		int segmentsForLeft = (int) Math.max(1, Math.floor(n * distanceFromMinToFocus / length));
		int segmentsForRight = n - segmentsForLeft;
		list.addAll(refineOnLogScale(new Interval(min, pointOfConcentration), segmentsForLeft, basis, pointOfConcentration));
		list.addAll(refineOnLogScale(new Interval(pointOfConcentration, max), segmentsForRight, basis, pointOfConcentration));
		return list;
	}

	public void refineRecursively(Interval interval, int maxNumberOfSubIntervalsPerRefinement, double basis, double pointOfConcentration,
			double factorForMaximumLengthOfFinestIntervals) {

		/* first, do a logarithmic refinement */
		List<Interval> initRefinement = refineOnLogScale(interval, maxNumberOfSubIntervalsPerRefinement, basis, pointOfConcentration);
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
			double maximumLengthOfFinestIntervals = Math.pow(distanceToPointOfContentration + 1, 2) * factorForMaximumLengthOfFinestIntervals;
			System.out.println(Math.pow(distanceToPointOfContentration + 1, 2) + " * " + factorForMaximumLengthOfFinestIntervals + " = " + maximumLengthOfFinestIntervals);
			List<Interval> refinements = refineOnLinearScale(intervalToRefine, maxNumberOfSubIntervalsPerRefinement, maximumLengthOfFinestIntervals);

			depth++;
			if (refinements.size() == 1 && refinements.get(0).equals(intervalToRefine))
				depth--;
			else {
				Collections.reverse(refinements);
				openRefinements.addAll(refinements);
			}

		} while (!openRefinements.isEmpty());
	}

}
