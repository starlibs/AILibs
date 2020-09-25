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
	 * @return Tells whether the order of realizations of this interface is relevant or not.
	 */
	public boolean isOrdered();

	/**
	 * @return Tells whether there is a limitation that each component must be contained at most once
	 */
	public boolean isUniqueComponents();

	/**
	 * @return Tells whether the required interface is optional
	 */
	public boolean isOptional();

	/**
	 * The minimum can be greater than 0 even if the interface is optional.
	 * In that case, the semantic is: Either the interface is not satisfied or it has at least this number of realizations.
	 *
	 * @return minimum number of required realizations if satisfied at all
	 */
	public int getMin();

	/**
	 * @return maximum number of allowed realizations
	 */
	public int getMax();
}
