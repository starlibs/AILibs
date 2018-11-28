package hasco.optimizingfactory;

import hasco.core.SoftwareConfigurationProblem;
import hasco.model.EvaluatedSoftwareConfigurationSolution;
import jaicore.basic.algorithm.IAlgorithmFactory;

public interface SoftwareConfigurationAlgorithmFactory<P extends SoftwareConfigurationProblem<V>, O, C extends EvaluatedSoftwareConfigurationSolution<V>, V extends Comparable<V>> extends IAlgorithmFactory<P, O> {

	@Override
	public SoftwareConfigurationAlgorithm<P, O, C, V> getAlgorithm();
}
