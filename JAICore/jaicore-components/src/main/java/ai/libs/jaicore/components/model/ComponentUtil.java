package ai.libs.jaicore.components.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import ai.libs.jaicore.basic.kvstore.KVStore;
import ai.libs.jaicore.components.api.IComponent;
import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.api.IParameter;
import ai.libs.jaicore.components.api.IRequiredInterfaceDefinition;
import ai.libs.jaicore.components.exceptions.ComponentNotFoundException;

/**
 * The ComponentUtil class can be used to deal with Components in a convenient way. For instance, for a given component (type) it can be used to return a parameterized ComponentInstance.
 *
 * @author wever
 */
public class ComponentUtil {

	private ComponentUtil() {
		/* Intentionally left blank to prevent instantiation of this class. */
	}

	/**
	 * This procedure returns a ComponentInstance of the given Component with default parameterization. Note that required interfaces are not resolved.
	 *
	 * @param component
	 *            The component for which a random parameterization is to be returned.
	 * @return An instantiation of the component with default parameterization.
	 */
	public static ComponentInstance getDefaultParameterizationOfComponent(final IComponent component) {
		Map<String, String> parameterValues = new HashMap<>();
		for (IParameter p : component.getParameters()) {
			parameterValues.put(p.getName(), p.getDefaultValue() + "");
		}
		return componentInstanceWithNoRequiredInterfaces(component, parameterValues);
	}

	/**
	 * This procedure returns a valid random parameterization of a given component. Random decisions are made with the help of the given Random object. Note that required interfaces are not resolved.
	 *
	 * @param component
	 *            The component for which a random parameterization is to be returned.
	 * @param rand
	 *            The Random instance for making the random decisions.
	 * @return An instantiation of the component with valid random parameterization.
	 */
	public static ComponentInstance getRandomParameterizationOfComponent(final IComponent component, final Random rand) {
		ComponentInstance ci;
		do {
			Map<String, String> parameterValues = new HashMap<>();
			for (IParameter p : component.getParameters()) {
				if (p.getDefaultDomain() instanceof CategoricalParameterDomain) {
					String[] values = ((CategoricalParameterDomain) p.getDefaultDomain()).getValues();
					parameterValues.put(p.getName(), values[rand.nextInt(values.length)]);
				} else {
					NumericParameterDomain numDomain = (NumericParameterDomain) p.getDefaultDomain();
					if (numDomain.isInteger()) {
						if ((int) (numDomain.getMax() - numDomain.getMin()) > 0) {
							parameterValues.put(p.getName(), ((int) (rand.nextInt((int) (numDomain.getMax() - numDomain.getMin())) + numDomain.getMin())) + "");
						} else {
							if (p.getDefaultValue() instanceof Double) {
								parameterValues.put(p.getName(), ((int) (double) p.getDefaultValue()) + "");
							} else {
								parameterValues.put(p.getName(), (int) p.getDefaultValue() + "");
							}
						}
					} else {
						parameterValues.put(p.getName(), (rand.nextDouble() * (numDomain.getMax() - numDomain.getMin()) + numDomain.getMin()) + "");
					}
				}
			}
			ci = componentInstanceWithNoRequiredInterfaces(component, parameterValues);
		} while (!ComponentInstanceUtil.isValidComponentInstantiation(ci));
		return ci;
	}

	public static ComponentInstance minParameterizationOfComponent(final IComponent component) {

		Map<String, String> parameterValues = new HashMap<>();
		for (IParameter p : component.getParameters()) {
			if (p.getDefaultDomain() instanceof CategoricalParameterDomain) {
				parameterValues.put(p.getName(), p.getDefaultValue() + "");
			} else {
				NumericParameterDomain numDomain = (NumericParameterDomain) p.getDefaultDomain();
				if (numDomain.isInteger()) {
					if ((int) (numDomain.getMax() - numDomain.getMin()) > 0) {
						parameterValues.put(p.getName(), (int) numDomain.getMin() + "");
					} else {
						parameterValues.put(p.getName(), (int) p.getDefaultValue() + "");
					}
				} else {
					parameterValues.put(p.getName(), numDomain.getMin() + "");
				}
			}
		}
		ComponentInstance ci = componentInstanceWithNoRequiredInterfaces(component, parameterValues);
		ComponentInstanceUtil.checkComponentInstantiation(ci);
		return ci;
	}

