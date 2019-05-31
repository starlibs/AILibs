package hasco.optimizingfactory;

import ai.libs.jaicore.basic.algorithm.IAlgorithmFactory;
import hasco.core.SoftwareConfigurationProblem;
import hasco.model.EvaluatedSoftwareConfigurationSolution;

public interface SoftwareConfigurationAlgorithmFactory<P extends SoftwareConfigurationProblem<V>, O extends EvaluatedSoftwareConfigurationSolution<V>, V extends Comparable<V>> extends IAlgorithmFactory<P, O> {

	@Override
	public SoftwareConfigurationAlgorithm<P, O, V> getAlgorithm();

	@Override
	public SoftwareConfigurationAlgorithm<P, O, V> getAlgorithm(P problem);
}
