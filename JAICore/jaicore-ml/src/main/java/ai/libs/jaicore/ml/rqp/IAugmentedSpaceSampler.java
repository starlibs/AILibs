package ai.libs.jaicore.ml.rqp;

import weka.core.Instance;

/**
 * Interface representing a class that samples interval-valued data from a set of precise data points.
 * @author Michael
 *
 */
public interface IAugmentedSpaceSampler {

	/**
	 * Generates a point in the augmented space from the AugmentedSpaceSampler's precise dataset.
	 *
	 * @return	A point in the augmented space consisting of upper and lower bounds for each attribute, including the target.
	 */
	public Instance augSpaceSample();
}
