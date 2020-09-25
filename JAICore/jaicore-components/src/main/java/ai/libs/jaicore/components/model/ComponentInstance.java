package ai.libs.jaicore.components.model;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.components.api.IComponent;
import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.api.IParameter;
import ai.libs.jaicore.logging.ToJSONStringUtil;

/**
 * For a given <code>Component</code>, a <code>Component Instance</code> defines all parameter values and the required interfaces (recursively) and thus provides a grounding of the respective
 * <code>Component</code>.
 *
 * @author fmohr, mwever
 *
 */
@JsonPropertyOrder(alphabetic = true)
public class ComponentInstance  implements IComponentInstance, Serializable {

	/* Auto-generated serial version UID. */
	private static final long serialVersionUID = 714378153827839502L;

	/* The component which serves as a kind of "type". */
	private final IComponent component;

	/* The grounding of the component including parameter values and recursively resolved required interfaces. */
	private final Map<String, String> parameterValues;
	private final Map<String, List<IComponentInstance>> satisfactionOfRequiredInterfaces;

	private final Map<String, String> annotations = new HashMap<>();

	@SuppressWarnings("unused")
	private ComponentInstance() {
		// for serialization purposes
		this.component = null;
		this.parameterValues = null;
		this.satisfactionOfRequiredInterfaces = null;
	}

	public ComponentInstance(final ComponentInstance other) {
		this.component = other.component;
		this.parameterValues = new HashMap<>(other.parameterValues);
		this.satisfactionOfRequiredInterfaces = new HashMap<>();
		other.satisfactionOfRequiredInterfaces.entrySet().forEach(x -> this.satisfactionOfRequiredInterfaces.put(x.getKey(), new ArrayList<>()));
		other.satisfactionOfRequiredInterfaces.entrySet().forEach(x -> x.getValue().forEach(ci -> this.satisfactionOfRequiredInterfaces.get(x.getKey()).add(new ComponentInstance((ComponentInstance)ci))));
		other.annotations.entrySet().forEach(x -> this.annotations.put(x.getKey(), x.getValue()));
	}

	/**
	 * Constructor for creating a <code>ComponentInstance</code> for a particular <code>Component</code>.
	 *
	 * @param component
	 *            The component that is grounded.
	 * @param parameterValues
	 *            A map containing the parameter values of this grounding.
	 * @param satisfactionOfRequiredInterfaces
	 *            The refinement of the required interfaces.
	 */
	public ComponentInstance(@JsonProperty("component") final IComponent component, @JsonProperty("parameterValues") final Map<String, String> parameterValues,
			@JsonProperty("satisfactionOfRequiredInterfaces") final Map<String, List<IComponentInstance>> satisfactionOfRequiredInterfaces) {
		super();
		this.component = component;
		this.parameterValues = parameterValues;
		this.satisfactionOfRequiredInterfaces = satisfactionOfRequiredInterfaces;
	}

	/**
	 * @return The <code>Component</code> to this <code>ComponentInstance</code>.
	 */
	@Override
	public IComponent getComponent() {
		return this.component;
	}

	/**
	 * @return The parameters and how their values were set.
	 */
	@Override
	public Map<String, String> getParameterValues() {
		return this.parameterValues;
	}

	/**
	 * @return The set of parameters of which the values have been set explicitly.
	 */
	@Override
	public Collection<IParameter> getParametersThatHaveBeenSetExplicitly() {
		if (this.parameterValues == null) {
			return new ArrayList<>();
		}
		return this.getComponent().getParameters().stream().filter(p -> this.parameterValues.containsKey(p.getName())).collect(Collectors.toList());
	}

	/**
	 * @return The set of parameters of which the values have not been set explicitly.
	 */
	@Override
	public Collection<IParameter> getParametersThatHaveNotBeenSetExplicitly() {
		return SetUtil.difference(this.component.getParameters(), this.getParametersThatHaveBeenSetExplicitly());
	}

	/**
	 * @param param
	 *            The parameter for which the value shall be returned.
	 * @return The value of the parameter.
	 */
	@Override
	public String getParameterValue(final IParameter param) {
		return this.getParameterValue(param.getName());
	}

	/**
	 * @param paramName
	 *            The name of the parameter for which the value is requested.
	 * @return The value of the parameter with the given name.
	 */
	@Override
	public String getParameterValue(final String paramName) {
		return this.parameterValues.get(paramName);
	}

	/**
	 * @return This method returns a mapping of interface IDs to component instances.
	 */
	@Override
	public Map<String, List<IComponentInstance>> getSatisfactionOfRequiredInterfaces() {
		return this.satisfactionOfRequiredInterfaces;
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
			sb.append(this.satisfactionOfRequiredInterfaces.entrySet().stream().map(x -> x.getValue().stream().map(ci -> ((ComponentInstance)ci).toComponentNameString()).collect(Collectors.joining())).collect(Collectors.toList()).toString());
		}
		return sb.toString();
	}

	@JsonIgnore
	@Override
	public String toString() {
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
	 * @throws IOException
	 *             An IOException is thrown if the object cannot be serialized to a String.
	 */
	@JsonIgnore
	public String getPrettyPrint() throws IOException {
		return new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT).writeValueAsString(this);
	}

	public String getNestedComponentDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getComponent().getName());
		this.satisfactionOfRequiredInterfaces.values().stream().map(x -> " - " + x.stream().map(ci -> ((ComponentInstance)ci).getNestedComponentDescription()).collect(Collectors.joining())).forEach(sb::append);
		return sb.toString();
	}

	/**
	 * Add an annotation to this component instance.
	 * @param key The key of how to address this annotation.
	 * @param annotation The annotation value.
	 */
	public void putAnnotation(final String key, final String annotation) {
		this.annotations.put(key, annotation);
	}

	/**
	 * Retrieve an annotation by its key.
	 * @param key The key for which to retrieve the annotation.
	 * @return The annotation value.
	 */
	public String getAnnotation(final String key) {
		return this.annotations.get(key);
	}

	public void appendAnnotation(final String key, final String annotation) {
		if (this.annotations.containsKey(key)) {
			this.annotations.put(key, this.annotations.get(key) + annotation);
		} else {
			this.annotations.put(key, annotation);
		}
	}

	@Override
	public List<IComponentInstance> getSatisfactionOfRequiredInterface(final String idOfRequiredInterface) {
		if (!this.component.hasRequiredInterfaceWithId(idOfRequiredInterface)) {
			throw new IllegalArgumentException("\"" + idOfRequiredInterface + "\" is not a valid required interface id of component " + this.component.getName()+ ". Valid ids are: " + this.component.getRequiredInterfaces().stream().map(ri -> "\n\t- " + ri.getId()).collect(Collectors.joining()));
		}
		return this.satisfactionOfRequiredInterfaces.get(idOfRequiredInterface);
	}
}
