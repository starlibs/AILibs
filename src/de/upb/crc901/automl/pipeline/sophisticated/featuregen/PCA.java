package de.upb.crc901.automl.pipeline.sophisticated.featuregen;

import de.upb.crc901.automl.pipeline.basic.SuvervisedFilterPreprocessor;
import weka.attributeSelection.PrincipalComponents;
import weka.attributeSelection.Ranker;

public class PCA extends SuvervisedFilterPreprocessor implements FeatureGenerator {

	public PCA() {
		super(new Ranker(), new PrincipalComponents());
	}

}
