package ai.libs.jaicore.components.optimizingfactory;

import org.api4.java.algorithm.IAlgorithmFactory;

import ai.libs.jaicore.components.api.IEvaluatedSoftwareConfigurationSolution;
import ai.libs.jaicore.components.model.SoftwareConfigurationProblem;

public interface SoftwareConfigurationAlgorithmFactory<P extends SoftwareConfigurationProblem<V>, O extends IEvaluatedSoftwareConfigurationSolution<V>, V extends Comparable<V>, A extends SoftwareConfigurationAlgorithm<P, O, V>> extends IAlgorithmFactory<P, O, A> {

}
