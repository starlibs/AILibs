package jaicore.ml.core;

/**
 * Abstract description of a feature domain. Needed for fANOVA application in the {@link ExtendedRandomTree}.
 * 
 * @author jmhansel
 *
 */
public abstract class FeatureDomain {
	
/**
 * Checks if the domain contains an item.
 * 
 * @param Item to be checked
 * @return 
 */
	abstract public boolean contains(Object item);
	
	/**
	 * Computes the size of the domain. For categorical features it returns the number of catogeries,
	 * for numeric features upper interval bound - lower interval bound.
	 * @return Size of feature domain
	 */
	abstract double getRangeSize();

}
