package jaicore.ml.core.dataset;

/**
 * Interface of an instance that has a target value.
 *
 * @author wever, fmohr
 *
 */
public interface ILabeledInstance<T> {

	/**
	 * Getter for the value of the target attribute.
	 *
	 * @param type The type to bind the value of the target attribute.
	 * @return The value of the traget attribute.
	 */
	public T getTargetValue();
}
