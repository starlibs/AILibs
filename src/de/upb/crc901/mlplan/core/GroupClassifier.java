package de.upb.crc901.mlplan.core;

import weka.core.Instances;

public interface GroupClassifier {
	
	public double[] classifyInstances(Instances instances);
}
