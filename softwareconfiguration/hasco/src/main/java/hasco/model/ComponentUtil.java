package hasco.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ComponentUtil {

	public static ComponentInstance defaultParameterizationOfComponent(final Component component) {
		Map<String, String> parameterValues = new HashMap<>();
		for (Parameter p : component.getParameters()) {
			parameterValues.put(p.getName(), p.getDefaultValue() + "");
		}
		return componentInstanceWithNoRequiredInterfaces(component, parameterValues);
	}

	/**
	 * @param component
	 * @param rand
	 * @return
	 */
	public static ComponentInstance randomParameterizationOfComponent(final Component component, final Random rand) {
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

		for (Dependency dep : component.getDependencies()) {

		}
		return componentInstanceWithNoRequiredInterfaces(component, parameterValues);
	}

	private static ComponentInstance componentInstanceWithNoRequiredInterfaces(final Component component, final Map<String, String> parameterValues) {
		return new ComponentInstance(component, parameterValues, new HashMap<>());
	}

}
