package hasco.core;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.serialization.ComponentLoader;
import jaicore.basic.IObjectEvaluator;
import jaicore.logging.ToJSONStringUtil;

public class SoftwareConfigurationProblem<V extends Comparable<V>> {
	private final Collection<Component> components;
	private final String requiredInterface;
	private final IObjectEvaluator<ComponentInstance, V> compositionEvaluator;

	public SoftwareConfigurationProblem(final File configurationFile, final String requiredInerface, final IObjectEvaluator<ComponentInstance, V> compositionEvaluator) throws IOException {
		ComponentLoader cl = new ComponentLoader();
		cl.loadComponents(configurationFile);
		this.components = cl.getComponents();
		this.requiredInterface = requiredInerface;
		this.compositionEvaluator = compositionEvaluator;
	}

	public SoftwareConfigurationProblem(final Collection<Component> components, final String requiredInterface, final IObjectEvaluator<ComponentInstance, V> compositionEvaluator) {
		super();
		this.components = components;
		this.requiredInterface = requiredInterface;
		this.compositionEvaluator = compositionEvaluator;
	}

	public SoftwareConfigurationProblem(final SoftwareConfigurationProblem<V> problem) {
		this.components = problem.getComponents();
		this.requiredInterface = problem.requiredInterface;
		this.compositionEvaluator = problem.compositionEvaluator;
	}

	public Collection<Component> getComponents() {
		return this.components;
	}

	public String getRequiredInterface() {
		return this.requiredInterface;
	}

	public IObjectEvaluator<ComponentInstance, V> getCompositionEvaluator() {
		return this.compositionEvaluator;
	}

	@Override
	public String toString() {
		Map<String, Object> fields = new HashMap<>();
		fields.put("components", this.components);
		fields.put("requiredInterface", this.requiredInterface);
		fields.put("compositionEvaluator", this.compositionEvaluator);
		return ToJSONStringUtil.toJSONString(this.getClass().getSimpleName(), fields);
	}
}
