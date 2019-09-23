package ai.libs.jaicore.ml.weka.learner.reduction.splitter;

public interface ISplitterFactory<T extends ISplitter> {
	public T getSplitter(int randomSeed);
}
