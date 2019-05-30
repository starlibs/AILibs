package hasco.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jaicore.logging.ToJSONStringUtil;

public class UnparametrizedComponentInstance {
	private final String componentName;
	private final Map<String, UnparametrizedComponentInstance> satisfactionOfRequiredInterfaces;

	public UnparametrizedComponentInstance(final String componentName, final Map<String, UnparametrizedComponentInstance> satisfactionOfRequiredInterfaces) {
		super();
		this.componentName = componentName;
		this.satisfactionOfRequiredInterfaces = satisfactionOfRequiredInterfaces;
	}

	public UnparametrizedComponentInstance(final ComponentInstance composition) {
		Map<String, ComponentInstance> resolvedRequiredInterfaces = composition.getSatisfactionOfRequiredInterfaces();
		this.satisfactionOfRequiredInterfaces = new HashMap<>();
		resolvedRequiredInterfaces.keySet().forEach(r -> {
			this.satisfactionOfRequiredInterfaces.put(r, new UnparametrizedComponentInstance(resolvedRequiredInterfaces.get(r)));
		});
		this.componentName = composition.getComponent().getName();
	}

	public String getComponentName() {
		return this.componentName;
	}

	public Map<String, UnparametrizedComponentInstance> getSatisfactionOfRequiredInterfaces() {
		return this.satisfactionOfRequiredInterfaces;
	}

	/**
	 * Determines the sub-composition under a path of required interfaces
	 **/
	public UnparametrizedComponentInstance getSubComposition(final List<String> path) {
		UnparametrizedComponentInstance current = this;
		for (String requiredInterface : path) {
			if (!current.getSatisfactionOfRequiredInterfaces().containsKey(requiredInterface)) {
				throw new IllegalArgumentException("Invalid path " + path + " (size " + path.size() + "). The component " + current.getComponentName() + " does not have a required interface with id \"" + requiredInterface + "\"");
			}
			current = current.getSatisfactionOfRequiredInterfaces().get(requiredInterface);
		}
		return current;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.componentName == null) ? 0 : this.componentName.hashCode());
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
		UnparametrizedComponentInstance other = (UnparametrizedComponentInstance) obj;
		if (this.componentName == null) {
			if (other.componentName != null) {
				return false;
			}
		} else if (!this.componentName.equals(other.componentName)) {
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

	@Override
	public String toString() {
		Map<String, Object> fields = new HashMap<>();
		fields.put("componentName", this.componentName);
		fields.put("satisfactionOfRequiredInterfaces", this.satisfactionOfRequiredInterfaces);
		return ToJSONStringUtil.toJSONString(this.getClass().getSimpleName(), fields);
	}
}
