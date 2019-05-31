package hasco.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import hasco.core.Util;

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
			if (Util.isDependencyPremiseSatisfied(dependency, refinedDomainMap) && !Util.isDependencyConditionSatisfied(dependency.getConclusion(), refinedDomainMap)) {
				return false;
			}
		}
		return true;
	}

}
