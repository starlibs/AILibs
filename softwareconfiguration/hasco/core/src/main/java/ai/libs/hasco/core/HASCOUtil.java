package ai.libs.hasco.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.math3.geometry.euclidean.oned.Interval;
import org.apache.commons.math3.geometry.partitioning.Region.Location;
import org.api4.java.ai.graphsearch.problem.IPathSearchInput;
import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.hasco.core.reduction.planning2search.IHASCOPlanningReduction;
import ai.libs.hasco.core.reduction.softcomp2planning.HASCOReduction;
import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.components.api.IComponent;
import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.api.INumericParameterRefinementConfigurationMap;
import ai.libs.jaicore.components.api.IParameter;
import ai.libs.jaicore.components.api.IParameterDependency;
import ai.libs.jaicore.components.api.IParameterDomain;
import ai.libs.jaicore.components.api.IRequiredInterfaceDefinition;
import ai.libs.jaicore.components.model.CategoricalParameterDomain;
import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.jaicore.components.model.NumericParameterDomain;
import ai.libs.jaicore.components.model.RefinementConfiguredSoftwareConfigurationProblem;
import ai.libs.jaicore.components.model.SoftwareConfigurationProblem;
import ai.libs.jaicore.logic.fol.structure.Literal;
import ai.libs.jaicore.logic.fol.structure.LiteralParam;
import ai.libs.jaicore.logic.fol.structure.Monom;
import ai.libs.jaicore.planning.classical.algorithms.strips.forward.StripsUtil;
import ai.libs.jaicore.planning.core.Action;
import ai.libs.jaicore.planning.core.interfaces.IPlan;
import ai.libs.jaicore.planning.hierarchical.problems.ceocipstn.CEOCIPSTNPlanningProblem;
import ai.libs.jaicore.planning.hierarchical.problems.htn.CostSensitiveHTNPlanningProblem;
import ai.libs.jaicore.planning.hierarchical.problems.htn.CostSensitivePlanningToStandardSearchProblemReduction;
import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.model.travesaltree.BackPointerPath;

/**
 * Utility functions in the context of HASCO algorithm selection and configurtion.
 *
 * @author Felix Mohr
 *
 */
public class HASCOUtil {

	private static final String LITERAL_RESOLVES = "resolves";
	private static final String LITERAL_PARAMCONTAINER = "parameterContainer";
	private static final String LITERAL_VAL = "val";
	private static final String LITERAL_INTERFACEGROUPDEFINITION = "interfaceGroup"; // [component name, interface ID (role), component handle, iGroupHandle]
	private static final String LITERAL_INTERFACEMEMBERSHIP = "interfaceMember"; // [iHandle, iGroupHandle]

	private static final Logger logger = LoggerFactory.getLogger(HASCOUtil.class);

	private HASCOUtil() {
		/* avoid instantiation */
	}

	public static <N, A> IPathSearchInput<N, A> getSearchProblem(final Collection<? extends IComponent> components, final String requiredInterface, final INumericParameterRefinementConfigurationMap paramRefinementConfig,
			final IHASCOPlanningReduction<N, A> plan2searchReduction) {
		HASCOReduction<Double> hascoReduction = new HASCOReduction<>();
		SoftwareConfigurationProblem<Double> coreProblem = new SoftwareConfigurationProblem<>(components, requiredInterface, n -> 0.0);
		RefinementConfiguredSoftwareConfigurationProblem<Double> problem = new RefinementConfiguredSoftwareConfigurationProblem<>(coreProblem, paramRefinementConfig);
		CostSensitiveHTNPlanningProblem<CEOCIPSTNPlanningProblem, Double> planningProblem = hascoReduction.encodeProblem(problem);
		return new CostSensitivePlanningToStandardSearchProblemReduction<CEOCIPSTNPlanningProblem, N, A, Double>(plan2searchReduction).encodeProblem(planningProblem);
	}

	public static <V extends Comparable<V>> CostSensitiveHTNPlanningProblem<CEOCIPSTNPlanningProblem, V> getPlannigProblem(final RefinementConfiguredSoftwareConfigurationProblem<V> problem) {
		HASCOReduction<V> hascoReduction = new HASCOReduction<>();
		return hascoReduction.encodeProblem(problem);
	}

	public static <N, A, V extends Comparable<V>> IPathSearchWithPathEvaluationsInput<N, A, V> getSearchProblemWithEvaluation(final RefinementConfiguredSoftwareConfigurationProblem<V> problem,
			final IHASCOPlanningReduction<N, A> plan2searchReduction) {
		return getSearchProblemWithEvaluation(getPlannigProblem(problem), plan2searchReduction);
	}

