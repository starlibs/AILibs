package hasco.optimizingfactory;

import hasco.core.SoftwareConfigurationProblem;
import jaicore.basic.algorithm.IAlgorithmFactory;

public interface SoftwareConfigurationAlgorithmFactory<P extends SoftwareConfigurationProblem<V>, O, V extends Comparable<V>> extends IAlgorithmFactory<P, O> {

	@Override
	public SoftwareConfigurationAlgorithm<P, O, V> getAlgorithm();
}
