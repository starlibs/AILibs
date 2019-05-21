package jaicore.ml.rqp;

import java.util.ArrayList;
import java.util.Random;

import weka.core.Instance;
import weka.core.Instances;

/**
 * Samples interval-valued data from a dataset of precise points by sampling k precise points (with replacement)
 * and generating a point in the interval-valued augmented space by only considering those k points, i.e. choosing
 * respective minima and maxima for each attribute from the chosen precise points.
 * @author Michael
 *
 */
public class ChooseKAugSpaceSampler extends AbstractAugmentedSpaceSampler {
	
	private int k;
	
	public ChooseKAugSpaceSampler(Instances preciseInsts, Random rng, int k) {
		super(preciseInsts, rng);
		this.k = k;
	}

	@Override
	public Instance augSpaceSample() {
		Instances preciseInsts = this.getPreciseInsts();
		ArrayList<Instance> sampledPoints = new ArrayList(k);
		for (int i = 0; i < k; i++) {
			sampledPoints.add(preciseInsts.get(
					this.getRng().nextInt(preciseInsts.size())));
		}
		return generateAugPoint(sampledPoints);
	}

	/**
	 * @return the k
	 */
	public int getK() {
		return k;
	}

	/**
	 * @param k the k to set
	 */
	public void setK(int k) {
		this.k = k;
	}

}
