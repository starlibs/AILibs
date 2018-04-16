package hasco.model;

import java.util.Map;

/**
 * For a given component, a composition defines all parameter values and the required interfaces (recursively)
 * 
 * @author fmohr
 *
 */
public class ComponentInstance {
	private final Component component;
	private final Map<String, String> parameterValues;
	private final Map<String, ComponentInstance> satisfactionOfRequiredInterfaces;

	public ComponentInstance(Component component, Map<String, String> parameterValues, Map<String, ComponentInstance> satisfactionOfRequiredInterfaces) {
		super();
		this.component = component;
		this.parameterValues = parameterValues;
		this.satisfactionOfRequiredInterfaces = satisfactionOfRequiredInterfaces;
	}

	public Component getComponent() {
		return component;
	}

	public Map<String, String> getParameterValues() {
		return parameterValues;
	}

	public Map<String, ComponentInstance> getSatisfactionOfRequiredInterfaces() {
		return satisfactionOfRequiredInterfaces;
	}
}
