package de.upb.crc901.automl;

import java.util.List;

import jaicore.basic.algorithm.AAlgorithmTestProblemSet;
import weka.core.Instances;

public abstract class MLProblemSet extends AAlgorithmTestProblemSet<Instances> {

	public MLProblemSet(String name) {
		super("ML task " + name);
	}
	
	public abstract List<Instances> getProblems();
}
