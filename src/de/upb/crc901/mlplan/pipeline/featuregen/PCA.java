package de.upb.crc901.mlplan.pipeline.featuregen;

import de.upb.crc901.mlplan.core.SuvervisedFilterPreprocessor;
import weka.attributeSelection.PrincipalComponents;
import weka.attributeSelection.Ranker;

public class PCA extends SuvervisedFilterPreprocessor implements FeatureGenerator {

	public PCA() {
		super(new Ranker(), new PrincipalComponents());
	}

}
