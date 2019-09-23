package ai.libs.jaicore.ml.weka.learner.reduction.splitter;

import java.util.Collection;

import weka.core.Instances;

public interface ISplitter {
	 public Collection<Collection<String>> split(Instances data) throws Exception;
}
