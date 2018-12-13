package jaicore.ml.core.dataset;

import org.apache.commons.math3.ml.clustering.Clusterable;

/**
 * Interface of an instance which consists of attributes, a target value and is {@link Clusterable}.
 *
 * @author jnowack
 */
public interface IClusterableInstance extends IInstance, Clusterable{
	
}
