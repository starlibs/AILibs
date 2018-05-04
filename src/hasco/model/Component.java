package hasco.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import jaicore.order.PartialOrderedSet;

public class Component {
	private final String name;
	private final Collection<String> requiredInterfaces = new HashSet<>(), providedInterfaces = new HashSet<>();
	private final PartialOrderedSet<Parameter> parameters = new PartialOrderedSet<>();
	private final Collection<Dependency> dependencies = new ArrayList<>();

	public Component(final String name) {
		super();
		this.name = name;
	}

	public Component(final String name, final Collection<String> requiredInterfaces, final Collection<String> providedInterfaces, final List<Parameter> parameters,
			final Collection<Dependency> dependencies) {
		this(name);
		this.requiredInterfaces.addAll(requiredInterfaces);
		this.providedInterfaces.addAll(providedInterfaces);
		this.parameters.addAll(parameters);
		this.dependencies.addAll(dependencies);
	}

	public String getName() {
		return this.name;
	}

	public Collection<String> getRequiredInterfaces() {
		return this.requiredInterfaces;
	}

	public Collection<String> getProvidedInterfaces() {
		return this.providedInterfaces;
	}

	public PartialOrderedSet<Parameter> getParameters() {
		return this.parameters;
	}
	
	public Parameter getParameter(String paramName) {
		Optional<Parameter> param = parameters.stream().filter(p -> p.getName().equals(paramName)).findFirst();
		if (!param.isPresent())
			throw new IllegalArgumentException("Component " + name + " has no parameter with name \"" + paramName + "\"");
		return param.get();
	}

	public void addProvidedInterface(final String interfaceName) {
		this.providedInterfaces.add(interfaceName);
	}

	public void addRequiredInterface(final String interfaceName) {
		this.requiredInterfaces.add(interfaceName);
	}

	public void addParameter(final Parameter param) {
		this.parameters.add(param);
	}

	public void addDependency(final Dependency dependency) {
		
		/* check whether this dependency is coherent with the current partial order on the parameters */
		Collection<Parameter> paramsInPremise = new HashSet<>();
		dependency.getPremise().forEach(c -> c.forEach(i -> paramsInPremise.add(i.getX())));
		Collection<Parameter> paramsInConclusion = new HashSet<>();
		dependency.getConclusion().forEach(i -> paramsInConclusion.add(i.getX()));
		for (Parameter before : paramsInPremise) {
			for (Parameter after : paramsInConclusion) {
				this.parameters.requireABeforeB(before, after);
			}
		}
		
		System.out.println(this.parameters.getLinearization());
		/* add the dependency to the set of dependencies */
		this.dependencies.add(dependency);
	}

	public Collection<Dependency> getDependencies() {
		return dependencies;
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
}
