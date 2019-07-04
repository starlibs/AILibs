package ai.libs.hasco.optimizingfactory;

import ai.libs.hasco.core.SoftwareConfigurationProblem;
import ai.libs.hasco.model.EvaluatedSoftwareConfigurationSolution;
import ai.libs.jaicore.basic.algorithm.IAlgorithmFactory;

public interface SoftwareConfigurationAlgorithmFactory<P extends SoftwareConfigurationProblem<V>, O extends EvaluatedSoftwareConfigurationSolution<V>, V extends Comparable<V>> extends IAlgorithmFactory<P, O> {

	@Override
	public SoftwareConfigurationAlgorithm<P, O, V> getAlgorithm();

	@Override
	public SoftwareConfigurationAlgorithm<P, O, V> getAlgorithm(P problem);
}
