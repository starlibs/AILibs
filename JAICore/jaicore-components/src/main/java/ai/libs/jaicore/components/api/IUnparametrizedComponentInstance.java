package ai.libs.jaicore.components.api;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface IUnparametrizedComponentInstance extends Serializable {

	public IComponent getComponent();

	/**
	 * @return This method returns a mapping of interface IDs to component instances.
	 */
	public Map<String, List<IUnparametrizedComponentInstance>> getSatisfactionOfRequiredInterfaces();

	public List<IUnparametrizedComponentInstance> getSatisfactionOfRequiredInterface(String idOfRequiredInterface);
}
