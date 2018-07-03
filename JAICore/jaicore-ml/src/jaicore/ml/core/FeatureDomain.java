package jaicore.ml.core;

import java.io.Serializable;

import weka.core.Instance;

/**
 * Abstract description of a feature domain. Needed for fANOVA application in
 * the {@link ExtendedRandomTree}.
 * 
 * @author jmhansel
 *
 */
public abstract class FeatureDomain implements Serializable {

	/**
		 * 
		 */
	private static final long serialVersionUID = -2314884533144162940L;

	/**
	 * Checks if the domain contains an item.
	 * 
	 * @param Item
	 *            to be checked
	 * @return
	 */
	abstract public boolean contains(Object item);

	/**
	 * Computes the size of the domain. For categorical features it returns the
	 * number of catogeries, for numeric features upper interval bound - lower
	 * interval bound.
	 * 
	 * @return Size of feature domain
	 */
	abstract public double getRangeSize();

	/**
	 * Checks whether a given weka instance is contained in the feature domain
	 * 
	 * @param instance
	 * @return true iff contained in the domain
	 */
	abstract public boolean containsInstance(double value);

}
