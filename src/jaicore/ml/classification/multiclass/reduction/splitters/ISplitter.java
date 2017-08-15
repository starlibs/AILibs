package jaicore.ml.classification.multiclass.reduction.splitters;

import java.util.Collection;

import weka.classifiers.Classifier;

public interface ISplitter {
	 public Collection<Collection<String>> split(Collection<String> set, Classifier c);
}
