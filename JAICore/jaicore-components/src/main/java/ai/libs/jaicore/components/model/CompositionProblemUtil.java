package ai.libs.jaicore.components.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.math3.geometry.euclidean.oned.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.components.api.IComponent;
import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.api.INumericParameterRefinementConfiguration;
import ai.libs.jaicore.components.api.IParameter;
import ai.libs.jaicore.components.api.IParameterDependency;
import ai.libs.jaicore.components.api.IParameterDomain;

public class CompositionProblemUtil {

	private static final Logger logger = LoggerFactory.getLogger(CompositionProblemUtil.class);

	private CompositionProblemUtil() {
		/* avoid instantiation */
	}

	public static Collection<IComponent> getComponentsThatResolveProblem(final SoftwareConfigurationProblem<?> configurationProblem) {
		return getComponentsThatProvideInterface(configurationProblem, configurationProblem.getRequiredInterface());
	}

	public static Collection<IComponent> getComponentsThatProvideInterface(final SoftwareConfigurationProblem<?> configurationProblem, final String requiredInterface){
		return configurationProblem.getComponents().stream().filter(c -> c.getProvidedInterfaces().contains(requiredInterface)).collect(Collectors.toList());
	}

	/**
	 * Computes a list of all component instances of the given composition.
	 *
	 * @param composition
	 * @return List of components in right to left depth-first order
	 */
	public static List<IComponentInstance> getComponentInstancesOfComposition(final IComponentInstance composition) {
		List<IComponentInstance> components = new LinkedList<>();
		Deque<IComponentInstance> componentInstances = new ArrayDeque<>();
		componentInstances.push(composition);
		IComponentInstance curInstance;
		while (!componentInstances.isEmpty()) {
			curInstance = componentInstances.pop();
			components.add(curInstance);
			for (Collection<IComponentInstance> instances : curInstance.getSatisfactionOfRequiredInterfaces().values()) {
				instances.forEach(componentInstances::push);
			}
		}
		return components;
	}

	/**
	 * Computes a String of component names that appear in the composition which can be used as an identifier for the composition
	 *
	 * @param composition
	 * @return String of all component names in right to left depth-first order
	 */
	public static String getComponentNamesOfComposition(final IComponentInstance composition) {
		StringBuilder builder = new StringBuilder();
		Deque<IComponentInstance> componentInstances = new ArrayDeque<>();
		componentInstances.push(composition);
		IComponentInstance curInstance;
		while (!componentInstances.isEmpty()) {
			curInstance = componentInstances.pop();
			builder.append(curInstance.getComponent().getName());
			if (curInstance.getSatisfactionOfRequiredInterfaces() != null) {
				for (Collection<IComponentInstance> instances : curInstance.getSatisfactionOfRequiredInterfaces().values()) {
					instances.forEach(componentInstances::push);
				}
			}
		}
		return builder.toString();
	}

	/**
	 * Computes a list of all components of the given composition.
	 *
	 * @param composition
	 * @return List of components in right to left depth-first order
	 */
	public static List<IComponent> getComponentsOfComposition(final IComponentInstance composition) {
		List<IComponent> components = new LinkedList<>();
		Deque<IComponentInstance> componentInstances = new ArrayDeque<>();
		componentInstances.push(composition);
		IComponentInstance curInstance;
		while (!componentInstances.isEmpty()) {
			curInstance = componentInstances.pop();
			components.add(curInstance.getComponent());
			for (Collection<IComponentInstance> instances : curInstance.getSatisfactionOfRequiredInterfaces().values()) {
				instances.forEach(componentInstances::push);
			}
		}
		return components;
	}

	public static boolean isDependencyPremiseSatisfied(final IParameterDependency dependency, final Map<IParameter, IParameterDomain> values) {
		logger.debug("Checking satisfcation of dependency {} with values {}", dependency, values);
		for (Collection<Pair<IParameter, IParameterDomain>> condition : dependency.getPremise()) {
			boolean check = isDependencyConditionSatisfied(condition, values);
			logger.trace("Result of check for condition {}: {}", condition, check);
			if (!check) {
				return false;
			}
		}
		return true;
	}

	public static boolean isDependencyConditionSatisfied(final Collection<Pair<IParameter, IParameterDomain>> condition, final Map<IParameter, IParameterDomain> values) {
		for (Pair<IParameter, IParameterDomain> conditionItem : condition) {
			IParameter param = conditionItem.getX();
			if (!values.containsKey(param)) {
				throw new IllegalArgumentException("Cannot check condition " + condition + " as the value for parameter " + param.getName() + " is not defined in " + values);
			}
			if (values.get(param) == null) {
				throw new IllegalArgumentException("Cannot check condition " + condition + " as the value for parameter " + param.getName() + " is NULL in " + values);
			}
			IParameterDomain requiredDomain = conditionItem.getY();
			IParameterDomain actualDomain = values.get(param);
			if (!requiredDomain.subsumes(actualDomain)) {
				return false;
			}
		}
		return true;
	}

