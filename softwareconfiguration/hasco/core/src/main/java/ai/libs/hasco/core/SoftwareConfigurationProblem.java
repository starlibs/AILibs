package ai.libs.hasco.core;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.api4.java.common.attributedobjects.IObjectEvaluator;

import ai.libs.hasco.model.Component;
import ai.libs.hasco.model.ComponentInstance;
import ai.libs.hasco.serialization.ComponentLoader;
import ai.libs.jaicore.logging.ToJSONStringUtil;

public class SoftwareConfigurationProblem<V extends Comparable<V>> {
	private final Collection<Component> components;
	private final String requiredInterface;
	private final IObjectEvaluator<ComponentInstance, V> compositionEvaluator;

	public SoftwareConfigurationProblem(final File configurationFile, final String requiredInerface, final IObjectEvaluator<ComponentInstance, V> compositionEvaluator) throws IOException {
		ComponentLoader cl = new ComponentLoader();
		cl.loadComponents(configurationFile);
		this.components = cl.getComponents();
		this.requiredInterface = requiredInerface;
		if (requiredInerface == null || requiredInerface.isEmpty()) {
			throw new IllegalArgumentException("Invalid required interface \"" + requiredInerface + "\"");
		}
		this.compositionEvaluator = compositionEvaluator;
	}

	public SoftwareConfigurationProblem(final Collection<Component> components, final String requiredInterface, final IObjectEvaluator<ComponentInstance, V> compositionEvaluator) {
		super();
		this.components = components;
		this.requiredInterface = requiredInterface;
		this.compositionEvaluator = compositionEvaluator;
		if (requiredInterface == null || requiredInterface.isEmpty()) {
			throw new IllegalArgumentException("Invalid required interface \"" + requiredInterface + "\"");
		}
	}

	public SoftwareConfigurationProblem(final SoftwareConfigurationProblem<V> problem) {
		this(problem.getComponents(), problem.requiredInterface, problem.compositionEvaluator);
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.components == null) ? 0 : this.components.hashCode());
		result = prime * result + ((this.compositionEvaluator == null) ? 0 : this.compositionEvaluator.hashCode());
		result = prime * result + ((this.requiredInterface == null) ? 0 : this.requiredInterface.hashCode());
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
		SoftwareConfigurationProblem other = (SoftwareConfigurationProblem) obj;
		if (this.components == null) {
			if (other.components != null) {
				return false;
			}
		} else if (!this.components.equals(other.components)) {
			return false;
		}
		if (this.compositionEvaluator == null) {
			if (other.compositionEvaluator != null) {
				return false;
			}
		} else if (!this.compositionEvaluator.equals(other.compositionEvaluator)) {
			return false;
		}
		if (this.requiredInterface == null) {
			if (other.requiredInterface != null) {
				return false;
			}
		} else if (!this.requiredInterface.equals(other.requiredInterface)) {
			return false;
		}
		return true;
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
