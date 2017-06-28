package util.search.core;

public interface GraphDependentNodeEvaluator<T, A, V extends Comparable<V>> extends NodeEvaluator<T, V> {
	public void setGenerator(GraphGenerator<T, A> generator);
}
