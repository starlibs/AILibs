package jaicore.ml.intervaltree;

import java.util.HashSet;

import jaicore.ml.core.FeatureSpace;
import weka.classifiers.Classifier;
import weka.classifiers.trees.RandomForest;

public class ExtendedRandomForest extends RandomForest {

	private static final long serialVersionUID = -4893299561817337649L;
	private FeatureSpace featureSpace;
	
	public ExtendedRandomForest() {
		super();
		ExtendedRandomTree erTree = new ExtendedRandomTree();
		this.setClassifier(erTree);
	}
	
	public ExtendedRandomForest(FeatureSpace featureSpace) {
		super();
		ExtendedRandomTree erTree = new ExtendedRandomTree();
		this.featureSpace = featureSpace;
		erTree.setFeatureSpace(featureSpace);
		this.setClassifier(erTree);
	}
	
	public void prepareForest() {
		for(Classifier classifier : m_Classifiers) {
			ExtendedRandomTree curTree = (ExtendedRandomTree) classifier;
			curTree.preprocess();
		}
			
	}
	
	public void printVariances() {
		for(Classifier classifier : m_Classifiers) {
			ExtendedRandomTree curTree = (ExtendedRandomTree) classifier;
			System.out.println("cur var: " + curTree.computeTotalVariance());
		}
	}
	
	public double computeMarginalForFeatureSubset(HashSet<Integer> features) {
		double avg = 0;
		for(Classifier classifier : m_Classifiers) {
			ExtendedRandomTree curTree = (ExtendedRandomTree) classifier;
			double curMarg = curTree.computeMarginalForSubsetOfFeatures(features);
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
