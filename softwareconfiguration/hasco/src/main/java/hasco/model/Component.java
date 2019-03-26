package hasco.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jaicore.basic.sets.PartialOrderedSet;
import jaicore.logging.ToJSONStringUtil;

/**
 * A <code>Component</code> is described by
 * - a name
 * - a collection of provided interfaces
 * - a list of required interfaces
 * - a set of parameters
 * - a list of dependencies
 * and can be used to describe any kind of components and model complex multi-component systems.
 * More specifically, <code>Component</code>s are used to model the search space of HASCO. By recursively
 * resolving required interfaces until there are no open choices left, HASCO may transform your component
 * description automatically into an HTN planning problem to automatically optimize a component setup
 * for a specific task.
 *
 * @author fmohr, wever
 */
@JsonPropertyOrder({ "name", "parameters", "dependencies", "providedInterfaces", "requiredInterfaces" })
public class Component {

	/* Logger */
	private static final Logger L = LoggerFactory.getLogger(Component.class);

	/* Description of the component. */
	private final String name;
	private Collection<String> providedInterfaces = new ArrayList<>();
	private LinkedHashMap<String, String> requiredInterfaces = new LinkedHashMap<>();
	private PartialOrderedSet<Parameter> parameters = new PartialOrderedSet<>();
	private Collection<Dependency> dependencies = new ArrayList<>();

	/**
	 * Constructor creating an empty <code>Component</code> with a specific name.
	 * @param name The name of the <code>Component</code>.
	 */
	public Component(final String name) {
		super();
		this.name = name;
		this.getProvidedInterfaces().add(this.name);
	}

	/**
	 * Constructor for a component giving the provided and required interfaces, the collection of parameters and a list of dependencies.
	 * @param name The name of the <code>Component</code>.
	 * @param providedInterfaces The collection of provided interfaces.
	 * @param requiredInterfaces The list of required interfaces.
	 * @param parameters Parameters of the <code>Component</code>.
	 * @param dependencies A list of dependencies to constrain the values of parameters (may be empty).
	 */
	@JsonCreator
	public Component(@JsonProperty("name") final String name, @JsonProperty("providedInterfaces") final Collection<String> providedInterfaces, @JsonProperty("requiredInterfaces") final List<Map<String, String>> requiredInterfaces,
			@JsonProperty("parameters") final PartialOrderedSet<Parameter> parameters, @JsonProperty("dependencies") final List<Dependency> dependencies) {
		this(name);
		this.providedInterfaces = providedInterfaces;
		this.requiredInterfaces = new LinkedHashMap<>();
		requiredInterfaces.stream().forEach(this.requiredInterfaces::putAll);
		this.parameters = parameters;
		this.dependencies = dependencies;
	}

	/**
	 * @return The name of the Component.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @return The map of required interfaces.
	 */
	public Map<String, String> getRequiredInterfaces() {
		return this.requiredInterfaces;
	}

	/**
	 * @return The collection of provided interfaces.
	 */
	public Collection<String> getProvidedInterfaces() {
		return this.providedInterfaces;
	}

	/**
	 * @return The set of parameters of this Component.
	 */
	public PartialOrderedSet<Parameter> getParameters() {
		return this.parameters;
	}

	/**
	 * Returns the parameter for a given name.
	 * @param paramName The name of the parameter to be returned.
	 * @return The parameter for the given name.
	 */
	public Parameter getParameterWithName(final String paramName) {
		Optional<Parameter> param = this.parameters.stream().filter(p -> p.getName().equals(paramName)).findFirst();
		if (!param.isPresent()) {
			throw new IllegalArgumentException("Component " + this.name + " has no parameter with name \"" + paramName + "\"");
		}
		return param.get();
	}

	/**
	 * @return The collection of dependencies on the parameters of this <code>Component</code>.
	 */
	public Collection<Dependency> getDependencies() {
		return this.dependencies;
	}

