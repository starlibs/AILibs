package jaicore.experiments;

/**
 * An IExperimentKeyGenerator generates and validates values for a computed key field.
 * 
 * 
 * @author fmohr
 *
 * @param <T>
 */
public interface IExperimentKeyGenerator<T> {

	/**
	 * @return The cardinality of the set of values that may be assigned to this key field
	 */
	public int getNumberOfValues();

	/**
	 * Deterministically computes the i-th value in the (totally ordered) set of values for this key 
	 * 
	 * @param i The index for the value
	 * @return
	 */
	public T getValue(int i);

	/**
	 * Tries to cast the given String to an object of the value domain and checks whether any entry in the set corresponds to it.  
	 * 
	 * @param value
	 * @return True iff there is one entity in the value set whose toString method evaluates to the given string.
	 */
	public boolean isValueValid(String value);
}
