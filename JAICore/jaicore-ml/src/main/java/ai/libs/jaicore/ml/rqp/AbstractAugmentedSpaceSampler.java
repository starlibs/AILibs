package ai.libs.jaicore.ml.rqp;

import java.util.List;
import java.util.Random;

import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public abstract class AbstractAugmentedSpaceSampler implements IAugmentedSpaceSampler {

	private final Instances preciseInsts;
	private final Random rng;

	public AbstractAugmentedSpaceSampler(final Instances preciseInsts, final Random rng) {
		super();
		this.preciseInsts = preciseInsts;
		this.rng = rng;
	}

	/*
	 * Generates a point in the augmented space from a given list of precise data points,
	 * i.e. chooses the respective minimum and maximum from the given points as lower and upper bounds for each attribute.
	 */
	protected static Instance generateAugPoint(final List<Instance> insts) {
		if(insts.isEmpty()) {
			throw new IllegalArgumentException("Cannot generate augmented point from an empty list.");
		}
		int numAttributes = insts.get(0).numAttributes();
		Instance augPoint = new DenseInstance(numAttributes * 2);

		for (int i = 0; i < numAttributes; i++) {
			double lowerBound = Double.POSITIVE_INFINITY;
			double upperBound = Double.NEGATIVE_INFINITY;

			for (Instance inst : insts) {
				double attrValue = inst.value(i);
				lowerBound = Math.min(lowerBound, attrValue);
				upperBound = Math.max(upperBound, attrValue);
			}

			augPoint.setValue(2 * i, lowerBound);
			augPoint.setValue((2 * i) + 1, upperBound);
		}

		return augPoint;
	}

	/**
	 * @return the preciseInsts
	 */
	public Instances getPreciseInsts() {
		return this.preciseInsts;
	}

	/**
	 * @return the rng
	 */
	public Random getRng() {
		return this.rng;
	}

}
