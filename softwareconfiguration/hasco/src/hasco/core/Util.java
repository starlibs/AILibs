package hasco.core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import org.apache.commons.math3.geometry.euclidean.oned.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hasco.model.CategoricalParameterDomain;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.model.Dependency;
import hasco.model.NumericParameterDomain;
import hasco.model.Parameter;
import hasco.model.ParameterDomain;
import hasco.model.ParameterRefinementConfiguration;
import jaicore.basic.sets.SetUtil;
import jaicore.basic.sets.SetUtil.Pair;
import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.Monom;
import jaicore.planning.graphgenerators.IPlanningGraphGeneratorDeriver;
import jaicore.planning.model.core.Action;
import jaicore.planning.model.core.Plan;
import jaicore.planning.model.core.PlannerUtil;
import jaicore.search.model.travesaltree.Node;

public class Util {

	private static final Logger logger = LoggerFactory.getLogger(Util.class);

	static Map<String, String> getParameterContainerMap(final Monom state, final String objectName) {
		Map<String, String> parameterContainerMap = new HashMap<>();
		List<Literal> containerLiterals = state.stream().filter(l -> l.getPropertyName().equals("parameterContainer") && l.getParameters().get(2).getName().equals(objectName))
				.collect(Collectors.toList());
		containerLiterals.forEach(l -> parameterContainerMap.put(l.getParameters().get(1).getName(), l.getParameters().get(3).getName()));
		return parameterContainerMap;
	}

	public static Map<ComponentInstance, Map<Parameter, String>> getParametrizations(final Monom state, final Collection<Component> components, final boolean resolveIntervals) {
		Map<String, ComponentInstance> objectMap = new HashMap<>();
		Map<String, Map<String, String>> parameterContainerMap = new HashMap<>(); // stores for each object the name of the container of each parameter
		Map<String, String> parameterValues = new HashMap<>();

		Map<ComponentInstance, Map<Parameter, String>> parameterValuesPerComponentInstance = new HashMap<>();

		Collection<String> overwrittenDataContainers = getOverwrittenDatacontainersInState(state);

		/*
		 * create (empty) component instances, detect containers for parameter values, and register the
		 * values of the data containers
		 */
		for (Literal l : state) {
			String[] params = l.getParameters().stream().map(p -> p.getName()).collect(Collectors.toList()).toArray(new String[] {});
			switch (l.getPropertyName()) {
			case "resolves":
				// String parentObjectName = params[0];
				// String interfaceName = params[1];
				String componentName = params[2];
				String objectName = params[3];
				Component component = components.stream().filter(c -> c.getName().equals(componentName)).findAny().get();
				ComponentInstance object = new ComponentInstance(component, new HashMap<>(), new HashMap<>());
				objectMap.put(objectName, object);
				break;
			case "parameterContainer":
				if (!parameterContainerMap.containsKey(params[2])) {
					parameterContainerMap.put(params[2], new HashMap<>());
				}
				parameterContainerMap.get(params[2]).put(params[1], params[3]);
				break;
			case "val":
				if (overwrittenDataContainers.contains(params[0])) {
					parameterValues.put(params[0], params[1]);
				}
				break;
			}
		}

		/* update the configurations of the objects */
		for (String objectName : objectMap.keySet()) {
			Map<Parameter, String> paramValuesForThisComponent = new HashMap<>();
			ComponentInstance object = objectMap.get(objectName);
			parameterValuesPerComponentInstance.put(object, paramValuesForThisComponent);
			for (Parameter p : object.getComponent().getParameters()) {

				assert parameterContainerMap.containsKey(objectName) : "No parameter container map has been defined for object " + objectName + " of component " + object.getComponent().getName()
						+ "!";
				assert parameterContainerMap.get(objectName).containsKey(p.getName()) : "The data container for parameter " + p.getName() + " of " + object.getComponent().getName()
						+ " is not defined!";

				String assignedValue = parameterValues.get(parameterContainerMap.get(objectName).get(p.getName()));
				String interpretedValue = "";
				if (assignedValue != null) {
					if (p.getDefaultDomain() instanceof NumericParameterDomain) {
						if (resolveIntervals) {
							NumericParameterDomain np = (NumericParameterDomain) p.getDefaultDomain();
							List<String> vals = SetUtil.unserializeList(assignedValue);
							Interval interval = new Interval(Double.valueOf(vals.get(0)), Double.valueOf(vals.get(1)));
							if (np.isInteger()) {
								interpretedValue = String.valueOf((int) Math.round(interval.getBarycenter()));
							} else {
								interpretedValue = String.valueOf(interval.getBarycenter());
							}
						} else {
							interpretedValue = assignedValue;
						}
					} else if (p.getDefaultDomain() instanceof CategoricalParameterDomain) {
						interpretedValue = assignedValue;
					} else {
						throw new UnsupportedOperationException("No support for parameters of type " + p.getClass().getName());
					}
					paramValuesForThisComponent.put(p, interpretedValue);
				}
			}
		}
		return parameterValuesPerComponentInstance;
	}

