package hasco.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class Component {
	private final String name;
	private final Collection<String> requiredInterfaces = new HashSet<>(), providedInterfaces = new HashSet<>();
	private final List<Parameter> parameters = new ArrayList<>();

	public Component(String name) {
		super();
		this.name = name;
	}
	
	public Component(String name, Collection<String> requiredInterfaces, Collection<String> providedInterfaces, List<Parameter> parameters) {
		this(name);
		this.requiredInterfaces.addAll(requiredInterfaces);
		this.providedInterfaces.addAll(providedInterfaces);
		this.parameters.addAll(parameters);
	}

	public String getName() {
		return name;
	}

	public Collection<String> getRequiredInterfaces() {
		return requiredInterfaces;
	}

	public Collection<String> getProvidedInterfaces() {
		return providedInterfaces;
	}
	
	public List<Parameter> getParameters() {
		return parameters;
	}

	public void addProvidedInterface(String interfaceName) {
		providedInterfaces.add(interfaceName);
	}
	
	public void addRequiredInterface(String interfaceName) {
		requiredInterfaces.add(interfaceName);
	}
	
	public void addParameter(Parameter param) {
		parameters.add(param);
	}
}
