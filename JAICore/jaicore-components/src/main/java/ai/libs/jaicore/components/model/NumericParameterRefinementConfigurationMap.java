package ai.libs.jaicore.components.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ai.libs.jaicore.components.api.IComponent;
import ai.libs.jaicore.components.api.INumericParameterRefinementConfigurationMap;
import ai.libs.jaicore.components.api.INumericParameterRefinementConfiguration;
import ai.libs.jaicore.components.api.IParameter;

public class NumericParameterRefinementConfigurationMap extends HashMap<String, Map<String, NumericParameterRefinementConfiguration>> implements INumericParameterRefinementConfigurationMap {

	@Override
	public Collection<String> getParameterNamesForWhichARefinementIsDefined(final IComponent component) {
		return this.get(component.getName()).keySet();
	}

	@Override
	public INumericParameterRefinementConfiguration getRefinement(final IComponent component, final IParameter parameter) {
		return this.getRefinement(component.getName(), parameter.getName());
	}

	@Override
	public INumericParameterRefinementConfiguration getRefinement(final String componentName, final String parameterName) {
		return this.get(componentName).get(parameterName);
	}

}
