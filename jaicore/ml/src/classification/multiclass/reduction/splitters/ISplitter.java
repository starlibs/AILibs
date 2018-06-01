package jaicore.ml.classification.multiclass.reduction.splitters;

import java.util.Collection;

import weka.core.Instances;

public interface ISplitter {
	 public Collection<Collection<String>> split(Instances data) throws Exception;
}
