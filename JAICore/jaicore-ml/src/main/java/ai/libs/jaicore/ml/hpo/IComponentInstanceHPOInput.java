package ai.libs.jaicore.ml.hpo;

import org.api4.java.common.attributedobjects.IObjectEvaluator;

import ai.libs.jaicore.components.api.IComponentInstance;

public interface IComponentInstanceHPOInput {

	public IComponentInstance getComponentInstanceToOptimize();

	public IObjectEvaluator<IComponentInstance, Double> getEvaluator();

}
