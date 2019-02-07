package hasco.model;

import java.util.HashMap;
import java.util.Map;

import jaicore.logging.ToJSONStringUtil;

public class UnparametrizedComponentInstance {
	private final Component component;
	private final Map<String, UnparametrizedComponentInstance> satisfactionOfRequiredInterfaces;
	
	public UnparametrizedComponentInstance(Component component, Map<String, UnparametrizedComponentInstance> satisfactionOfRequiredInterfaces) {
		super();
		this.component = component;
		this.satisfactionOfRequiredInterfaces = satisfactionOfRequiredInterfaces;
	}
	
	public UnparametrizedComponentInstance(ComponentInstance composition) {
		Map<String, ComponentInstance> resolvedRequiredInterfaces = composition.getSatisfactionOfRequiredInterfaces();
		satisfactionOfRequiredInterfaces = new HashMap<>();
		resolvedRequiredInterfaces.keySet().forEach(r -> {
			satisfactionOfRequiredInterfaces.put(r, new UnparametrizedComponentInstance(resolvedRequiredInterfaces.get(r)));
		});
		this.component = composition.getComponent();
	}

	public Component getComponent() {
		return component;
	}

	public Map<String, UnparametrizedComponentInstance> getSatisfactionOfRequiredInterfaces() {
		return satisfactionOfRequiredInterfaces;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((component == null) ? 0 : component.hashCode());
		result = prime * result + ((satisfactionOfRequiredInterfaces == null) ? 0 : satisfactionOfRequiredInterfaces.hashCode());
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
		if (component == null) {
			if (other.component != null)
				return false;
		} else if (!component.equals(other.component))
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
		fields.put("component", this.component);
		fields.put("satisfactionOfRequiredInterfaces", this.satisfactionOfRequiredInterfaces);
		return ToJSONStringUtil.toJSONString(this.getClass().getSimpleName(), fields);
	}
}
