package ai.libs.mlplan.multiclass.wekamlplan.sophisticated.featuregen;

import ai.libs.mlplan.multiclass.wekamlplan.weka.model.SuvervisedFilterPreprocessor;
import weka.attributeSelection.PrincipalComponents;
import weka.attributeSelection.Ranker;

@SuppressWarnings("serial")
public class PCA extends SuvervisedFilterPreprocessor implements FeatureGenerator {

	public PCA() {
		super(new Ranker(), new PrincipalComponents());
	}

}
