package hasco.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.kvstore.KVStore;
import jaicore.basic.sets.SetUtil;

/**
 * The ComponentUtil class can be used to deal with Components in a convenient way.
 * For instance, for a given component (type) it can be used to return a parameterized ComponentInstance.
 *
 * @author wever
 */
public class ComponentUtil {

	private static final Logger logger = LoggerFactory.getLogger(ComponentUtil.class);

	private ComponentUtil() {
		/* Intentionally left blank to prevent instantiation of this class. */
	}

	/**
	 * This procedure returns a ComponentInstance of the given Component with default parameterization.
	 * Note that required interfaces are not resolved.
	 *
	 * @param component The component for which a random parameterization is to be returned.
	 * @return An instantiation of the component with default parameterization.
	 */
	public static ComponentInstance defaultParameterizationOfComponent(final Component component) {
		Map<String, String> parameterValues = new HashMap<>();
		for (Parameter p : component.getParameters()) {
			parameterValues.put(p.getName(), p.getDefaultValue() + "");
		}
		return componentInstanceWithNoRequiredInterfaces(component, parameterValues);
	}

	/**
	 * This procedure returns a valid random parameterization of a given component. Random decisions are made with the help of the given Random object.
	 * Note that required interfaces are not resolved.
	 *
	 * @param component The component for which a random parameterization is to be returned.
	 * @param rand The Random instance for making the random decisions.
	 * @return An instantiation of the component with valid random parameterization.
	 */
	public static ComponentInstance randomParameterizationOfComponent(final Component component, final Random rand) {
		ComponentInstance ci;
		do {
			Map<String, String> parameterValues = new HashMap<>();
			for (Parameter p : component.getParameters()) {
				if (p.getDefaultDomain() instanceof CategoricalParameterDomain) {
					String[] values = ((CategoricalParameterDomain) p.getDefaultDomain()).getValues();
					parameterValues.put(p.getName(), values[rand.nextInt(values.length)]);
				} else {
					NumericParameterDomain numDomain = (NumericParameterDomain) p.getDefaultDomain();
					if (numDomain.isInteger()) {
						if ((int) (numDomain.getMax() - numDomain.getMin()) > 0) {
							parameterValues.put(p.getName(), (rand.nextInt((int) (numDomain.getMax() - numDomain.getMin())) + numDomain.getMin()) + "");
						} else {
							parameterValues.put(p.getName(), p.getDefaultValue() + "");
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

	private static ComponentInstance componentInstanceWithNoRequiredInterfaces(final Component component, final Map<String, String> parameterValues) {
		return new ComponentInstance(component, parameterValues, new HashMap<>());
	}

	/**
	 * Searches and returns all components within a collection of components that provide a specific interface.
	 *
	 * @param components The collection of components to search in.
	 * @param providedInterface The interface of interest.
	 * @return A sub-collection of components all of which provide the requested providedInterface.
	 */
	public static Collection<Component> getComponentsProvidingInterface(final Collection<Component> components, final String providedInterface) {
		return components.stream().filter(x -> x.getProvidedInterfaces().contains(providedInterface)).collect(Collectors.toList());
	}

	/**
	 * Enumerates all possible component instances for a specific root component and a collection of components for resolving required interfaces.
	 * Hyperparameters are set to the default value.
	 *
	 * @param rootComponent The component to be considered the root.
	 * @param components The collection fo components that is used for resolving required interfaces recursively.
	 * @return A collection of component instances of the given root component with all possible algorithm choices.
	 */
	public static Collection<ComponentInstance> getAllAlgorithmSelectionInstances(final Component rootComponent, final Collection<Component> components) {
		Collection<ComponentInstance> instanceList = new LinkedList<>();
		instanceList.add(ComponentUtil.defaultParameterizationOfComponent(rootComponent));

		for (Entry<String, String> requiredInterface : rootComponent.getRequiredInterfaces().entrySet()) {
			List<ComponentInstance> tempList = new LinkedList<>();

			Collection<Component> possiblePlugins = ComponentUtil.getComponentsProvidingInterface(components, requiredInterface.getValue());
			for (ComponentInstance ci : instanceList) {
				for (Component possiblePlugin : possiblePlugins) {
					for (ComponentInstance reqICI : getAllAlgorithmSelectionInstances(possiblePlugin, components)) {
						ComponentInstance copyOfCI = new ComponentInstance(ci.getComponent(), new HashMap<>(ci.getParameterValues()), new HashMap<>(ci.getSatisfactionOfRequiredInterfaces()));
						copyOfCI.getSatisfactionOfRequiredInterfaces().put(requiredInterface.getKey(), reqICI);
						tempList.add(copyOfCI);
					}
				}
			}

			instanceList.clear();
			instanceList.addAll(tempList);
		}

		return instanceList;
	}

	public static int getNumberOfUnparametrizedCompositions(final Collection<Component> components, final String requiredInterface) {
		Collection<Component> candidates = components.stream().filter(c -> c.getProvidedInterfaces().contains(requiredInterface)).collect(Collectors.toList());
		int numCandidates = 0;
		for (Component candidate : candidates) {
			int waysToResolveComponent = 0;
			if (candidate.getRequiredInterfaces().isEmpty()) {
				waysToResolveComponent = 1;
			} else {
				for (String req : candidate.getRequiredInterfaces().keySet()) {
					int subSolutionsForThisInterface = getNumberOfUnparametrizedCompositions(components, candidate.getRequiredInterfaces().get(req));
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

	public static boolean isDefaultConfiguration(final ComponentInstance instance) {
		for (Parameter p : instance.getParametersThatHaveBeenSetExplicitly()) {
			if (p.isNumeric()) {
				List<String> intervalAsList = SetUtil.unserializeList(instance.getParameterValue(p));
				double defaultValue = Double.parseDouble(p.getDefaultValue().toString());
				boolean isCompatibleWithDefaultValue = defaultValue >= Double.parseDouble(intervalAsList.get(0)) && defaultValue <= Double.parseDouble(intervalAsList.get(1));
				if (!isCompatibleWithDefaultValue) {
					logger.info("{} has value {}, which does not subsume the default value {}", p.getName(), instance.getParameterValue(p), defaultValue);
					return false;
				} else {
					logger.info("{} has value {}, which IS COMPATIBLE with the default value {}", p.getName(), instance.getParameterValue(p), defaultValue);
				}
			} else {
				if (!instance.getParameterValue(p).equals(p.getDefaultValue().toString())) {
					logger.info("{} has value {}, which is not the default {}", p.getName(), instance.getParameterValue(p), p.getDefaultValue());
					return false;
				}
			}
		}
		for (ComponentInstance child : instance.getSatisfactionOfRequiredInterfaces().values()) {
			if (!isDefaultConfiguration(child)) {
				return false;
			}
		}
		return true;
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

			for (Parameter p : c.getParameters()) {
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
}
