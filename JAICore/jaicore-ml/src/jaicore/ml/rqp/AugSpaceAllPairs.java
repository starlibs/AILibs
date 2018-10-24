package jaicore.ml.rqp;

import java.util.ArrayList;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

public class AugSpaceAllPairs implements IAugSpaceSamplingFunction {

	@Override
	public Instances apply(Instances input) {
		int n_precise = input.numInstances();
		ArrayList<Attribute> aug_attrs = new ArrayList<Attribute>(input.numAttributes()*2);
		for (int attr = 0; attr < input.numAttributes() - 1; attr++) {
			aug_attrs.add(new Attribute("x" + attr + "_lower"));
			aug_attrs.add(new Attribute("x" + attr + "_upper"));
		}
		aug_attrs.add(new Attribute("y_min"));
		aug_attrs.add(new Attribute("y_max"));
		int n_all_pairs = ( n_precise * (n_precise - 1) ) / 2;
		Instances aug_instances = new Instances("aug_space_train", aug_attrs, n_all_pairs);
		
		for (int i = 0; i < n_precise; i++) {
			for (int j = 0; j < n_precise; j++) {
				ArrayList<Instance> sampledPoints = new ArrayList<Instance>();
				
				Instance x1 = input.get(i);
				Instance x2 = input.get(j);
				
				// Assume last attribute is the class
				int numFeatures = input.numAttributes() - 1;
				
				for (Instance inst : input) {
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
				aug_instances.add(AbstractAugmentedSpaceSampler.generateAugPoint(sampledPoints));
			}
		}
		
		return aug_instances;
	}

}
