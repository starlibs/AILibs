package hasco.core;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.serialization.ComponentLoader;
import jaicore.basic.IObjectEvaluator;

public class SoftwareConfigurationProblem<V extends Comparable<V>> {
	private final Collection<Component> components;
	private final String requiredInterface;
	private final IObjectEvaluator<ComponentInstance, V> compositionEvaluator;

	public SoftwareConfigurationProblem(File configurationFile, String requiredInerface, IObjectEvaluator<ComponentInstance, V> compositionEvaluator) throws IOException {
		ComponentLoader cl = new ComponentLoader();
		cl.loadComponents(configurationFile);
		this.components = cl.getComponents();
		this.requiredInterface = requiredInerface;
		this.compositionEvaluator = compositionEvaluator;
	}

	public SoftwareConfigurationProblem(Collection<Component> components, String requiredInterface, IObjectEvaluator<ComponentInstance, V> compositionEvaluator) {
		super();
		this.components = components;
		this.requiredInterface = requiredInterface;
		this.compositionEvaluator = compositionEvaluator;
	}
	
	public SoftwareConfigurationProblem(SoftwareConfigurationProblem<V> problem) {
		this.components = problem.getComponents();
		this.requiredInterface = problem.requiredInterface;
		this.compositionEvaluator = problem.compositionEvaluator;
	}

	public Collection<Component> getComponents() {
		return components;
	}

	public String getRequiredInterface() {
		return requiredInterface;
	}

	public IObjectEvaluator<ComponentInstance, V> getCompositionEvaluator() {
		return compositionEvaluator;
	}
}
