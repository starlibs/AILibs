package hasco.query;

import java.util.Collection;

import hasco.model.Component;

public class CompositionProblem {
	private Collection<Component> components;
	private String requiredInterface;

	public CompositionProblem(Collection<Component> components, String requiredInterface) {
		super();
		this.components = components;
		this.requiredInterface = requiredInterface;
	}

	public Collection<Component> getComponents() {
		return components;
	}

	public void setComponents(Collection<Component> components) {
		this.components = components;
	}

	public String getRequiredInterface() {
		return requiredInterface;
	}

	public void setRequiredInterface(String requiredInterface) {
		this.requiredInterface = requiredInterface;
	}
}