	public static ComponentInstance maxParameterizationOfComponent(final IComponent component) {
		Map<String, String> parameterValues = new HashMap<>();
		for (IParameter p : component.getParameters()) {
			if (p.getDefaultDomain() instanceof CategoricalParameterDomain) {
				parameterValues.put(p.getName(), p.getDefaultValue() + "");
			} else {
				NumericParameterDomain numDomain = (NumericParameterDomain) p.getDefaultDomain();
				if (numDomain.isInteger()) {
					if ((int) (numDomain.getMax() - numDomain.getMin()) > 0) {
						parameterValues.put(p.getName(), (int) numDomain.getMax() + "");
					} else {
						parameterValues.put(p.getName(), (int) p.getDefaultValue() + "");
					}
				} else {
					parameterValues.put(p.getName(), numDomain.getMax() + "");
				}
			}
		}
		ComponentInstance ci = componentInstanceWithNoRequiredInterfaces(component, parameterValues);
		ComponentInstanceUtil.checkComponentInstantiation(ci);
		return ci;
	}

	private static ComponentInstance componentInstanceWithNoRequiredInterfaces(final IComponent component, final Map<String, String> parameterValues) {
		return new ComponentInstance(component, parameterValues, new HashMap<>());
	}

	public static List<ComponentInstance> categoricalParameterizationsOfComponent(final IComponent component) {
		Map<String, String> parameterValues = new HashMap<>();
		List<ComponentInstance> parameterizedInstances = new ArrayList<>();
		List<IParameter> categoricalParameters = new ArrayList<>();
		int maxParameterIndex = 0;
		for (IParameter p : component.getParameters()) {
			if (p.getDefaultDomain() instanceof CategoricalParameterDomain) {
				String[] values = ((CategoricalParameterDomain) p.getDefaultDomain()).getValues();
				if (values.length > maxParameterIndex) {
					maxParameterIndex = values.length;
				}
				categoricalParameters.add(p);
			} else {
				parameterValues.put(p.getName(), p.getDefaultValue() + "");
			}
		}
		for (int parameterIndex = 0; parameterIndex < maxParameterIndex; parameterIndex++) {
			Map<String, String> categoricalParameterValues = new HashMap<>();
			for (int i = 0; i < categoricalParameters.size(); i++) {
				String parameterValue = null;
				String[] values = ((CategoricalParameterDomain) categoricalParameters.get(i).getDefaultDomain()).getValues();
				if (parameterIndex < values.length) {
					parameterValue = values[parameterIndex];
				} else {
					parameterValue = categoricalParameters.get(i).getDefaultValue() + "";
				}
				categoricalParameterValues.put(categoricalParameters.get(i).getName(), parameterValue);
			}
			categoricalParameterValues.putAll(parameterValues);
			parameterizedInstances.add(new ComponentInstance(component, categoricalParameterValues, new HashMap<>()));
		}

		return parameterizedInstances;
	}

	/**
	 * Searches and returns all components within a collection of components that provide a specific interface.
	 *
	 * @param components
	 *            The collection of components to search in.
	 * @param providedInterface
	 *            The interface of interest.
	 * @return A sub-collection of components all of which provide the requested providedInterface.
	 */
	public static Collection<IComponent> getComponentsProvidingInterface(final Collection<? extends IComponent> components, final String providedInterface) {
		return components.stream().filter(x -> x.getProvidedInterfaces().contains(providedInterface)).collect(Collectors.toList());
	}

