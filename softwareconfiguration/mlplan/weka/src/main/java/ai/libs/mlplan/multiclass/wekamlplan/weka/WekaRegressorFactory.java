package ai.libs.mlplan.multiclass.wekamlplan.weka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.exceptions.ComponentInstantiationFailedException;
import ai.libs.jaicore.ml.weka.classification.learner.IWekaClassifier;
import ai.libs.jaicore.ml.weka.regression.learner.WekaRegressor;
import ai.libs.mlplan.core.ILearnerFactory;

public class WekaRegressorFactory implements ILearnerFactory<IWekaClassifier> {

	private Logger logger = LoggerFactory.getLogger(WekaRegressorFactory.class);
	private final WekaPipelineFactory classifierFactory = new WekaPipelineFactory();

	@Override
	public IWekaClassifier getComponentInstantiation(final IComponentInstance groundComponent) throws ComponentInstantiationFailedException {
		this.logger.debug("Instantiate weka regressor from component instance {}.", groundComponent);
		return new WekaRegressor(this.classifierFactory.getComponentInstantiation(groundComponent).getClassifier());
	}

}