	public static List<Interval> getNumericParameterRefinement(final Interval interval, final double focus, final boolean integer, final INumericParameterRefinementConfiguration refinementConfig) {

		double inf = interval.getInf();
		double sup = interval.getSup();

		/* if there is nothing to refine anymore */
		if (inf == sup) {
			return new ArrayList<>();
		}

		/*
		 * if this is an integer and the number of comprised integers are at most as
		 * many as the branching factor, enumerate them
		 */
		if (integer && (Math.floor(sup) - Math.ceil(inf) + 1 <= refinementConfig.getRefinementsPerStep())) {
			List<Interval> proposedRefinements = new ArrayList<>();
			for (int i = (int) Math.ceil(inf); i <= (int) Math.floor(sup); i++) {
				proposedRefinements.add(new Interval(i, i));
			}
			return proposedRefinements;
		}

		/*
		 * if the interval is already below the threshold for this parameter, no more
		 * refinements will be allowed
		 */
		if (sup - inf < refinementConfig.getIntervalLength()) {
			return new ArrayList<>();
		}

		if (!refinementConfig.isInitRefinementOnLogScale()) {
			List<Interval> proposedRefinements = refineOnLinearScale(interval, refinementConfig.getRefinementsPerStep(), refinementConfig.getIntervalLength());
			for (Interval proposedRefinement : proposedRefinements) {
				assert proposedRefinement.getInf() >= inf && proposedRefinement.getSup() <= sup : "The proposed refinement [" + proposedRefinement.getInf() + ", " + proposedRefinement.getSup() + "] is not a sub-interval of [" + inf + ", "
						+ sup + "].";
				if (proposedRefinement.equals(interval)) {
					throw new IllegalStateException("No real refinement! Intervals are identical.");
				}
			}
			return proposedRefinements;
		}

		List<Interval> proposedRefinements = refineOnLogScale(interval, refinementConfig.getRefinementsPerStep(), 2, focus);
		for (Interval proposedRefinement : proposedRefinements) {
			double epsilon = 1E-7;
			assert proposedRefinement.getInf() + epsilon >= inf && proposedRefinement.getSup() <= sup + epsilon : "The proposed refinement [" + proposedRefinement.getInf() + ", " + proposedRefinement.getSup()
			+ "] is not a sub-interval of [" + inf + ", " + sup + "].";
			if (proposedRefinement.equals(interval)) {
				throw new IllegalStateException("No real refinement! Intervals are identical.");
			}
		}
		return proposedRefinements;
	}

	public static List<Interval> refineOnLinearScale(final Interval interval, final int maxNumberOfSubIntervals, final double minimumLengthOfIntervals) {
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

	public static List<Interval> refineOnLogScale(final Interval interval, final int n, final double basis, final double pointOfConcentration) {
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
		list.addAll(refineOnLogScale(new Interval(min, pointOfConcentration), segmentsForLeft, basis, pointOfConcentration));
		list.addAll(refineOnLogScale(new Interval(pointOfConcentration, max), segmentsForRight, basis, pointOfConcentration));
		return list;
	}

	public static void refineRecursively(final Interval interval, final int maxNumberOfSubIntervalsPerRefinement, final double basis, final double pointOfConcentration, final double factorForMaximumLengthOfFinestIntervals) {

		/* first, do a logarithmic refinement */
		List<Interval> initRefinement = refineOnLogScale(interval, maxNumberOfSubIntervalsPerRefinement, basis, pointOfConcentration);
		Collections.reverse(initRefinement);

		Deque<Interval> openRefinements = new LinkedList<>();
		openRefinements.addAll(initRefinement);
		int depth = 0;
		do {
			Interval intervalToRefine = openRefinements.pop();
			if (logger.isInfoEnabled()) {
				StringBuilder offsetSB = new StringBuilder();
				for (int i = 0; i < depth; i++) {
					offsetSB.append("\t");
				}
				logger.info("{}[{}, {}]", offsetSB, intervalToRefine.getInf(), intervalToRefine.getSup());
			}

			/* compute desired granularity for this specific interval */
			double distanceToPointOfContentration = Math.min(Math.abs(intervalToRefine.getInf() - pointOfConcentration), Math.abs(intervalToRefine.getSup() - pointOfConcentration));
			double maximumLengthOfFinestIntervals = Math.pow(distanceToPointOfContentration + 1, 2) * factorForMaximumLengthOfFinestIntervals;
			logger.info("{} * {} = {}", Math.pow(distanceToPointOfContentration + 1, 2), factorForMaximumLengthOfFinestIntervals, maximumLengthOfFinestIntervals);
			List<Interval> refinements = refineOnLinearScale(intervalToRefine, maxNumberOfSubIntervalsPerRefinement, maximumLengthOfFinestIntervals);

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
