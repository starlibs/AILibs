package jaicore.ml.classification.multiclass.reduction.splitters;

public interface ISplitterFactory<T extends ISplitter> {
	public T getSplitter(int randomSeed);
}
