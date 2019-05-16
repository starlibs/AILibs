package jaicore.ml.core.dataset;

import jaicore.ml.core.dataset.attribute.IAttributeValue;

/**
 * Interface of an instance that has a target value.
 *
 * @author wever, fmohr
 *
 */
public interface ILabeledInstance extends IInstance {

	/**
	 * Getter for the value of the target attribute.
	 *
	 * @param type The type to bind the value of the target attribute.
	 * @return The value of the traget attribute.
	 */
	public <T> IAttributeValue<T> getTargetValue(Class<T> type);
}
