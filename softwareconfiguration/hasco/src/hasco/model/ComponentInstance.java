package hasco.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import jaicore.basic.sets.SetUtil;

/**
 * For a given component, a composition defines all parameter values and the
 * required interfaces (recursively)
 *
 * @author fmohr
 *
 */
@JsonPropertyOrder(alphabetic=true)
public class ComponentInstance {
	private final Component component;
	private final Map<String, String> parameterValues;
	/**
	 * The satisfactionOfRequiredInterfaces map maps from Interface IDs to
	 * ComopnentInstances
	 */
	private final Map<String, ComponentInstance> satisfactionOfRequiredInterfaces;

	public ComponentInstance(@JsonProperty("component") final Component component,
			@JsonProperty("parameterValues") final Map<String, String> parameterValues,
			@JsonProperty("satisfactionOfRequiredInterfaces") final Map<String, ComponentInstance> satisfactionOfRequiredInterfaces) {
		super();
		this.component = component;
		this.parameterValues = parameterValues;
		this.satisfactionOfRequiredInterfaces = satisfactionOfRequiredInterfaces;
	}

	public Component getComponent() {
		return this.component;
	}

	public Map<String, String> getParameterValues() {
		return this.parameterValues;
	}

	public Collection<Parameter> getParametersThatHaveBeenSetExplicitly() {
		if (parameterValues == null)
			return new ArrayList<>();
		return getComponent().getParameters().stream().filter(p -> parameterValues.containsKey(p.getName()))
				.collect(Collectors.toList());
	}

	public Collection<Parameter> getParametersThatHaveNotBeenSetExplicitly() {
		return SetUtil.difference(component.getParameters(), getParametersThatHaveBeenSetExplicitly());
	}

	public String getParameterValue(Parameter param) {
		return getParameterValue(param.getName());
	}

	public String getParameterValue(String param) {
		return parameterValues.get(param);
	}

	/**
	 * @return This method returns a mapping of interface IDs to component
	 *         instances.
	 */
	public Map<String, ComponentInstance> getSatisfactionOfRequiredInterfaces() {
		return this.satisfactionOfRequiredInterfaces;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(this.component);
		sb.append(this.parameterValues);

		return sb.toString();
	}

	@JsonIgnore
	public String getPrettyPrint() {
		return getPrettyPrint(0);
	}

	private String getPrettyPrint(int offset) {
		StringBuilder sb = new StringBuilder();
		sb.append(component.getName());
		sb.append("{");
		boolean atLeastOneParamPrinted = false;
		for (String key : parameterValues.keySet()) {
			if (atLeastOneParamPrinted)
				sb.append(", ");
			atLeastOneParamPrinted = true;
			sb.append(key + " = " + parameterValues.get(key));
		}
		sb.append("}");
		sb.append("\n");
		for (String requiredInterface : component.getRequiredInterfaces().keySet()) {
			for (int i = 0; i < offset + 1; i++)
				sb.append("\t");
			sb.append(requiredInterface);
			sb.append(": ");
			if (satisfactionOfRequiredInterfaces.containsKey(requiredInterface))
				sb.append(satisfactionOfRequiredInterfaces.get(requiredInterface).getPrettyPrint(offset + 1));
			else
				sb.append("null\n");
		}
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((component == null) ? 0 : component.hashCode());
		result = prime * result + ((parameterValues == null) ? 0 : parameterValues.hashCode());
		result = prime * result
				+ ((satisfactionOfRequiredInterfaces == null) ? 0 : satisfactionOfRequiredInterfaces.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ComponentInstance other = (ComponentInstance) obj;
		if (component == null) {
			if (other.component != null)
				return false;
		} else if (!component.equals(other.component))
			return false;
		if (parameterValues == null) {
			if (other.parameterValues != null)
				return false;
		} else if (!parameterValues.equals(other.parameterValues))
			return false;
		if (satisfactionOfRequiredInterfaces == null) {
			if (other.satisfactionOfRequiredInterfaces != null)
				return false;
		} else if (!satisfactionOfRequiredInterfaces.equals(other.satisfactionOfRequiredInterfaces))
			return false;
		return true;
	}
}
