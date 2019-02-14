package hasco.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import jaicore.basic.sets.SetUtil;
import jaicore.basic.sets.SetUtil.Pair;
import jaicore.logging.ToJSONStringUtil;

/**
 * For a given component, a composition defines all parameter values and the
 * required interfaces (recursively)
 *
 * @author fmohr
 *
 */
@JsonPropertyOrder(alphabetic = true)
public class ComponentInstance {
	private final Component component;
	private final Map<String, String> parameterValues;
	/**
	 * The satisfactionOfRequiredInterfaces map maps from Interface IDs to
	 * ComopnentInstances
	 */
	private final Map<String, ComponentInstance> satisfactionOfRequiredInterfaces;

	public ComponentInstance(@JsonProperty("component") final Component component, @JsonProperty("parameterValues") final Map<String, String> parameterValues,
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
		if (this.parameterValues == null) {
			return new ArrayList<>();
		}
		return this.getComponent().getParameters().stream().filter(p -> this.parameterValues.containsKey(p.getName())).collect(Collectors.toList());
	}

	public Collection<Parameter> getParametersThatHaveNotBeenSetExplicitly() {
		return SetUtil.difference(this.component.getParameters(), this.getParametersThatHaveBeenSetExplicitly());
	}

	public String getParameterValue(final Parameter param) {
		return this.getParameterValue(param.getName());
	}

	public String getParameterValue(final String param) {
		return this.parameterValues.get(param);
	}

	/**
	 * @return This method returns a mapping of interface IDs to component
	 *         instances.
	 */
	public Map<String, ComponentInstance> getSatisfactionOfRequiredInterfaces() {
		return this.satisfactionOfRequiredInterfaces;
	}
	
	public Collection<Component> getContainedComponents() {
		Collection<Component> components = new HashSet<>();
		components.add(this.getComponent());
		for (ComponentInstance ci : satisfactionOfRequiredInterfaces.values()) {
			components.addAll(ci.getContainedComponents());
		}
		return components;
	}
	
	public boolean matchesPathRestrictions(Collection<List<Pair<String,String>>> paths) {
		for (List<Pair<String,String>> path : paths) {
			if (!matchesPathRestriction(path))
				return false;
		}
		return true;
	}
	
	public boolean matchesPathRestriction(List<Pair<String,String>> path) {
		if (path.isEmpty())
			return true;

		/* if the first entry is on null, we interpret it as a filter on this component itself */
		int i = 0;
		if (path.get(0).getX() == null) {
			String requiredComponent = path.get(0).getY();
			if (!requiredComponent.equals("*") && !this.component.getName().equals(requiredComponent))
				return false;
			i = 1;
		}
		
		/* now go over the rest of the path and check every entry on conformity */
		ComponentInstance current = this;
		int n = path.size();
		for (; i < n; i++) {
			Pair<String,String> selection = path.get(i);
			if (!current.getComponent().getRequiredInterfaces().containsKey(selection.getX()))
				throw new IllegalArgumentException("Invalid path restriction: " + selection.getX() + " is not a required interface of " + current.getComponent().getName());
			ComponentInstance instanceChosenForRequiredInterface = current.getSatisfactionOfRequiredInterfaces().get(selection.getX());
			if (!selection.getY().equals("*") && !instanceChosenForRequiredInterface.getComponent().getName().equals(selection.getY()))
				return false;
			current = instanceChosenForRequiredInterface;
		}
		return true;
	}

	@JsonIgnore
	public String getPrettyPrint() {
		return this.getPrettyPrint(0);
	}

	private String getPrettyPrint(final int offset) {
		StringBuilder sb = new StringBuilder();
		sb.append(this.component.getName());
		sb.append("{");
		boolean atLeastOneParamPrinted = false;
		for (String key : this.parameterValues.keySet()) {
			if (atLeastOneParamPrinted) {
				sb.append(", ");
			}
			atLeastOneParamPrinted = true;
			sb.append(key + " = " + this.parameterValues.get(key));
		}
		sb.append("}");
		sb.append("\n");
		for (String requiredInterface : this.component.getRequiredInterfaces().keySet()) {
			for (int i = 0; i < offset + 1; i++) {
				sb.append("\t");
			}
			sb.append(requiredInterface);
			sb.append(": ");
			if (this.satisfactionOfRequiredInterfaces.containsKey(requiredInterface)) {
				sb.append(this.satisfactionOfRequiredInterfaces.get(requiredInterface).getPrettyPrint(offset + 1));
			} else {
				sb.append("null\n");
			}
		}
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.component == null) ? 0 : this.component.hashCode());
		result = prime * result + ((this.parameterValues == null) ? 0 : this.parameterValues.hashCode());
		result = prime * result + ((this.satisfactionOfRequiredInterfaces == null) ? 0 : this.satisfactionOfRequiredInterfaces.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		ComponentInstance other = (ComponentInstance) obj;
		if (this.component == null) {
			if (other.component != null) {
				return false;
			}
		} else if (!this.component.equals(other.component)) {
			return false;
		}
		if (this.parameterValues == null) {
			if (other.parameterValues != null) {
				return false;
			}
		} else if (!this.parameterValues.equals(other.parameterValues)) {
			return false;
		}
		if (this.satisfactionOfRequiredInterfaces == null) {
			if (other.satisfactionOfRequiredInterfaces != null) {
				return false;
			}
		} else if (!this.satisfactionOfRequiredInterfaces.equals(other.satisfactionOfRequiredInterfaces)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		Map<String, Object> fields = new HashMap<>();
		fields.put("component", this.component);
		fields.put("parameterValues", this.parameterValues);
		fields.put("satisfactionOfRequiredInterfaces", this.satisfactionOfRequiredInterfaces);
		return ToJSONStringUtil.toJSONString(this.getClass().getSimpleName(), fields);
	}
}
