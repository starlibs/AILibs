package ai.libs.jaicore.components.api;

import java.util.Collection;

public interface INumericParameterRefinementConfigurationMap {

	public Collection<String> getParameterNamesForWhichARefinementIsDefined(IComponent component);

	public INumericParameterRefinementConfiguration getRefinement(IComponent component, IParameter parameter);

	public INumericParameterRefinementConfiguration getRefinement(String componentName, String parameterName);
}
