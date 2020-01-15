package ai.libs.jaicore.ml.weka.classification.learner.reduction.splitter;

public interface ISplitterFactory<T extends ISplitter> {
	public T getSplitter(int randomSeed);
}
