package ai.libs.jaicore.ml.weka.rangequery;

import java.util.ArrayList;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

public class AugSpaceAllPairs implements IAugSpaceSamplingFunction {

	@Override
	public Instances apply(final Instances input) {
		int nPrecise = input.numInstances();
		ArrayList<Attribute> augAttrs = new ArrayList<>(input.numAttributes() * 2);
		for (int attr = 0; attr < input.numAttributes() - 1; attr++) {
			augAttrs.add(new Attribute("x" + attr + "_lower"));
			augAttrs.add(new Attribute("x" + attr + "_upper"));
		}
		augAttrs.add(new Attribute("y_min"));
		augAttrs.add(new Attribute("y_max"));
		int nAllPairs = (nPrecise * (nPrecise - 1)) / 2;
		Instances augInstances = new Instances("aug_space_train", augAttrs, nAllPairs);

		for (int i = 0; i < nPrecise; i++) {
			for (int j = 0; j < nPrecise; j++) {
				ArrayList<Instance> sampledPoints = new ArrayList<>();

				Instance x1 = input.get(i);
				Instance x2 = input.get(j);

				// Assume last attribute is the class
				int numFeatures = input.numAttributes() - 1;

				for (Instance inst : input) {
					boolean inInterval = true;
					for (int att = 0; att < numFeatures && inInterval; att++) {
						if (inst.value(att) < Math.min(x1.value(att), x2.value(att)) || inst.value(att) > Math.max(x1.value(att), x2.value(att))) {
							inInterval = false;
						}
					}
					if (inInterval) {
						sampledPoints.add(inst);
					}
				}
				augInstances.add(AbstractAugmentedSpaceSampler.generateAugPoint(sampledPoints));
			}
		}

		return augInstances;
	}

}
