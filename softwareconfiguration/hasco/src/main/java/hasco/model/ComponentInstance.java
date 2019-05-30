package hasco.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import jaicore.basic.sets.SetUtil;
import jaicore.basic.sets.SetUtil.Pair;
import jaicore.logging.ToJSONStringUtil;

/**
 * For a given <code>Component</code>, a <code>Component Instance</code> defines all parameter values and the
 * required interfaces (recursively) and thus provides a grounding of the respective <code>Component</code>.
 *
 * @author fmohr, mwever
 *
 */
@JsonPropertyOrder(alphabetic = true)
public class ComponentInstance {

	/* Logger */
	private static final Logger L = LoggerFactory.getLogger(ComponentInstance.class);

	/* The component which serves as a kind of "type". */
	private final Component component;

	/* The grounding of the component including parameter values and recursively resolved required interfaces. */
	private final Map<String, String> parameterValues;
	private final Map<String, ComponentInstance> satisfactionOfRequiredInterfaces;

	/**
	 * Constructor for creating a <code>ComponentInstance</code> for a particular <code>Component</code>.
	 * @param component The component that is grounded.
	 * @param parameterValues A map containing the parameter values of this grounding.
	 * @param satisfactionOfRequiredInterfaces The refinement of the required interfaces.
	 */
	public ComponentInstance(@JsonProperty("component") final Component component, @JsonProperty("parameterValues") final Map<String, String> parameterValues,
			@JsonProperty("satisfactionOfRequiredInterfaces") final Map<String, ComponentInstance> satisfactionOfRequiredInterfaces) {
		super();
		this.component = component;
		this.parameterValues = parameterValues;
		this.satisfactionOfRequiredInterfaces = satisfactionOfRequiredInterfaces;
	}

	/**
	 * @return The <code>Component</code> to this <code>ComponentInstance</code>.
	 */
	public Component getComponent() {
		return this.component;
	}

	/**
	 * @return The parameters and how their values were set.
	 */
	public Map<String, String> getParameterValues() {
		return this.parameterValues;
	}

	/**
	 * @return The set of parameters of which the values have been set explicitly.
	 */
	public Collection<Parameter> getParametersThatHaveBeenSetExplicitly() {
		if (this.parameterValues == null) {
			return new ArrayList<>();
		}
		return this.getComponent().getParameters().stream().filter(p -> this.parameterValues.containsKey(p.getName())).collect(Collectors.toList());
	}

	/**
	 * @return The set of parameters of which the values have not been set explicitly.
	 */
	public Collection<Parameter> getParametersThatHaveNotBeenSetExplicitly() {
		return SetUtil.difference(this.component.getParameters(), this.getParametersThatHaveBeenSetExplicitly());
	}

	/**
	 * @param param The parameter for which the value shall be returned.
	 * @return The value of the parameter.
	 */
	public String getParameterValue(final Parameter param) {
		return this.getParameterValue(param.getName());
	}

	/**
	 * @param paramName The name of the parameter for which the value is requested.
	 * @return The value of the parameter with the given name.
	 */
	public String getParameterValue(final String paramName) {
		return this.parameterValues.get(paramName);
	}

	/**
	 * @return This method returns a mapping of interface IDs to component
	 *         instances.
	 */
	public Map<String, ComponentInstance> getSatisfactionOfRequiredInterfaces() {
		return this.satisfactionOfRequiredInterfaces;
	}

	/**
	 * @return A collection of all components contained (recursively) in this <code>ComponentInstance</code>.
	 */
	public Collection<Component> getContainedComponents() {
		Collection<Component> components = new HashSet<>();
		components.add(this.getComponent());
		for (ComponentInstance ci : this.satisfactionOfRequiredInterfaces.values()) {
			components.addAll(ci.getContainedComponents());
		}
		return components;
	}

	/**
	 * This method checks, whether a given list of paths of refinements conforms the constraints for parameter refinements.
	 * @param paths A list of paths of refinements to be checked.
	 * @return Returns true if everything is alright and false if there is an issue with the given paths.
	 */
	public boolean matchesPathRestrictions(final Collection<List<Pair<String, String>>> paths) {
		for (List<Pair<String, String>> path : paths) {
			if (!this.matchesPathRestriction(path)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * This method checks, whether a path of refinements conforms the constraints for parameter refinements.
	 * @param path A path of refinements to be checked.
	 * @return Returns true if everything is alright and false if there is an issue with the given path.
	 */
	public boolean matchesPathRestriction(final List<Pair<String, String>> path) {
		if (path.isEmpty()) {
			return true;
		}

		/* if the first entry is on null, we interpret it as a filter on this component itself */
		int i = 0;
		if (path.get(0).getX() == null) {
			String requiredComponent = path.get(0).getY();
			if (!requiredComponent.equals("*") && !this.component.getName().equals(requiredComponent)) {
				return false;
			}
			i = 1;
		}

		/* now go over the rest of the path and check every entry on conformity */
		ComponentInstance current = this;
		int n = path.size();
		for (; i < n; i++) {
			Pair<String, String> selection = path.get(i);
			if (!current.getComponent().getRequiredInterfaces().containsKey(selection.getX())) {
				throw new IllegalArgumentException("Invalid path restriction " + path + ": " + selection.getX() + " is not a required interface of " + current.getComponent().getName());
			}
			ComponentInstance instanceChosenForRequiredInterface = current.getSatisfactionOfRequiredInterfaces().get(selection.getX());
			if (!selection.getY().equals("*") && !instanceChosenForRequiredInterface.getComponent().getName().equals(selection.getY())) {
				return false;
			}
			current = instanceChosenForRequiredInterface;
		}
		return true;
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

	public String toComponentNameString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getComponent().getName());
		if (!this.satisfactionOfRequiredInterfaces.isEmpty()) {
			sb.append(this.satisfactionOfRequiredInterfaces.entrySet().stream().map(x -> x.getValue().toComponentNameString()).collect(Collectors.toList()).toString());
		}
		return sb.toString();
	}

	@JsonIgnore
	@Override
	public String toString() {
		try {
			return new ObjectMapper().writeValueAsString(this);
		} catch (JsonProcessingException e) {
			L.warn("Could not directly serialize ComponentInstance to JSON:", e);
		}

		Map<String, Object> fields = new HashMap<>();
		fields.put("component", this.component);
		fields.put("parameterValues", this.parameterValues);
		fields.put("satisfactionOfRequiredInterfaces", this.satisfactionOfRequiredInterfaces);
		return ToJSONStringUtil.toJSONString(this.getClass().getSimpleName(), fields);
	}

	/**
	 * Returns the description of a <code>ComponentInstance</code> as a pretty print with indentation.
	 *
	 * @return A string representing this object in JSON format.
	 * @throws IOException An IOException is thrown if the object cannot be serialized to a String.
	 */
	@JsonIgnore
	public String getPrettyPrint() throws IOException {
		return new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT).writeValueAsString(this);
	}

	public String getNestedComponentDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getComponent().getName());
		this.satisfactionOfRequiredInterfaces.values().stream().map(x -> " - " + x.getNestedComponentDescription()).forEach(sb::append);
		return sb.toString();
	}

}
