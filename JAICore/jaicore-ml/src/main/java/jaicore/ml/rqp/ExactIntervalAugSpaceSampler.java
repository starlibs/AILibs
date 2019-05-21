package jaicore.ml.rqp;

import java.util.ArrayList;
import java.util.Random;

import weka.core.Instance;
import weka.core.Instances;

/**
 * Samples interval-valued data from a dataset of precise points.
 * First chooses two precise points of random to define an interval.
 * Then finds all other points that lie in this interval to generate the interval-valued point in the augmented space.
 * May be very inefficient for even medium-sized datasets.
 * @author Michael
 *
 */
public class ExactIntervalAugSpaceSampler extends AbstractAugmentedSpaceSampler {

	public ExactIntervalAugSpaceSampler(Instances preciseInsts, Random rng) {
		super(preciseInsts, rng);
	}

	@Override
	public Instance augSpaceSample() {
		Instances preciseInsts = this.getPreciseInsts();
		int numInsts = preciseInsts.size();
		ArrayList<Instance> sampledPoints = new ArrayList<Instance>();
		
		Instance x1 = preciseInsts.get(this.getRng().nextInt(numInsts));
		Instance x2 = preciseInsts.get(this.getRng().nextInt(numInsts));
		
		// Assume last attribute is the class
		int numFeatures = preciseInsts.numAttributes() - 1;
		
		for (Instance inst : preciseInsts) {
			boolean inInterval = true;
			for (int att = 0; att < numFeatures && inInterval == true; att++) {
				if (inst.value(att) < Math.min(x1.value(att), x2.value(att)) 
				 || inst.value(att) > Math.max(x1.value(att), x2.value(att))) {
					inInterval = false;
				}
			}
			if (inInterval) {
				sampledPoints.add(inst);
			}
		}
		
		return generateAugPoint(sampledPoints);
	}

}
