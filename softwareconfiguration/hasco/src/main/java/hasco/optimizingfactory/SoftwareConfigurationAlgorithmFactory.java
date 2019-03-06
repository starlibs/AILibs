package hasco.optimizingfactory;

import hasco.core.SoftwareConfigurationProblem;
import hasco.model.EvaluatedSoftwareConfigurationSolution;
import jaicore.basic.algorithm.IAlgorithmFactory;

public interface SoftwareConfigurationAlgorithmFactory<P extends SoftwareConfigurationProblem<V>, O extends EvaluatedSoftwareConfigurationSolution<V>, V extends Comparable<V>> extends IAlgorithmFactory<P, O> {

	@Override
	public SoftwareConfigurationAlgorithm<P, O, V> getAlgorithm();

	@Override
	public SoftwareConfigurationAlgorithm<P, O, V> getAlgorithm(P problem);
}