	/**
	 * Enumerates all possible component instances for a specific root component and a collection of components for resolving required interfaces. Hyperparameters are set to the default value.
	 *
	 * @param rootComponent
	 *            The component to be considered the root.
	 * @param components
	 *            The collection fo components that is used for resolving required interfaces recursively.
	 * @return A collection of component instances of the given root component with all possible algorithm choices.
	 */
	public static Collection<ComponentInstance> getAllAlgorithmSelectionInstances(final IComponent rootComponent, final Collection<? extends IComponent> components) {
		Collection<ComponentInstance> instanceList = new LinkedList<>();
		instanceList.add(ComponentUtil.getDefaultParameterizationOfComponent(rootComponent));

		for (IRequiredInterfaceDefinition requiredInterface : rootComponent.getRequiredInterfaces()) {
			List<ComponentInstance> tempList = new LinkedList<>();

			Collection<IComponent> possiblePlugins = ComponentUtil.getComponentsProvidingInterface(components, requiredInterface.getName());
			for (ComponentInstance ci : instanceList) {
				for (IComponent possiblePlugin : possiblePlugins) {
					for (ComponentInstance reqICI : getAllAlgorithmSelectionInstances(possiblePlugin, components)) {
						ComponentInstance copyOfCI = new ComponentInstance(ci.getComponent(), new HashMap<>(ci.getParameterValues()), new HashMap<>(ci.getSatisfactionOfRequiredInterfaces()));
						copyOfCI.getSatisfactionOfRequiredInterfaces().put(requiredInterface.getId(), Arrays.asList(reqICI));
						tempList.add(copyOfCI);
					}
				}
			}

			instanceList.clear();
			instanceList.addAll(tempList);
		}

		return instanceList;
	}

	/**
	 * Enumerates all possible component instances for a specific root component and a collection of components for resolving required interfaces. Hyperparameters are set to the default value.
	 *
	 * @param requiredInterface
	 *            The interface required to be provided by the root components.
	 * @param components
	 *            The collection fo components that is used for resolving required interfaces recursively.
	 * @return A collection of component instances of the given root component with all possible algorithm choices.
	 */
	public static Collection<ComponentInstance> getAllAlgorithmSelectionInstances(final String requiredInterface, final Collection<? extends IComponent> components) {
		Collection<ComponentInstance> instanceList = new ArrayList<>();
		components.stream().filter(x -> x.getProvidedInterfaces().contains(requiredInterface)).map(x -> getAllAlgorithmSelectionInstances(x, components)).forEach(instanceList::addAll);
		return instanceList;
	}

	public static int getNumberOfUnparametrizedCompositions(final Collection<? extends IComponent> components, final String requiredInterface) {
		if (hasCycles(components, requiredInterface)) {
			return -1;
		}

		Collection<IComponent> candidates = components.stream().filter(c -> c.getProvidedInterfaces().contains(requiredInterface)).collect(Collectors.toList());
		int numCandidates = 0;
		for (IComponent candidate : candidates) {
			int waysToResolveComponent = 0;
			if (candidate.getRequiredInterfaces().isEmpty()) {
				waysToResolveComponent = 1;
			} else {
				for (IRequiredInterfaceDefinition reqIFaceDef : candidate.getRequiredInterfaces()) {
					String reqIFace = reqIFaceDef.getName();
					int numberOfSolutionPerSlotForThisInterface = getNumberOfUnparametrizedCompositions(components, reqIFace);
					int subSolutionsForThisInterface = 0;
					if (reqIFaceDef.isOptional() || reqIFaceDef.getMin() == 0) {
						subSolutionsForThisInterface ++;
					}

					/* now consider all numbers i of positive realizations of this required interface */
					for (int i = Math.max(1, reqIFaceDef.getMin()); i <= reqIFaceDef.getMax(); i++) {
						int numberOfPossibleRealizationsForThisFixedNumberOfSlots = 1;
						int numCandidatesForNextSlot = numberOfSolutionPerSlotForThisInterface;
						for (int j = 0; j < i; j++) {
							numberOfPossibleRealizationsForThisFixedNumberOfSlots *= numCandidatesForNextSlot;
							if (reqIFaceDef.isUniqueComponents()) {
								numCandidatesForNextSlot --;
							}
						}
						subSolutionsForThisInterface += numberOfPossibleRealizationsForThisFixedNumberOfSlots;
					}

					if (waysToResolveComponent > 0) {
						waysToResolveComponent *= subSolutionsForThisInterface;
					} else {
						waysToResolveComponent = subSolutionsForThisInterface;
					}
				}
			}
			numCandidates += waysToResolveComponent;
		}
		return numCandidates;
	}

