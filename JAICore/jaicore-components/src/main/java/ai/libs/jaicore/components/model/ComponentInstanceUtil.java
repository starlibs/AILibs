package ai.libs.jaicore.components.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.components.api.IComponent;
import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.api.IParameter;
import ai.libs.jaicore.components.api.IParameterDependency;
import ai.libs.jaicore.components.api.IParameterDomain;
import ai.libs.jaicore.components.api.IRequiredInterfaceDefinition;
import ai.libs.jaicore.components.serialization.ComponentSerialization;

/**
 * The ComponentInstanceUtil provides some utilities to deal with component instances.
 * For instance, it may be used to check whether a ComponentInstance conforms the dependencies
 * defined in the respective Component.
 *
 * @author wever
 */
public class ComponentInstanceUtil {

	private static final Logger logger = LoggerFactory.getLogger(ComponentInstanceUtil.class);

	private ComponentInstanceUtil() {
		/* Private constructor to prevent anyone to instantiate this Util class by accident. */
	}

	/**
	 * Checks whether a component instance adheres to the defined inter-parameter dependencies defined in the component.
	 * @param ci The component instance to be verified.
	 * @return Returns true iff all dependency conditions hold.
	 */
	public static boolean isValidComponentInstantiation(final ComponentInstance ci) {
		try {
			checkComponentInstantiation(ci);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Checks whether a component instance adheres to the defined inter-parameter dependencies defined in the component.
	 * @param ci The component instance to be verified.
	 * @throws Exception with explanation if it is not valid
	 */
	public static void checkComponentInstantiation(final ComponentInstance ci) {
		Map<IParameter, IParameterDomain> refinedDomainMap = new HashMap<>();

		for (IParameter param : ci.getComponent().getParameters()) {
			if (param.getDefaultDomain() instanceof NumericParameterDomain) {
				double parameterValue = Double.parseDouble(ci.getParameterValue(param));
				refinedDomainMap.put(param, new NumericParameterDomain(((NumericParameterDomain) param.getDefaultDomain()).isInteger(), parameterValue, parameterValue));
			} else if (param.getDefaultDomain() instanceof CategoricalParameterDomain) {
				refinedDomainMap.put(param, new CategoricalParameterDomain(Arrays.asList(ci.getParameterValue(param))));
			}
		}

		for (IParameterDependency dependency : ci.getComponent().getParameterDependencies()) {
			if (CompositionProblemUtil.isDependencyPremiseSatisfied(dependency, refinedDomainMap) && !CompositionProblemUtil.isDependencyConditionSatisfied(dependency.getConclusion(), refinedDomainMap)) {
				throw new IllegalStateException("The dependency " + dependency + " of component " + ci.getComponent().getName() + " in the following component instance is violated: " + new ComponentSerialization().serialize(ci));
			}
		}
	}

	public static String toComponentNameString(final IComponentInstance ci) {
		StringBuilder sb = new StringBuilder();
		sb.append(ci.getComponent().getName());
		if (!ci.getSatisfactionOfRequiredInterfaces().isEmpty()) {
			sb.append("(").append(ci.getSatisfactionOfRequiredInterfaces().values().stream().map(ciList -> ciList.stream().map(cil -> ((ComponentInstance)cil).toComponentNameString()).collect(Collectors.joining())).collect(Collectors.joining(", "))).append(")");
		}
		return sb.toString();
	}

	public static ComponentInstance getDefaultParametrization(final IComponentInstance ci) {
		Map<String, List<IComponentInstance>> defaultRequiredInterfaces = new HashMap<>();
		ci.getSatisfactionOfRequiredInterfaces().forEach((name, ciReqList) -> {
			List<IComponentInstance> l = ciReqList.stream().map(ComponentInstanceUtil::getDefaultParametrization).collect(Collectors.toList());
			defaultRequiredInterfaces.put(name, l);
		});
		return new ComponentInstance(ci.getComponent(), new HashMap<>(), defaultRequiredInterfaces);
	}

	public static boolean isDefaultConfiguration(final IComponentInstance instance) {
		for (IParameter p : instance.getParametersThatHaveBeenSetExplicitly()) {
			if (p.isNumeric()) {
				double defaultValue = Double.parseDouble(p.getDefaultValue().toString());
				String parameterValue = instance.getParameterValue(p);

				boolean isCompatibleWithDefaultValue = false;
				if (parameterValue.contains("[")) {
					List<String> intervalAsList = SetUtil.unserializeList(instance.getParameterValue(p));
					isCompatibleWithDefaultValue = defaultValue >= Double.parseDouble(intervalAsList.get(0)) && defaultValue <= Double.parseDouble(intervalAsList.get(1));
				} else {
					isCompatibleWithDefaultValue = Math.abs(defaultValue - Double.parseDouble(parameterValue)) < 1E-8;
				}
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
		for (Collection<IComponentInstance> childList : instance.getSatisfactionOfRequiredInterfaces().values()) {
			for (IComponentInstance child : childList) {
				if (!isDefaultConfiguration(child)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Samples a random component instance with random parameters.
	 *
	 * @param requiredInterface The required interface the sampled component instance must conform.
	 * @param components The components that can be chosen.
	 * @param rand Random number generator for pseudo randomization.
	 * @return A randomly sampled component instance with random parameters.
	 */
	public static ComponentInstance sampleRandomComponentInstance(final String requiredInterface, final Collection<IComponent> components, final Random rand) {
		List<IComponent> componentsList = new ArrayList<>(ComponentUtil.getComponentsProvidingInterface(components, requiredInterface));
		ComponentInstance ci = ComponentUtil.getRandomParameterizationOfComponent(componentsList.get(rand.nextInt(componentsList.size())), rand);
		for (IRequiredInterfaceDefinition i : ci.getComponent().getRequiredInterfaces()) {
			ci.getSatisfactionOfRequiredInterfaces().put(i.getId(), Arrays.asList(sampleRandomComponentInstance(i.getName(), components, rand)));
		}
		return ci;
	}

	/**
	 * Samples a random component instance with default parameters.
	 *
	 * @param requiredInterface The required interface the sampled component instance must conform.
	 * @param components The components that can be chosen.
	 * @param rand Random number generator for pseudo randomization.
	 * @return A randomly sampled component instance with default parameters.
	 */
	public static ComponentInstance sampleDefaultComponentInstance(final String requiredInterface, final Collection<? extends IComponent> components, final Random rand) {
		List<IComponent> componentsList = new ArrayList<>(ComponentUtil.getComponentsProvidingInterface(components, requiredInterface));
		ComponentInstance ci = ComponentUtil.getDefaultParameterizationOfComponent(componentsList.get(rand.nextInt(componentsList.size())));
		for (IRequiredInterfaceDefinition i : ci.getComponent().getRequiredInterfaces()) {
			ci.getSatisfactionOfRequiredInterfaces().put(i.getId(), Arrays.asList(sampleDefaultComponentInstance(i.getName(), components, rand)));
		}
		return ci;
	}



	/**
	 * This method checks, whether a given list of paths of refinements conforms the constraints for parameter refinements.
	 *
	 * @param paths
	 *            A list of paths of refinements to be checked.
	 * @return Returns true if everything is alright and false if there is an issue with the given paths.
	 */
	public static boolean matchesPathRestrictions(final ComponentInstance ci, final Collection<List<Pair<String, String>>> paths) {
		for (List<Pair<String, String>> path : paths) {
			if (!matchesPathRestriction(ci, path)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * This method checks, whether a path of refinements conforms the constraints for parameter refinements.
	 *
	 * @param path
	 *            A path of refinements to be checked.
	 * @return Returns true if everything is alright and false if there is an issue with the given path.
	 */
	public static boolean matchesPathRestriction(final ComponentInstance ci, final List<Pair<String, String>> path) {
		if (path.isEmpty()) {
			return true;
		}

		/* if the first entry is on null, we interpret it as a filter on this component itself */
		int i = 0;
		if (path.get(0).getX() == null) {
			String requiredComponent = path.get(0).getY();
			if (!requiredComponent.equals("*") && !ci.getComponent().getName().equals(requiredComponent)) {
				return false;
			}
			i = 1;
		}

		/* now go over the rest of the path and check every entry on conformity */
		IComponentInstance current = ci;
		int n = path.size();
		for (; i < n; i++) {
			Pair<String, String> selection = path.get(i);
			if (current.getComponent().getRequiredInterfaces().stream().noneMatch(ri -> ri.getId().equals(selection.getX()))) {
				throw new IllegalArgumentException("Invalid path restriction " + path + ": " + selection.getX() + " is not a required interface of " + current.getComponent().getName());
			}
			Collection<IComponentInstance> instancesChosenForRequiredInterface = current.getSatisfactionOfRequiredInterfaces().get(selection.getX());
			for (IComponentInstance instanceChosenForRequiredInterface : instancesChosenForRequiredInterface) {
				if (!selection.getY().equals("*") && !instanceChosenForRequiredInterface.getComponent().getName().equals(selection.getY())) {
					return false;
				}
				current = instanceChosenForRequiredInterface;
			}
		}
		return true;
	}

	/**
	 * @return A collection of all components contained (recursively) in this <code>ComponentInstance</code>.
	 */
	public static Collection<IComponent> getContainedComponents(final IComponentInstance ci) {
		Collection<IComponent> components = new HashSet<>();
		components.add(ci.getComponent());
		for (Collection<IComponentInstance> ciList : ci.getSatisfactionOfRequiredInterfaces().values()) {
			for (IComponentInstance ciSub : ciList) {
				components.addAll(getContainedComponents(ciSub));
			}
		}
		return components;
	}

	public static String getComponentInstanceAsComponentNames(final IComponentInstance instance) {
		StringBuilder sb = new StringBuilder();
		sb.append(instance.getComponent().getName());
		if (!instance.getSatisfactionOfRequiredInterfaces().isEmpty()) {
			sb.append("{").append(instance.getSatisfactionOfRequiredInterfaces().values().stream().map(ciList -> ciList.stream().map(ComponentInstanceUtil::getComponentInstanceAsComponentNames).collect(Collectors.joining())).collect(Collectors.joining(","))).append("}");
		}
		return sb.toString();
	}

	public static boolean isSubInstance(final IComponentInstance sub, final IComponentInstance sup) {

		/* check component name */
		if (!sub.getComponent().getName().equals(sup.getComponent().getName())) {
			return false;
		}

		/* check parameters */
		Map<String, String> parametersOfSub = sub.getParameterValues();
		Map<String, String> parametersOfSup = sup.getParameterValues();
		for (Entry<String, String> p : parametersOfSub.entrySet()) {
			if (!parametersOfSup.containsKey(p.getKey()) || !parametersOfSup.get(p.getKey()).equals(p.getValue())) {
				return false;
			}
		}

		/* check required interfaces */
		for (Entry<String, List<IComponentInstance>> provisionsOfSub : sub.getSatisfactionOfRequiredInterfaces().entrySet()) {
			int n = provisionsOfSub.getValue().size();
			List<IComponentInstance> provisionsOfSup = sup.getSatisfactionOfRequiredInterface(provisionsOfSub.getKey());
			if (provisionsOfSup.size() < n) {
				return false;
			}
			for (int i = 0; i < n; i++) {
				if (!isSubInstance(provisionsOfSub.getValue().get(i), provisionsOfSup.get(i))) {
					return false;
				}
			}
		}

		/* if no incompatibility was found, return true */
		return true;
	}
}
