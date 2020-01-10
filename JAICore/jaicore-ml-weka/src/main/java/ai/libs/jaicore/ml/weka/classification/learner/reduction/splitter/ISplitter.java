package ai.libs.jaicore.ml.weka.classification.learner.reduction.splitter;

import java.util.Collection;

import org.api4.java.ai.ml.core.dataset.splitter.SplitFailedException;

import weka.core.Instances;

public interface ISplitter {
	public Collection<Collection<String>> split(Instances data) throws SplitFailedException, InterruptedException;
}