	public static Collection<String> getOverwrittenDatacontainersInState(final Monom state) {
		return state.stream().filter(l -> l.getPropertyName().equals("overwritten")).map(l -> l.getParameters().get(0).getName()).collect(Collectors.toSet());
	}

	public static Collection<String> getClosedDatacontainersInState(final Monom state) {
		return state.stream().filter(l -> l.getPropertyName().equals("closed")).map(l -> l.getParameters().get(0).getName()).collect(Collectors.toSet());
	}

	static Map<String, ComponentInstance> getGroundComponentsFromState(final Monom state, final Collection<Component> components, final boolean resolveIntervals) {
		Map<String, ComponentInstance> objectMap = new HashMap<>();
		Map<String, Map<String, String>> parameterContainerMap = new HashMap<>(); // stores for each object the name of the container of each parameter
		Map<String, String> parameterValues = new HashMap<>();
		Map<String, String> interfaceContainerMap = new HashMap<>();
		Collection<String> overwrittenDatacontainers = getOverwrittenDatacontainersInState(state);

		/* create (empty) component instances, detect containers for parameter values, and register the values of the data containers */
		for (Literal l : state) {
			String[] params = l.getParameters().stream().map(p -> p.getName()).collect(Collectors.toList()).toArray(new String[] {});
			switch (l.getPropertyName()) {
			case "resolves":
				// String parentObjectName = params[0];
				// String interfaceName = params[1];
				String componentName = params[2];
				String objectName = params[3];

				Component component = components.stream().filter(c -> c.getName().equals(componentName)).findAny().get();
				ComponentInstance object = new ComponentInstance(component, new HashMap<>(), new HashMap<>());
				objectMap.put(objectName, object);
				break;
			case "parameterContainer":
				if (!parameterContainerMap.containsKey(params[2])) {
					parameterContainerMap.put(params[2], new HashMap<>());
				}
				parameterContainerMap.get(params[2]).put(params[1], params[3]);
				break;
			case "val":
				parameterValues.put(params[0], params[1]);
				break;
			case "interfaceIdentifier":
				interfaceContainerMap.put(params[3], params[1]);
				break;
			}
		}

		/* now establish the binding of the required interfaces of the component instances */
		state.stream().filter(l -> l.getPropertyName().equals("resolves")).forEach(l -> {
			String[] params = l.getParameters().stream().map(p -> p.getName()).collect(Collectors.toList()).toArray(new String[] {});
			String parentObjectName = params[0];
			// String interfaceName = params[1];
			String objectName = params[3];

			ComponentInstance object = objectMap.get(objectName);
			if (!parentObjectName.equals("request")) {
				assert interfaceContainerMap.containsKey(objectName) : "Object name " + objectName + " for requried interface must have a defined identifier ";
				objectMap.get(parentObjectName).getSatisfactionOfRequiredInterfaces().put(interfaceContainerMap.get(objectName), object);
			}
		});

		/* set the explicitly defined parameters (e.g. overwritten containers) in the component instances */
		for (String objectName : objectMap.keySet()) {
			ComponentInstance object = objectMap.get(objectName);
			for (Parameter p : object.getComponent().getParameters()) {

				assert parameterContainerMap.containsKey(objectName) : "No parameter container map has been defined for object " + objectName + " of component " + object.getComponent().getName()
						+ "!";
				assert parameterContainerMap.get(objectName).containsKey(p.getName()) : "The data container for parameter " + p.getName() + " of " + object.getComponent().getName()
						+ " is not defined!";
				String paramContainerName = parameterContainerMap.get(objectName).get(p.getName());
				if (overwrittenDatacontainers.contains(paramContainerName)) {
					String assignedValue = parameterValues.get(paramContainerName);
					assert assignedValue != null : "parameter containers must always have a value!";
					object.getParameterValues().put(p.getName(), getParamValue(p, assignedValue, resolveIntervals));
				}
			}
		}
		return objectMap;
	}

