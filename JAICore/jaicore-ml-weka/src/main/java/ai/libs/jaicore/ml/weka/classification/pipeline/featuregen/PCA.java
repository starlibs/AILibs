package ai.libs.jaicore.ml.weka.classification.pipeline.featuregen;

import ai.libs.jaicore.ml.weka.classification.pipeline.SuvervisedFilterPreprocessor;
import weka.attributeSelection.PrincipalComponents;
import weka.attributeSelection.Ranker;

@SuppressWarnings("serial")
public class PCA extends SuvervisedFilterPreprocessor implements FeatureGenerator {

	public PCA() {
		super(new Ranker(), new PrincipalComponents());
	}

}
