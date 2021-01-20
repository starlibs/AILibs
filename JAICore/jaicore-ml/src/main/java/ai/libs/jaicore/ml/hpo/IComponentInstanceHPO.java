package ai.libs.jaicore.ml.hpo;

import org.api4.java.algorithm.IAlgorithm;

import ai.libs.jaicore.components.api.IEvaluatedSoftwareConfigurationSolution;
import ai.libs.jaicore.ml.hpo.ga.IComponentInstanceHPOGAInput;

public interface IComponentInstanceHPO<V extends Comparable<V>> extends IAlgorithm<IComponentInstanceHPOGAInput, IEvaluatedSoftwareConfigurationSolution<V>> {

}
