package ai.libs.hasco.optimizingfactory;

import org.api4.java.algorithm.IAlgorithmFactory;

import ai.libs.hasco.core.SoftwareConfigurationProblem;
import ai.libs.hasco.model.EvaluatedSoftwareConfigurationSolution;

public interface SoftwareConfigurationAlgorithmFactory<P extends SoftwareConfigurationProblem<V>, O extends EvaluatedSoftwareConfigurationSolution<V>, V extends Comparable<V>, A extends SoftwareConfigurationAlgorithm<P, O, V>> extends IAlgorithmFactory<P, O, A> {

}
