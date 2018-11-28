package hasco.optimizingfactory;

import hasco.core.SoftwareConfigurationProblem;
import hasco.model.EvaluatedSoftwareConfigurationSolution;
import jaicore.basic.algorithm.AOptimizer;

public abstract class SoftwareConfigurationAlgorithm<P extends SoftwareConfigurationProblem<V>, O, C extends EvaluatedSoftwareConfigurationSolution<V>, V extends Comparable<V>>
		extends AOptimizer<P, O, C, V> {

	public SoftwareConfigurationAlgorithm() {
		super();
	}

	public SoftwareConfigurationAlgorithm(P input) {
		super(input);
	}

}
