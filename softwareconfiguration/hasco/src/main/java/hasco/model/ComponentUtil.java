package hasco.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * The ComponentUtil class can be used to deal with Components in a convenient way.
 * For instance, for a given component (type) it can be used to return a parameterized ComponentInstance.
 *
 * @author wever
 */
public class ComponentUtil {

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
						parameterValues.put(p.getName(), (rand.nextInt((int) (numDomain.getMax() - numDomain.getMin())) + numDomain.getMin()) + "");
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

}