	/**
	 * Adds another provided interface to the collection of provided interfaces.
	 * @param interfaceName The interface to be added to the provided interfaces.
	 */
	public boolean addProvidedInterface(final String interfaceName) {
		if (!this.providedInterfaces.contains(interfaceName)) {
			return this.providedInterfaces.add(interfaceName);
		} else {
			return false;
		}
	}

	/**
	 * Adds an additional required interface with an ID (local identifier) and an interface name (provided interface of another Component) to the required interfaces of this Component.
	 * @param interfaceID The local identifier to reference the specific required interface.
	 * @param interfaceName The provided interface of another component.
	 */
	public void addRequiredInterface(final String interfaceID, final String interfaceName) {
		this.requiredInterfaces.put(interfaceID, interfaceName);
	}

	/**
	 * Adds a parameter to the set of parameters iff the parameter or another parameter with the same name does not yet exist.
	 * @param param The parameter to be added.
	 */
	public void addParameter(final Parameter param) {
		if (this.parameters.stream().anyMatch(p -> p.getName().equals(param.getName()))) {
			throw new IllegalArgumentException("Component " + this.name + " already has a parameter with name " + param.getName());
		}
		this.parameters.add(param);
	}

	/**
	 * Adds a dependency constraint to the dependencies of this Component.
	 * @param dependency The dependency to be added.
	 */
	public void addDependency(final Dependency dependency) {
		/*
		 * check whether this dependency is coherent with the current partial order on
		 * the parameters
		 */
		Collection<Parameter> paramsInPremise = new HashSet<>();
		dependency.getPremise().forEach(c -> c.forEach(i -> paramsInPremise.add(i.getX())));
		Collection<Parameter> paramsInConclusion = new HashSet<>();
		dependency.getConclusion().forEach(i -> paramsInConclusion.add(i.getX()));
		for (Parameter before : paramsInPremise) {
			for (Parameter after : paramsInConclusion) {
				this.parameters.requireABeforeB(before, after);
			}
		}

		/* add the dependency to the set of dependencies */
		this.dependencies.add(dependency);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.dependencies == null) ? 0 : this.dependencies.hashCode());
		result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
		result = prime * result + ((this.parameters == null) ? 0 : this.parameters.hashCode());
		result = prime * result + ((this.providedInterfaces == null) ? 0 : this.providedInterfaces.hashCode());
		result = prime * result + ((this.requiredInterfaces == null) ? 0 : this.requiredInterfaces.hashCode());
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
		Component other = (Component) obj;
		if (this.dependencies == null) {
			if (other.dependencies != null) {
				return false;
			}
		} else if (!this.dependencies.equals(other.dependencies)) {
			return false;
		}
		if (this.name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!this.name.equals(other.name)) {
			return false;
		}
		if (this.parameters == null) {
			if (other.parameters != null) {
				return false;
			}
		} else if (!this.parameters.equals(other.parameters)) {
			return false;
		}
		if (this.providedInterfaces == null) {
			if (other.providedInterfaces != null) {
				return false;
			}
		} else if (!this.providedInterfaces.equals(other.providedInterfaces)) {
			return false;
		}
		if (this.requiredInterfaces == null) {
			if (other.requiredInterfaces != null) {
				return false;
			}
		} else if (!this.requiredInterfaces.equals(other.requiredInterfaces)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		try {
			return new ObjectMapper().writeValueAsString(this);
		} catch (JsonProcessingException e) {
			L.warn("Could not directly serialize Component to JSON: ", e);
		}

		Map<String, Object> fields = new HashMap<>();
		fields.put("name", this.name);
		fields.put("providedInterfaces", this.providedInterfaces);
		fields.put("requiredInterfaces", this.requiredInterfaces);
		fields.put("parameters", this.parameters);
		return ToJSONStringUtil.toJSONString(this.getClass().getSimpleName(), fields);
	}

}
