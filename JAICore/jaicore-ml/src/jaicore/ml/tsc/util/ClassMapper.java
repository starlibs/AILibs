package jaicore.ml.tsc.util;

import java.util.List;

/**
 * Class mapper used for predictions of String objects which are internally
 * predicted by time series classifiers as ints.
 * 
 * @author Julian Lienen
 *
 */
public class ClassMapper {
	/**
	 * Stored class values which indices are used to map integers to strings.
	 */
	private List<String> classValues;

	/**
	 * Constructor using a list of String value to realize the mapping
	 * 
	 * @param classValues
	 *            String values used for the mapping. The values are identified by
	 *            the given indices in the given list
	 */
	public ClassMapper(final List<String> classValues) {
		this.classValues = classValues;
	}

	/**
	 * Maps a String value to an integer value based on the <code>value</code>'s
	 * position in the <code>classValues</code>.
	 * 
	 * @param value
	 *            The value to be looked up
	 * @return Returns the mapped index or -1 if not stored
	 */
	public int map(final String value) {
		return this.classValues.indexOf(value);
	}

	/**
	 * Maps an integer value to a string based on the position <code>index</code> in
	 * the <code>classValues</code>.
	 * 
	 * @param index
	 *            The index used for the lookup
	 * @return Returns the given string at the position <code>index</code>
	 */
	public String map(final int index) {
		return this.classValues.get(index);
	}

	/**
	 * Getter for the <code>classValues</code>.
	 * 
	 * @return Returns the stored class values
	 */
	public List<String> getClassValues() {
		return classValues;
	}

	/**
	 * Setter for the <code>classValues</code>.
	 * 
	 * @param classValues
	 *            The class values to be set.
	 */
	public void setClassValues(final List<String> classValues) {
		this.classValues = classValues;
	}
}