	public static <N, A, V extends Comparable<V>> ComponentInstance getSolutionCompositionForNode(final IPlanningGraphGeneratorDeriver<?, ?, ?, ?, N, A> planningGraphDeriver,
			final Collection<Component> components, final Monom initState, final Node<N, ?> path, final boolean resolveIntervals) {
		return getSolutionCompositionForPlan(components, initState, planningGraphDeriver.getPlan(path.externalPath()), resolveIntervals);
	}

	public static <N, A, V extends Comparable<V>> ComponentInstance getComponentInstanceForNode(final IPlanningGraphGeneratorDeriver<?, ?, ?, ?, N, A> planningGraphDeriver,
			final Collection<Component> components, final Monom initState, final Node<N, ?> path, String name, final boolean resolveIntervals) {
		return getComponentInstanceForPlan(components, initState, planningGraphDeriver.getPlan(path.externalPath()), name, resolveIntervals);
	}

	public static Monom getFinalStateOfPlan(final Monom initState, final Plan<? extends Action> plan) {
		Monom state = new Monom(initState);
		for (Action a : plan.getActions()) {
			PlannerUtil.updateState(state, a);
		}
		return state;
	}

	public static ComponentInstance getSolutionCompositionForPlan(final Collection<Component> components, final Monom initState, final Plan<? extends Action> plan, final boolean resolveIntervals) {
		return getSolutionCompositionFromState(components, getFinalStateOfPlan(initState, plan), resolveIntervals);
	}

	public static ComponentInstance getComponentInstanceForPlan(final Collection<Component> components, final Monom initState, final Plan<? extends Action> plan, String name,
			final boolean resolveIntervals) {
		return getComponentInstanceFromState(components, getFinalStateOfPlan(initState, plan), name, resolveIntervals);
	}

	public static ComponentInstance getSolutionCompositionFromState(final Collection<Component> components, final Monom state, final boolean resolveIntervals) {
		return getComponentInstanceFromState(components, state, "solution", resolveIntervals);
	}

	public static ComponentInstance getComponentInstanceFromState(final Collection<Component> components, final Monom state, String name, final boolean resolveIntervals) {
		return Util.getGroundComponentsFromState(state, components, resolveIntervals).get(name);
	}