	public static ComponentInstance getRandomParametrization(final IComponentInstance componentInstance, final Random rand) {
		ComponentInstance randomParametrization = getRandomParameterizationOfComponent(componentInstance.getComponent(), rand);
		componentInstance.getSatisfactionOfRequiredInterfaces().entrySet()
		.forEach(x -> randomParametrization.getSatisfactionOfRequiredInterfaces().put(x.getKey(), Arrays.asList(getRandomParametrization(x.getValue().iterator().next(), rand))));
		return randomParametrization;
	}

	public static boolean hasCycles(final Collection<? extends IComponent> components, final String requiredInterface) {
		return hasCycles(components, requiredInterface, new LinkedList<>());
	}

	private static boolean hasCycles(final Collection<? extends IComponent> components, final String requiredInterface, final List<String> componentList) {
		Collection<IComponent> candidates = components.stream().filter(c -> c.getProvidedInterfaces().contains(requiredInterface)).collect(Collectors.toList());

		for (IComponent c : candidates) {
			if (componentList.contains(c.getName())) {
				return true;
			}

			List<String> componentListCopy = new LinkedList<>(componentList);
			componentListCopy.add(c.getName());

			for (IRequiredInterfaceDefinition subRequiredInterface : c.getRequiredInterfaces()) {
				if (hasCycles(components, subRequiredInterface.getName(), componentListCopy)) {
					return true;
				}
			}
		}
		return false;
	}

	public static KVStore getStatsForComponents(final Collection<Component> components) {
		KVStore stats = new KVStore();
		int numComponents = 0;
		int numNumericParams = 0;
		int numIntParams = 0;
		int numDoubleParams = 0;
		int numCatParams = 0;
		int numBoolParams = 0;
		int otherParams = 0;

		for (Component c : components) {
			numComponents++;

			for (IParameter p : c.getParameters()) {
				if (p.getDefaultDomain() instanceof CategoricalParameterDomain) {
					numCatParams++;
					if (p.getDefaultDomain() instanceof BooleanParameterDomain) {
						numBoolParams++;
					}
				} else if (p.getDefaultDomain() instanceof NumericParameterDomain) {
					numNumericParams++;
					if (((NumericParameterDomain) p.getDefaultDomain()).isInteger()) {
						numIntParams++;
					} else {
						numDoubleParams++;
					}
				} else {
					otherParams++;
				}
			}
		}

		stats.put("nComponents", numComponents);
		stats.put("nNumericParameters", numNumericParams);
		stats.put("nIntegerParameters", numIntParams);
		stats.put("nContinuousParameters", numDoubleParams);
		stats.put("nCategoricalParameters", numCatParams);
		stats.put("nBooleanParameters", numBoolParams);
		stats.put("nOtherParameters", otherParams);

		return stats;
	}

	/**
	 * Returns a collection of components that is relevant to resolve all recursive dependency when the request concerns a component with the provided required interface.
	 *
	 * @param components
	 *            A collection of component to search for relevant components.
	 * @param requiredInterface
	 *            The requested required interface.
	 * @return The collection of affected components when requesting the given required interface.
	 */
	public static Collection<IComponent> getAffectedComponents(final Collection<? extends IComponent> components, final String requiredInterface) {
		Collection<IComponent> affectedComponents = new HashSet<>(ComponentUtil.getComponentsProvidingInterface(components, requiredInterface));
		if (affectedComponents.isEmpty()) {
			throw new IllegalArgumentException("Could not resolve the requiredInterface " + requiredInterface);
		}
		Set<IComponent> recursiveResolvedComps = new HashSet<>();
		affectedComponents.forEach(x -> x.getRequiredInterfaces().stream().map(iface -> getAffectedComponents(components, iface.getName())).forEach(recursiveResolvedComps::addAll));
		affectedComponents.addAll(recursiveResolvedComps);
		return affectedComponents;
	}

	public static IComponent getComponentByName(final String componentName, final Collection<? extends IComponent> components) throws ComponentNotFoundException {
		for (IComponent component : components) {
			if (component.getName().equals(componentName)) {
				return component;
			}
		}

		throw new ComponentNotFoundException("No Component with this name loaded: " + componentName);
	}
}
