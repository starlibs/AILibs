package jaicore.ml.rqp;

import weka.core.Instance;

/**
 * Interface representing a class that samples interval-valued data from a set of precise data points. 
 * @author Michael
 *
 */
public interface IAugmentedSpaceSampler {
	public Instance augSpaceSample();
}
