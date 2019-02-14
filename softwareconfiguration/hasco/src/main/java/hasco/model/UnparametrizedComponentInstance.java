package hasco.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jaicore.logging.ToJSONStringUtil;

public class UnparametrizedComponentInstance {
	private final String componentName;
	private final Map<String, UnparametrizedComponentInstance> satisfactionOfRequiredInterfaces;

	public UnparametrizedComponentInstance(String componentName,
			Map<String, UnparametrizedComponentInstance> satisfactionOfRequiredInterfaces) {
		super();
		this.componentName = componentName;
		this.satisfactionOfRequiredInterfaces = satisfactionOfRequiredInterfaces;
	}

	public UnparametrizedComponentInstance(ComponentInstance composition) {
		Map<String, ComponentInstance> resolvedRequiredInterfaces = composition.getSatisfactionOfRequiredInterfaces();
		satisfactionOfRequiredInterfaces = new HashMap<>();
		resolvedRequiredInterfaces.keySet().forEach(r -> {
			satisfactionOfRequiredInterfaces.put(r,
					new UnparametrizedComponentInstance(resolvedRequiredInterfaces.get(r)));
		});
		this.componentName = composition.getComponent().getName();
	}

	public String getComponentName() {
		return componentName;
	}

	public Map<String, UnparametrizedComponentInstance> getSatisfactionOfRequiredInterfaces() {
		return satisfactionOfRequiredInterfaces;
	}
	
	/**
	 * Determines the sub-composition under a path of required interfaces
	 **/
	public UnparametrizedComponentInstance getSubComposition(List<String> path) {
		UnparametrizedComponentInstance current = this;
		for (String requiredInterface : path) {
			current = current.getSatisfactionOfRequiredInterfaces().get(requiredInterface);
		}
		return current;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((componentName == null) ? 0 : componentName.hashCode());
		result = prime * result
				+ ((satisfactionOfRequiredInterfaces == null) ? 0 : satisfactionOfRequiredInterfaces.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UnparametrizedComponentInstance other = (UnparametrizedComponentInstance) obj;
		if (componentName == null) {
			if (other.componentName != null)
				return false;
		} else if (!componentName.equals(other.componentName))
			return false;
		if (satisfactionOfRequiredInterfaces == null) {
			if (other.satisfactionOfRequiredInterfaces != null)
				return false;
		} else if (!satisfactionOfRequiredInterfaces.equals(other.satisfactionOfRequiredInterfaces))
			return false;
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
