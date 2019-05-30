package jaicore.ml.core.dataset;

public interface IModifiableInstance {

	/**
	 * Removes an attribute value for the given position.
	 *
	 * @param position The position of the attribute within the instance.
	 * @return True, if the removal was successful. False, otherwise.
	 */
	public boolean removeAttributeValue(int position);
}
