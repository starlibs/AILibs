package ai.libs.jaicore.components.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.logging.ToJSONStringUtil;

public class UnparametrizedComponentInstance {
	private final String componentName;
	private final Map<String, UnparametrizedComponentInstance> satisfactionOfRequiredInterfaces;

	public UnparametrizedComponentInstance(final String componentName, final Map<String, UnparametrizedComponentInstance> satisfactionOfRequiredInterfaces) {
		super();
		this.componentName = componentName;
		this.satisfactionOfRequiredInterfaces = satisfactionOfRequiredInterfaces;
	}

	public UnparametrizedComponentInstance(final IComponentInstance composition) {
		Map<String, IComponentInstance> resolvedRequiredInterfaces = composition.getSatisfactionOfRequiredInterfaces();
		this.satisfactionOfRequiredInterfaces = new HashMap<>();
		resolvedRequiredInterfaces.keySet().forEach(r -> this.satisfactionOfRequiredInterfaces.put(r, new UnparametrizedComponentInstance(resolvedRequiredInterfaces.get(r))));
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
		return new HashCodeBuilder().append(this.componentName).append(this.satisfactionOfRequiredInterfaces).hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || obj.getClass() != UnparametrizedComponentInstance.class) {
			return false;
		}
		UnparametrizedComponentInstance other = (UnparametrizedComponentInstance) obj;
		return new EqualsBuilder().append(this.componentName, other.componentName).append(this.satisfactionOfRequiredInterfaces, other.satisfactionOfRequiredInterfaces).isEquals();
	}

	@Override
	public String toString() {
		Map<String, Object> fields = new HashMap<>();
		fields.put("componentName", this.componentName);
		fields.put("satisfactionOfRequiredInterfaces", this.satisfactionOfRequiredInterfaces);
		return ToJSONStringUtil.toJSONString(this.getClass().getSimpleName(), fields);
	}
}
