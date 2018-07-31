package jaicore.ml.intervaltree;

import java.util.HashSet;
import java.util.Set;

import jaicore.ml.core.FeatureSpace;
import weka.classifiers.Classifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;

public class ExtendedRandomForest extends RandomForest {

	private static final long serialVersionUID = -4893299561817337649L;
	private FeatureSpace featureSpace;
	
	public ExtendedRandomForest() {
		super();
		ExtendedRandomTree erTree = new ExtendedRandomTree();
		erTree.setMinNum(25);
		this.setClassifier(erTree);
	}
	
	@Override
	public void buildClassifier(Instances data) throws Exception {
		super.buildClassifier(data);
		
	}
	
	public ExtendedRandomForest(FeatureSpace featureSpace) {
		super();
		ExtendedRandomTree erTree = new ExtendedRandomTree();
		this.featureSpace = featureSpace;
		erTree.setFeatureSpace(featureSpace);
		this.setClassifier(erTree);
	}
	
	public void prepareForest(Instances data) {
		this.featureSpace = new FeatureSpace(data);
		for(Classifier classifier : m_Classifiers) {
			ExtendedRandomTree curTree = (ExtendedRandomTree) classifier;
			curTree.setFeatureSpace(this.featureSpace);
			curTree.preprocess();
		}
			
	}
	
	public void printVariances() {
		for(Classifier classifier : m_Classifiers) {
			ExtendedRandomTree curTree = (ExtendedRandomTree) classifier;
			System.out.println("cur var: " + curTree.getTotalVariance());
		}
	}
	
	public double computeMarginalForFeatureSubset(Set<Integer> features) {
		double avg = 0;
		for(Classifier classifier : m_Classifiers) {
			ExtendedRandomTree curTree = (ExtendedRandomTree) classifier;
			double curMarg = curTree.computeMarginalVarianceContributionForSubsetOfFeatures(features);
			avg += curMarg * 1.0/m_Classifiers.length;
		}
		return avg;
	}
	
	public int getSize() {
		return m_Classifiers.length;
	}
	
	public FeatureSpace getFeatureSpace() {
		return this.featureSpace;
	}

}
