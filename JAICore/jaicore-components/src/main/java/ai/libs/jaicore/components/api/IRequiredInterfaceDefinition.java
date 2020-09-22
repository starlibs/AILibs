package ai.libs.jaicore.components.api;

import java.io.Serializable;

public interface IRequiredInterfaceDefinition extends Serializable {

	/**
	 * @return the name of the interface
	 */
	public String getName();

	/**
	 * @return the id of this interface within the component that defines it (can be interpreted as the *role* name of this interface in the component)
	 */
	public String getId();

	/**
	 * @return minimum number of required realizations
	 */
	public int getMin();

	/**
	 * @return maximium number of allowed realizations
	 */
	public int getMax();
}