	/**
	 * Computes a String of component names that appear in the composition which can be used as an identifier for the composition
	 * 
	 * @param composition
	 * @return String of all component names in right to left depth-first order
	 */
	public static String getComponentNamesOfComposition(ComponentInstance composition) {
		StringBuilder builder = new StringBuilder();
		Deque<ComponentInstance> componentInstances = new ArrayDeque<ComponentInstance>();
		componentInstances.push(composition);
		ComponentInstance curInstance;
		while (!componentInstances.isEmpty()) {
			curInstance = componentInstances.pop();
			builder.append(curInstance.getComponent().getName());
			LinkedHashMap<String, String> requiredInterfaces = curInstance.getComponent().getRequiredInterfaces();
			// This set should be ordered
			Set<String> requiredInterfaceNames = requiredInterfaces.keySet();
			for (String requiredInterfaceName : requiredInterfaceNames) {
				ComponentInstance instance = curInstance.getSatisfactionOfRequiredInterfaces().get(requiredInterfaceName);
				componentInstances.push(instance);
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
	public static List<Component> getComponentsOfComposition(ComponentInstance composition) {
		List<Component> components = new LinkedList<Component>();
		Deque<ComponentInstance> componentInstances = new ArrayDeque<ComponentInstance>();
		componentInstances.push(composition);
		ComponentInstance curInstance;
		while (!componentInstances.isEmpty()) {
			curInstance = componentInstances.pop();
			components.add(curInstance.getComponent());
			LinkedHashMap<String, String> requiredInterfaces = curInstance.getComponent().getRequiredInterfaces();
			// This set should be ordered
			Set<String> requiredInterfaceNames = requiredInterfaces.keySet();
			for (String requiredInterfaceName : requiredInterfaceNames) {
				ComponentInstance instance = curInstance.getSatisfactionOfRequiredInterfaces().get(requiredInterfaceName);
				componentInstances.push(instance);
			}
		}
		return components;
	}

	public static Map<Parameter, ParameterDomain> getUpdatedDomainsOfComponentParameters(final Monom state, final Component component, final String objectIdentifierInState) {
		Map<String, String> parameterContainerMap = new HashMap<>();
		Map<String, String> parameterContainerMapInv = new HashMap<>();
		Map<String, String> parameterValues = new HashMap<>();

		/* detect containers for parameter values, and register the values of the data containers */
		for (Literal l : state) {
			String[] params = l.getParameters().stream().map(p -> p.getName()).collect(Collectors.toList()).toArray(new String[] {});
			switch (l.getPropertyName()) {
			case "parameterContainer":
				if (!params[2].equals(objectIdentifierInState)) {
					continue;
				}
				parameterContainerMap.put(params[1], params[3]);
				parameterContainerMapInv.put(params[3], params[1]);
				break;
			case "val":
				parameterValues.put(params[0], params[1]);
				break;
			}
		}

		/* determine current values of the parameters of this component instance */
		Map<Parameter, String> paramValuesForThisComponentInstance = new HashMap<>();
		for (Parameter p : component.getParameters()) {
			assert parameterContainerMap.containsKey(p.getName()) : "The data container for parameter " + p.getName() + " of " + objectIdentifierInState + " is not defined!";
			String assignedValue = parameterValues.get(parameterContainerMap.get(p.getName()));
			assert assignedValue != null : "No value has been assigned to parameter " + p.getName() + " stored in container " + parameterContainerMap.get(p.getName()) + " in state " + state;
			String value = getParamValue(p, assignedValue, false);
			assert value != null : "Determined value NULL for parameter " + p.getName() + ", which is not plausible.";
			paramValuesForThisComponentInstance.put(p, value);
		}

		/* extract instance */
		Collection<Component> components = new ArrayList<>();
		components.add(component);
		ComponentInstance instance = getComponentInstanceFromState(components, state, objectIdentifierInState, false);

		/* now compute the new domains based on the current values */
		Collection<Parameter> overwrittenParams = getOverwrittenDatacontainersInState(state).stream().filter(containerName -> parameterContainerMap.containsValue(containerName))
				.map(containerName -> component.getParameterWithName(parameterContainerMapInv.get(containerName))).collect(Collectors.toList());
		return getUpdatedDomainsOfComponentParameters(instance);
		// return null;
	}

	// public static Map<Parameter, ParameterDomain> getUpdatedDomainsOfComponentParameters(final ComponentInstance instance) {
	//
	// /* detect containers for parameter values, and register the values of the data containers */
	// Component component = instance.getComponent();
	//
	// /* determine current values of the parameters of this component instance */
	// Map<Parameter, String> paramValuesForThisComponentInstance = new HashMap<>();
	// for (Parameter p : component.getParameters()) {
	// String assignedValue = instance.getParameterValues().get(p.getName());
	// String value = getParamValue(p, assignedValue, false);
	// paramValuesForThisComponentInstance.put(p, value);
	// }
	//
	// /* now compute the new domains based on the current values */
	//// Collection<Parameter> overwrittenParams = getOverwrittenDatacontainersInState(state).stream().filter(containerName -> parameterContainerMap.containsValue(containerName))
	// // .map(containerName -> component.getParameter(parameterContainerMapInv.get(containerName))).collect(Collectors.toList());
	// return getUpdatedDomainsOfComponentParameters(component, paramValuesForThisComponentInstance, new ArrayList<>());
	// }

	private static String getParamValue(final Parameter p, final String assignedValue, final boolean resolveIntervals) {
		String interpretedValue = "";
		if (assignedValue == null) {
			throw new IllegalArgumentException("Cannot determine true value for assigned param value " + assignedValue + " for parameter " + p.getName());
		}
		if (p.isNumeric()) {
			if (resolveIntervals) {
				NumericParameterDomain np = (NumericParameterDomain) p.getDefaultDomain();
				List<String> vals = SetUtil.unserializeList(assignedValue);
				Interval interval = new Interval(Double.valueOf(vals.get(0)), Double.valueOf(vals.get(1)));
				if (np.isInteger()) {
					interpretedValue = String.valueOf((int) Math.round(interval.getBarycenter()));
				} else {
					interpretedValue = String.valueOf(interval.getBarycenter());
				}
			} else {
				interpretedValue = assignedValue;
			}
		} else if (p.getDefaultDomain() instanceof CategoricalParameterDomain) {
			interpretedValue = assignedValue;
		} else {
			throw new UnsupportedOperationException("No support for parameters of type " + p.getClass().getName());
		}
		return interpretedValue;
	}

	public static Map<Parameter, ParameterDomain> getUpdatedDomainsOfComponentParameters(final ComponentInstance componentInstance) {
		Component component = componentInstance.getComponent();

		/* initialize all params for which a decision has been made already with their respective value */
		Map<Parameter, ParameterDomain> domains = new HashMap<>();
		for (Parameter p : componentInstance.getParametersThatHaveBeenSetExplicitly()) {
			if (p.isNumeric()) {
				NumericParameterDomain defaultDomain = (NumericParameterDomain) p.getDefaultDomain();
				Interval interval = SetUtil.unserializeInterval(componentInstance.getParameterValue(p));
				domains.put(p, new NumericParameterDomain(defaultDomain.isInteger(), interval.getInf(), interval.getSup()));
			} else if (p.isCategorical()) {
				domains.put(p, new CategoricalParameterDomain(new String[] { componentInstance.getParameterValue(p) }));
			}
		}

		/* initialize all others with the default domain */
		for (Parameter p : componentInstance.getParametersThatHaveNotBeenSetExplicitly()) {
			domains.put(p, p.getDefaultDomain());
		}
		assert (domains.keySet().equals(component.getParameters())) : "There are parameters for which no current domain was derived.";

		/* update domains based on the dependencies defined for this component */
		for (Dependency dependency : component.getDependencies()) {
			if (isDependencyPremiseSatisfied(dependency, domains)) {
				logger.info("Premise of dependency {} is satisfied, applying its conclusions ...", dependency);
				for (Pair<Parameter, ParameterDomain> newDomain : dependency.getConclusion()) {
					/*
					 * directly use the concluded domain if the current value is NOT subsumed by it. Otherwise, just
					 * stick to the current domain
					 */
					Parameter param = newDomain.getX();
					ParameterDomain concludedDomain = newDomain.getY();
					if (!componentInstance.getParametersThatHaveBeenSetExplicitly().contains(param)) {
						domains.put(param, concludedDomain);
						logger.debug("Changing domain of {} from {} to {}", param, domains.get(param), concludedDomain);
					} else {
						logger.debug("Not changing domain of {} since it has already been set explicitly in the past.", param);
					}
					// }
					// else
					// logger.debug("Not changing domain of {} from {} to {}, because the current domain is already narrower.", newDomain.getX(), domains.get(newDomain.getX()), newDomain.getY());

					// ParameterDomain intersection = null;
					// if (param.isNumeric()) {
					// NumericParameterDomain cConcludedDomain = (NumericParameterDomain)concludedDomain;
					// NumericParameterDomain currentDomain = (NumericParameterDomain)domains.get(newDomain.getX());
					// intersection = new NumericParameterDomain(cConcludedDomain.isInteger(),
					// Math.max(cConcludedDomain.getMin(), currentDomain.getMin()), Math.min(cConcludedDomain.getMax(),
					// currentDomain.getMax()));
					// }
					// else if (param.isCategorical()) {
					// CategoricalParameterDomain cConcludedDomain = (CategoricalParameterDomain)concludedDomain;
					// CategoricalParameterDomain currentDomain =
					// (CategoricalParameterDomain)domains.get(newDomain.getX());
					// intersection = new
					// CategoricalParameterDomain(SetUtil.intersection(Arrays.asList(cConcludedDomain.getValues()),
					// Arrays.asList(currentDomain.getValues())));
					// }
					// else
					// throw new UnsupportedOperationException("Cannot currently handle parameters that are not numeric
					// and not categorical.");
					// assert intersection != null : "The intersection of the current domain and the domain dictated by
					// a rule has failed";
					// domains.put(param, intersection);
				}
			} else
				logger.debug("Ignoring unsatisfied dependency {}.", dependency);
		}
		return domains;
	}

	public static boolean isDependencyPremiseSatisfied(final Dependency dependency, final Map<Parameter, ParameterDomain> values) {
		logger.debug("Checking satisfcation of dependency {} with values {}", dependency, values);
		for (Collection<Pair<Parameter, ParameterDomain>> condition : dependency.getPremise()) {
			boolean check = isDependencyConditionSatisfied(condition, values);
			logger.trace("Result of check for condition {}: {}", condition, check);
			if (!check)
				return false;
		}
		return true;
	}

	public static boolean isDependencyConditionSatisfied(final Collection<Pair<Parameter, ParameterDomain>> condition, final Map<Parameter, ParameterDomain> values) {
		for (Pair<Parameter, ParameterDomain> conditionItem : condition) {
			ParameterDomain requiredDomain = conditionItem.getY();
			Parameter param = conditionItem.getX();
			ParameterDomain actualDomain = values.get(param);
			assert values.containsKey(param) : "Cannot check condition " + condition + " as the value for parameter " + param.getName() + " is not defined in " + values;
			assert values.get(param) != null : "Cannot check condition " + condition + " as the value for parameter " + param.getName() + " is NULL in " + values;
			if (!requiredDomain.subsumes(actualDomain))
				return false;
		}
		return true;
	}

	public static List<Interval> getNumericParameterRefinement(final Interval interval, double focus, boolean integer, final ParameterRefinementConfiguration refinementConfig) {

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
				assert proposedRefinement.getInf() >= inf && proposedRefinement.getSup() <= sup : "The proposed refinement [" + proposedRefinement.getInf() + ", " + proposedRefinement.getSup()
						+ "] is not a sub-interval of [" + inf + ", " + sup + "].";
				assert !proposedRefinement.equals(interval) : "No real refinement! Intervals are identical.";
			}
			return proposedRefinements;
		}

		List<Interval> proposedRefinements = refineOnLogScale(interval, refinementConfig.getRefinementsPerStep(), 2, focus);
		for (Interval proposedRefinement : proposedRefinements) {
			double epsilon = 1E-7;
			assert proposedRefinement.getInf() + epsilon >= inf && proposedRefinement.getSup() <= sup + epsilon : "The proposed refinement [" + proposedRefinement.getInf() + ", "
					+ proposedRefinement.getSup() + "] is not a sub-interval of [" + inf + ", " + sup + "].";
			assert !proposedRefinement.equals(interval) : "No real refinement! Intervals are identical.";
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

	public static void refineRecursively(final Interval interval, final int maxNumberOfSubIntervalsPerRefinement, final double basis, final double pointOfConcentration,
			final double factorForMaximumLengthOfFinestIntervals) {

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
			double distanceToPointOfContentration = Math.min(Math.abs(intervalToRefine.getInf() - pointOfConcentration), Math.abs(intervalToRefine.getSup() - pointOfConcentration));
			double maximumLengthOfFinestIntervals = Math.pow(distanceToPointOfContentration + 1, 2) * factorForMaximumLengthOfFinestIntervals;
			System.out.println(Math.pow(distanceToPointOfContentration + 1, 2) + " * " + factorForMaximumLengthOfFinestIntervals + " = " + maximumLengthOfFinestIntervals);
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

	public static boolean isDefaultConfiguration(ComponentInstance instance) {
		for (Parameter p : instance.getParametersThatHaveBeenSetExplicitly()) {
			if (p.isNumeric()) {
				List<String> intervalAsList = SetUtil.unserializeList(instance.getParameterValue(p));
				double defaultValue = Double.parseDouble(p.getDefaultValue().toString());
				boolean isCompatibleWithDefaultValue = defaultValue >= Double.parseDouble(intervalAsList.get(0)) && defaultValue <= Double.parseDouble(intervalAsList.get(1));
				if (!isCompatibleWithDefaultValue) {
					logger.info(p.getName() + " has value " + instance.getParameterValue(p) + ", which does not subsume the default value " + defaultValue);
					return false;
				}
				else
					logger.info(p.getName() + " has value " + instance.getParameterValue(p) + ", which IS COMPATIBLE with the default value " + defaultValue);
			} else {
				if (!instance.getParameterValue(p).equals(p.getDefaultValue().toString())) {
					logger.info(p.getName() + " has value " + instance.getParameterValue(p) + ", which is not the default " + p.getDefaultValue().toString());
					return false;
				}
			}
		}
		for (ComponentInstance child : instance.getSatisfactionOfRequiredInterfaces().values()) {
			if (!isDefaultConfiguration(child))
				return false;
		}
		return true;
	}
}
