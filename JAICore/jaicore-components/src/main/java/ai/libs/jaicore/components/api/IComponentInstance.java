package ai.libs.jaicore.components.api;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface IComponentInstance extends Serializable {

	public IComponent getComponent();

	/**
	 * @return The parameters and how their values were set.
	 */
	public Map<String, String> getParameterValues();

	/**
	 * @return The set of parameters of which the values have been set explicitly.
	 */
	public Collection<IParameter> getParametersThatHaveBeenSetExplicitly();

	/**
	 * @return The set of parameters of which the values have not been set explicitly.
	 */
	public Collection<IParameter> getParametersThatHaveNotBeenSetExplicitly();

	/**
	 * @param param
	 *            The parameter for which the value shall be returned.
	 * @return The value of the parameter.
	 */
	public String getParameterValue(final IParameter param);

	/**
	 * @param paramName
	 *            The name of the parameter for which the value is requested.
	 * @return The value of the parameter with the given name.
	 */
	public String getParameterValue(final String paramName);

	/**
	 * @return This method returns a mapping of interface IDs to component instances.
	 */
	public Map<String, List<IComponentInstance>> getSatisfactionOfRequiredInterfaces();

	public List<IComponentInstance> getSatisfactionOfRequiredInterface(String idOfRequiredInterface);
}
