package hasco.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import jaicore.basic.sets.PartialOrderedSet;

@JsonPropertyOrder({ "name", "parameters", "dependencies", "providedInterfaces", "requiredInterfaces" })
public class Component {
	private final String name;
	private Collection<String> providedInterfaces = new ArrayList<>();
	private LinkedHashMap<String, String> requiredInterfaces = new LinkedHashMap<>();
	private PartialOrderedSet<Parameter> parameters = new PartialOrderedSet<>();
	private Collection<Dependency> dependencies = new ArrayList<>();

	public Component(@JsonProperty("name") final String name) {
		super();
		this.name = name;
	}

	// Json constructor
	@JsonCreator
	public Component(@JsonProperty("name") final String name,
			@JsonProperty("providedInterfaces") Collection<String> providedInterfaces,
			@JsonProperty("requiredInterfaces") List<Map<String, String>> requiredInterfaces,
			@JsonProperty("parameters") PartialOrderedSet<Parameter> parameters,
			@JsonProperty("dependencies") List<Dependency> dependencies) {
		super();
		this.name = name;
		this.providedInterfaces = providedInterfaces;
		this.requiredInterfaces = new LinkedHashMap<>();
		requiredInterfaces.iterator().next().forEach(this.requiredInterfaces::put);
		this.parameters = parameters;
		this.dependencies = dependencies;
	}

	public Component(final String name, final TreeMap<String, String> requiredInterfaces,
			final Collection<String> providedInterfaces, final List<Parameter> parameters,
			final Collection<Dependency> dependencies) {
		this(name);
		this.requiredInterfaces.putAll(requiredInterfaces);
		this.providedInterfaces.addAll(providedInterfaces);
		if (!this.providedInterfaces.contains(name)) {
			this.providedInterfaces.add(name);
		}
		parameters.forEach(this::addParameter);
		this.dependencies.addAll(dependencies);
	}

	public String getName() {
		return this.name;
	}

	public LinkedHashMap<String, String> getRequiredInterfaces() {
		return this.requiredInterfaces;
	}

	public Collection<String> getProvidedInterfaces() {
		return this.providedInterfaces;
	}

	public PartialOrderedSet<Parameter> getParameters() {
		return this.parameters;
	}

	public Parameter getParameterWithName(final String paramName) {
		Optional<Parameter> param = this.parameters.stream().filter(p -> p.getName().equals(paramName)).findFirst();
		if (!param.isPresent()) {
			throw new IllegalArgumentException(
					"Component " + this.name + " has no parameter with name \"" + paramName + "\"");
		}
		return param.get();
	}

	public void addProvidedInterface(final String interfaceName) {
		this.providedInterfaces.add(interfaceName);
	}

	public void addRequiredInterface(final String interfaceID, final String interfaceName) {
		this.requiredInterfaces.put(interfaceID, interfaceName);
	}

	public void addParameter(final Parameter param) {
		assert !this.parameters.stream().filter(p -> p.getName().equals(param.getName())).findAny()
				.isPresent() : "Component " + this.name + " already has parameter with name " + param.getName();
		this.parameters.add(param);
	}

	public void addDependency(final Dependency dependency) {

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

	public Collection<Dependency> getDependencies() {
		return this.dependencies;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(this.providedInterfaces);
		sb.append(":");
		sb.append(this.name);
		sb.append("(");
		boolean first = true;
		for (Parameter p : this.parameters) {
			if (first) {
				first = false;
			} else {
				sb.append(",");
			}
			sb.append(p);
		}
		sb.append(")");
		sb.append(":");
		sb.append(this.requiredInterfaces);

		return sb.toString();
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
}