	public static <N, A, V extends Comparable<V>> IPathSearchWithPathEvaluationsInput<N, A, V> getSearchProblemWithEvaluation(final CostSensitiveHTNPlanningProblem<CEOCIPSTNPlanningProblem, V> planningProblem,
			final IHASCOPlanningReduction<N, A> plan2searchReduction) {
		return new CostSensitivePlanningToStandardSearchProblemReduction<CEOCIPSTNPlanningProblem, N, A, V>(plan2searchReduction).encodeProblem(planningProblem);
	}

	/**
	 *
	 * @param state
	 * @param objectName
	 * @return
	 */
	public static Map<String, String> getParameterContainerMap(final Monom state, final String objectName) {
		Map<String, String> parameterContainerMap = new HashMap<>();
		List<Literal> containerLiterals = state.stream().filter(l -> l.getPropertyName().equals(LITERAL_PARAMCONTAINER) && l.getParameters().get(2).getName().equals(objectName)).collect(Collectors.toList());
		containerLiterals.forEach(l -> parameterContainerMap.put(l.getParameters().get(1).getName(), l.getParameters().get(3).getName()));
		return parameterContainerMap;
	}

	public static Map<ComponentInstance, Map<IParameter, String>> getParametrizations(final Monom state, final Collection<? extends IComponent> components, final boolean resolveIntervals) {
		Map<String, ComponentInstance> objectMap = new HashMap<>();
		Map<String, Map<String, String>> parameterContainerMap = new HashMap<>(); // stores for each object the name of the container of each parameter
		Map<String, String> parameterValues = new HashMap<>();

		Map<ComponentInstance, Map<IParameter, String>> parameterValuesPerComponentInstance = new HashMap<>();

		Collection<String> overwrittenDataContainers = getOverwrittenDatacontainersInState(state);

		/*
		 * create (empty) component instances, detect containers for parameter values, and register the
		 * values of the data containers
		 */
		for (Literal l : state) {
			String[] params = l.getParameters().stream().map(LiteralParam::getName).collect(Collectors.toList()).toArray(new String[] {});
			switch (l.getPropertyName()) {
			case LITERAL_RESOLVES: // field 0 and 1 (parent object name and interface name) are ignored here
				String componentName = params[2];
				String objectName = params[3];
				Optional<? extends IComponent> component = components.stream().filter(c -> c.getName().equals(componentName)).findAny();
				assert component.isPresent() : "Could not find component with name " + componentName;
				ComponentInstance object = new ComponentInstance(component.get(), new HashMap<>(), new HashMap<>());
				objectMap.put(objectName, object);
				break;
			case LITERAL_PARAMCONTAINER:
				if (!parameterContainerMap.containsKey(params[2])) {
					parameterContainerMap.put(params[2], new HashMap<>());
				}
				parameterContainerMap.get(params[2]).put(params[1], params[3]);
				break;
			case LITERAL_VAL:
				if (overwrittenDataContainers.contains(params[0])) {
					parameterValues.put(params[0], params[1]);
				}
				break;

			default:

				/* simply ignore other literals */
				break;
			}
		}

		/* update the configurations of the objects */
		for (Entry<String, ComponentInstance> entry : objectMap.entrySet()) {
			Map<IParameter, String> paramValuesForThisComponent = new HashMap<>();
			String objectName = entry.getKey();
			ComponentInstance object = entry.getValue();
			parameterValuesPerComponentInstance.put(object, paramValuesForThisComponent);
			for (IParameter p : object.getComponent().getParameters()) {

				assert parameterContainerMap.containsKey(objectName) : "No parameter container map has been defined for object " + objectName + " of component " + object.getComponent().getName() + "!";
				assert parameterContainerMap.get(objectName).containsKey(p.getName()) : "The data container for parameter " + p.getName() + " of " + object.getComponent().getName() + " is not defined!";

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

	public static Map<String, ComponentInstance> getGroundComponentsFromState(final Monom state, final Collection<? extends IComponent> components, final boolean resolveIntervals) {
		Map<String, ComponentInstance> objectMap = new HashMap<>();
		Map<String, Map<String, String>> parameterContainerMap = new HashMap<>(); // stores for each object the name of the container of each parameter
		Map<String, String> parameterValues = new HashMap<>();
		Map<String, String> interfaceMembershipMap = new HashMap<>();
		Map<String, Integer> interfaceIndexMap = new HashMap<>();
		Map<String, String> interfaceGroupComponentMap = new HashMap<>(); // stores for each interface group to which component instance it belongs
		Map<String, String> interfaceGroupRoleMap = new HashMap<>(); // stores for each interface group the role it has for the component
		Collection<String> overwrittenDatacontainers = getOverwrittenDatacontainersInState(state);
		Map<String, Map<String, Map<Integer, ComponentInstance>>> orderedRequiredInterfaceMap = new HashMap<>(); // stores for each ci reference and each of its ri references the map that assigns to the slot number the concrete component
		// instance

		/* create (empty) component instances, detect containers for parameter values, and register the values of the data containers */
		for (Literal l : state) {
			String[] params = l.getParameters().stream().map(LiteralParam::getName).collect(Collectors.toList()).toArray(new String[] {});
			switch (l.getPropertyName()) {
			case LITERAL_RESOLVES: // field 0 and 1 (parent object name and interface name) are ignored here
				String componentName = params[2];
				String objectName = params[3];
				Optional<? extends IComponent> component = components.stream().filter(c -> c.getName().equals(componentName)).findAny();
				if (!component.isPresent()) {
					throw new IllegalStateException("Error when treating literal " + l + ". The provided list of components has no component with name \"" + componentName + "\". List of known components: "
							+ components.stream().map(c -> "\n\t" + c.getName()).collect(Collectors.joining()));
				}
				ComponentInstance object = new ComponentInstance(component.get(), new HashMap<>(), new HashMap<>());
				objectMap.put(objectName, object);
				break;
			case LITERAL_PARAMCONTAINER:
				if (!parameterContainerMap.containsKey(params[2])) {
					parameterContainerMap.put(params[2], new HashMap<>());
				}
				parameterContainerMap.get(params[2]).put(params[1], params[3]);
				break;
			case LITERAL_VAL:
				parameterValues.put(params[0], params[1]);
				break;

			case LITERAL_INTERFACEGROUPDEFINITION:
				interfaceGroupComponentMap.put(params[3], params[2]);
				interfaceGroupRoleMap.put(params[3], params[1]);
				break;

			case LITERAL_INTERFACEMEMBERSHIP: // first argument is the interface, second is the interface group it belongs to, third argument is the index of the realization
				interfaceMembershipMap.put(params[0], params[1]);
				interfaceIndexMap.put(params[0], Integer.parseInt(params[2]));
				break;
			default:
				/* simply ignore other cases */
				break;
			}
		}

		/* now establish the binding of the required interfaces of the component instances */
		state.stream().filter(l -> l.getPropertyName().equals(LITERAL_RESOLVES)).forEach(l -> {
			String[] params = l.getParameters().stream().map(LiteralParam::getName).collect(Collectors.toList()).toArray(new String[] {});
			String handleOfRequiredInterface = params[0];
			String handleOfComponentInstanceThatProvidesTheInterface = params[3];
			if (!handleOfRequiredInterface.equals("request")) {
				if (!interfaceMembershipMap.containsKey(handleOfRequiredInterface)) {
					throw new IllegalArgumentException("The state contains a literal " + l + ", which suggests that " + handleOfRequiredInterface
							+ " is the handle of a requried interface. But this handle is not registered in the map. State is: " + state.stream().sorted((s1, s2) -> s1.compareTo(s2)).map(lit -> "\n\t" + lit).collect(Collectors.joining()));
				}
				String iFaceGroupHandle = interfaceMembershipMap.get(handleOfRequiredInterface);
				String handleOfComponentInstanceThatRequiresTheInterface = interfaceGroupComponentMap.get(iFaceGroupHandle);
				ComponentInstance providingCI = objectMap.get(handleOfComponentInstanceThatProvidesTheInterface);
				int indexOfRealization = interfaceIndexMap.get(handleOfRequiredInterface);
				orderedRequiredInterfaceMap.computeIfAbsent(handleOfComponentInstanceThatRequiresTheInterface, rci -> new HashMap<>()).computeIfAbsent(interfaceGroupRoleMap.get(iFaceGroupHandle), gri -> new HashMap<>()).put(indexOfRealization, providingCI);
			}
		});
		for (Entry<String, ComponentInstance> entry : objectMap.entrySet()) {
			ComponentInstance requiringCI = entry.getValue();
			String requiringCIReference = entry.getKey();
			for (IRequiredInterfaceDefinition ri : requiringCI.getComponent().getRequiredInterfaces()) {
				String reqInterfaceId = ri.getId();
				List<IComponentInstance> realizations = new ArrayList<>();
				if (orderedRequiredInterfaceMap.containsKey(requiringCIReference) && orderedRequiredInterfaceMap.get(requiringCIReference).containsKey(reqInterfaceId)) {
					Map<Integer, ComponentInstance> provisionsOfThisInterface = orderedRequiredInterfaceMap.get(requiringCIReference).get(reqInterfaceId);
					int n = provisionsOfThisInterface.size();
					for (int i = 1; i <= n; i++) { // indices here start at 1
						if (!provisionsOfThisInterface.containsKey(i)) {
							throw new IllegalArgumentException(
									"The realizations of the required interface " + requiringCI + " of component " + requiringCI.getComponent().getName() + " is not consecutive! Here is the map: \n" + provisionsOfThisInterface);
						}
						realizations.add(provisionsOfThisInterface.get(i));
					}
				}
				requiringCI.getSatisfactionOfRequiredInterfaces().put(reqInterfaceId, realizations);
			}
		}

		/* set the explicitly defined parameters (e.g. overwritten containers) in the component instances */
		for (Entry<String, ComponentInstance> entry : objectMap.entrySet()) {
			String objectName = entry.getKey();
			ComponentInstance object = entry.getValue();
			for (IParameter p : object.getComponent().getParameters()) {
				if (!parameterContainerMap.containsKey(objectName)) {
					throw new IllegalStateException("No parameter container map has been defined for object " + objectName + " of component " + object.getComponent().getName() + "!");
				}
				if (!parameterContainerMap.get(objectName).containsKey(p.getName())) {
					throw new IllegalStateException(
							"The data container for parameter " + p.getName() + " of " + object.getComponent().getName() + " is not defined! State: " + state.stream().sorted().map(l -> "\n\t" + l).collect(Collectors.joining()));
				}
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

	public static <N, A, V extends Comparable<V>> ComponentInstance getSolutionCompositionForNode(final IHASCOPlanningReduction<N, A> planningGraphDeriver, final Collection<? extends IComponent> components, final Monom initState,
			final BackPointerPath<N, A, ?> path, final boolean resolveIntervals) {
		return getSolutionCompositionForPlan(components, initState, planningGraphDeriver.decodeSolution(new SearchGraphPath<>(path)), resolveIntervals);
	}

	public static <N, A, V extends Comparable<V>> ComponentInstance getComponentInstanceForNode(final IHASCOPlanningReduction<N, A> planningGraphDeriver, final Collection<? extends IComponent> components, final Monom initState,
			final BackPointerPath<N, A, ?> path, final String name, final boolean resolveIntervals) {
		return getComponentInstanceForPlan(components, initState, planningGraphDeriver.decodeSolution(new SearchGraphPath<>(path)), name, resolveIntervals);
	}

	public static Monom getFinalStateOfPlan(final Monom initState, final IPlan plan) {
		Monom state = new Monom(initState);
		for (Action a : plan.getActions()) {
			StripsUtil.updateState(state, a);
		}
		return state;
	}

	public static ComponentInstance getSolutionCompositionForPlan(final Collection<? extends IComponent> components, final Monom initState, final IPlan plan, final boolean resolveIntervals) {
		return getSolutionCompositionFromState(components, getFinalStateOfPlan(initState, plan), resolveIntervals);
	}

	public static ComponentInstance getComponentInstanceForPlan(final Collection<? extends IComponent> components, final Monom initState, final IPlan plan, final String name, final boolean resolveIntervals) {
		return getComponentInstanceFromState(components, getFinalStateOfPlan(initState, plan), name, resolveIntervals);
	}

	public static ComponentInstance getSolutionCompositionFromState(final Collection<? extends IComponent> components, final Monom state, final boolean resolveIntervals) {
		return getComponentInstanceFromState(components, state, "solution", resolveIntervals);
	}

	public static ComponentInstance getComponentInstanceFromState(final Collection<? extends IComponent> components, final Monom state, final String name, final boolean resolveIntervals) {
		return HASCOUtil.getGroundComponentsFromState(state, components, resolveIntervals).get(name);
	}

	public static Map<IParameter, IParameterDomain> getUpdatedDomainsOfComponentParameters(final Monom state, final IComponent component, final String objectIdentifierInState) {
		Map<String, String> parameterContainerMap = new HashMap<>();
		Map<String, String> parameterContainerMapInv = new HashMap<>();
		Map<String, String> parameterValues = new HashMap<>();

		/* detect containers for parameter values, and register the values of the data containers */
		for (Literal l : state) {
			String[] params = l.getParameters().stream().map(LiteralParam::getName).collect(Collectors.toList()).toArray(new String[] {});
			switch (l.getPropertyName()) {
			case LITERAL_PARAMCONTAINER:
				if (!params[2].equals(objectIdentifierInState)) {
					continue;
				}
				parameterContainerMap.put(params[1], params[3]);
				parameterContainerMapInv.put(params[3], params[1]);
				break;
			case LITERAL_VAL:
				parameterValues.put(params[0], params[1]);
				break;
			default: // ignore other literals
				break;
			}
		}

		/* determine current values of the parameters of this component instance */
		Map<IParameter, String> paramValuesForThisComponentInstance = new HashMap<>();
		for (IParameter p : component.getParameters()) {
			if (!parameterContainerMap.containsKey(p.getName())) {
				throw new IllegalStateException("The data container for parameter " + p.getName() + " of " + objectIdentifierInState + " is not defined!");
			}
			String assignedValue = parameterValues.get(parameterContainerMap.get(p.getName()));
			if (assignedValue == null) {
				throw new IllegalStateException("No value has been assigned to parameter " + p.getName() + " stored in container " + parameterContainerMap.get(p.getName()) + " in state " + state);
			}
			String value = getParamValue(p, assignedValue, false);
			assert value != null : "Determined value NULL for parameter " + p.getName() + ", which is not plausible.";
			paramValuesForThisComponentInstance.put(p, value);
		}

		/* extract instance */
		ComponentInstance instance = getComponentInstanceFromState(Arrays.asList(component), state, objectIdentifierInState, false);

		/* now compute the new domains based on the current values */
		return getUpdatedDomainsOfComponentParameters(instance);
	}

	private static String getParamValue(final IParameter p, final String assignedValue, final boolean resolveIntervals) {
		if (assignedValue == null) {
			throw new IllegalArgumentException("Cannot determine true value for assigned param value " + assignedValue + " for parameter " + p.getName());
		}
		String interpretedValue = "";
		if (p.isNumeric()) {
			if (resolveIntervals) {
				NumericParameterDomain np = (NumericParameterDomain) p.getDefaultDomain();
				List<String> vals = SetUtil.unserializeList(assignedValue);
				Interval interval = new Interval(Double.valueOf(vals.get(0)), Double.valueOf(vals.get(1)));
				interpretedValue = String.valueOf(interval.checkPoint((double) p.getDefaultValue(), 0.001) == Location.OUTSIDE ? interval.getBarycenter() : (double) p.getDefaultValue());
				if (np.isInteger()) {
					interpretedValue = String.valueOf((int) Math.round(Double.parseDouble(interpretedValue)));
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

	public static Map<IParameter, IParameterDomain> getUpdatedDomainsOfComponentParameters(final ComponentInstance componentInstance) {
		IComponent component = componentInstance.getComponent();

		/* initialize all params for which a decision has been made already with their respective value */
		Map<IParameter, IParameterDomain> domains = new HashMap<>();
		for (IParameter p : componentInstance.getParametersThatHaveBeenSetExplicitly()) {
			if (p.isNumeric()) {
				NumericParameterDomain defaultDomain = (NumericParameterDomain) p.getDefaultDomain();
				Interval interval = SetUtil.unserializeInterval(componentInstance.getParameterValue(p));
				domains.put(p, new NumericParameterDomain(defaultDomain.isInteger(), interval.getInf(), interval.getSup()));
			} else if (p.isCategorical()) {
				domains.put(p, new CategoricalParameterDomain(new String[] { componentInstance.getParameterValue(p) }));
			}
		}

		/* initialize all others with the default domain */
		for (IParameter p : componentInstance.getParametersThatHaveNotBeenSetExplicitly()) {
			domains.put(p, p.getDefaultDomain());
		}
		assert (domains.keySet().equals(component.getParameters())) : "There are parameters for which no current domain was derived.";

		/* update domains based on the dependencies defined for this component */
		for (IParameterDependency dependency : component.getParameterDependencies()) {
			if (ai.libs.jaicore.components.model.CompositionProblemUtil.isDependencyPremiseSatisfied(dependency, domains)) {
				logger.info("Premise of dependency {} is satisfied, applying its conclusions ...", dependency);
				for (Pair<IParameter, IParameterDomain> newDomain : dependency.getConclusion()) {
					/*
					 * directly use the concluded domain if the current value is NOT subsumed by it. Otherwise, just
					 * stick to the current domain
					 */
					IParameter param = newDomain.getX();
					IParameterDomain concludedDomain = newDomain.getY();
					if (!componentInstance.getParametersThatHaveBeenSetExplicitly().contains(param)) {
						domains.put(param, concludedDomain);
						logger.debug("Changing domain of {} from {} to {}", param, domains.get(param), concludedDomain);
					} else {
						logger.debug("Not changing domain of {} since it has already been set explicitly in the past.", param);
					}
				}
			} else {
				logger.debug("Ignoring unsatisfied dependency {}.", dependency);
			}
		}
		return domains;
	}
}
