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
 * @param item to be checked
 * @return 
 */
	abstract public boolean contains(Object item);

}
