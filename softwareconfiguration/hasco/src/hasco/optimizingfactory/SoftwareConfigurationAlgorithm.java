package hasco.optimizingfactory;

import hasco.core.SoftwareConfigurationProblem;
import hasco.model.ComponentInstance;
import jaicore.basic.algorithm.IOptimizationAlgorithm;

public interface SoftwareConfigurationAlgorithm<P extends SoftwareConfigurationProblem<V>, O, V extends Comparable<V>> extends IOptimizationAlgorithm<P, O, ComponentInstance, V> {

}
