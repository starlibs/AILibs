package ai.libs.softwareconfiguration.optimizingfactory;

import org.api4.java.algorithm.IAlgorithmFactory;

import ai.libs.softwareconfiguration.model.EvaluatedSoftwareConfigurationSolution;
import ai.libs.softwareconfiguration.model.SoftwareConfigurationProblem;

public interface SoftwareConfigurationAlgorithmFactory<P extends SoftwareConfigurationProblem<V>, O extends EvaluatedSoftwareConfigurationSolution<V>, V extends Comparable<V>, A extends SoftwareConfigurationAlgorithm<P, O, V>> extends IAlgorithmFactory<P, O, A> {

}
