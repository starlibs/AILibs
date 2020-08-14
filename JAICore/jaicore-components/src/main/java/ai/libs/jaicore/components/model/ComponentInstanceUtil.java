package ai.libs.jaicore.components.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * The ComponentInstanceUtil provides some utilities to deal with component instances.
 * For instance, it may be used to check whether a ComponentInstance conforms the dependencies
 * defined in the respective Component.
 *
 * @author wever
 */
public class ComponentInstanceUtil {

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
		Map<Parameter, IParameterDomain> refinedDomainMap = new HashMap<>();

		for (Parameter param : ci.getComponent().getParameters()) {
			if (param.getDefaultDomain() instanceof NumericParameterDomain) {
				double parameterValue = Double.parseDouble(ci.getParameterValue(param));
				refinedDomainMap.put(param, new NumericParameterDomain(((NumericParameterDomain) param.getDefaultDomain()).isInteger(), parameterValue, parameterValue));
			} else if (param.getDefaultDomain() instanceof CategoricalParameterDomain) {
				refinedDomainMap.put(param, new CategoricalParameterDomain(Arrays.asList(ci.getParameterValue(param))));
			}
		}

		for (Dependency dependency : ci.getComponent().getDependencies()) {
			if (CompositionProblemUtil.isDependencyPremiseSatisfied(dependency, refinedDomainMap) && !CompositionProblemUtil.isDependencyConditionSatisfied(dependency.getConclusion(), refinedDomainMap)) {
				throw new IllegalStateException("Problem with dependency " + dependency);
			}
		}
	}

	public static String toComponentNameString(final ComponentInstance ci) {
		StringBuilder sb = new StringBuilder();
		sb.append(ci.getComponent().getName());
		if (!ci.getSatisfactionOfRequiredInterfaces().isEmpty()) {
			sb.append("(").append(ci.getSatisfactionOfRequiredInterfaces().values().stream().map(ComponentInstanceUtil::toComponentNameString).collect(Collectors.joining(", "))).append(")");
		}
		return sb.toString();
	}

	public static ComponentInstance getDefaultParametrization(final ComponentInstance ci) {
		Map<String, ComponentInstance> defaultRequiredInterfaces = new HashMap<>();
		ci.getSatisfactionOfRequiredInterfaces().forEach((name, ciReq) -> defaultRequiredInterfaces.put(name, getDefaultParametrization(ciReq)));
		return new ComponentInstance(ci.getComponent(), new HashMap<>(), defaultRequiredInterfaces);
	}

	/**
	 * Samples a random component instance with random parameters.
	 *
	 * @param requiredInterface The required interface the sampled component instance must conform.
	 * @param components The components that can be chosen.
	 * @param rand Random number generator for pseudo randomization.
	 * @return A randomly sampled component instance with random parameters.
	 */
	public static ComponentInstance sampleRandomComponentInstance(final String requiredInterface, final Collection<Component> components, final Random rand) {
		List<Component> componentsList = new ArrayList<>(ComponentUtil.getComponentsProvidingInterface(components, requiredInterface));
		ComponentInstance ci = ComponentUtil.getRandomParameterizationOfComponent(componentsList.get(rand.nextInt(componentsList.size())), rand);
		for (Interface i : ci.getComponent().getRequiredInterfaces()) {
			ci.getSatisfactionOfRequiredInterfaces().put(i.getId(), sampleRandomComponentInstance(i.getName(), components, rand));
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
	public static ComponentInstance sampleDefaultComponentInstance(final String requiredInterface, final Collection<Component> components, final Random rand) {
		List<Component> componentsList = new ArrayList<>(ComponentUtil.getComponentsProvidingInterface(components, requiredInterface));
		ComponentInstance ci = ComponentUtil.getDefaultParameterizationOfComponent(componentsList.get(rand.nextInt(componentsList.size())));
		for (Interface i : ci.getComponent().getRequiredInterfaces()) {
			ci.getSatisfactionOfRequiredInterfaces().put(i.getId(), sampleDefaultComponentInstance(i.getName(), components, rand));
		}
		return ci;
	}
}
