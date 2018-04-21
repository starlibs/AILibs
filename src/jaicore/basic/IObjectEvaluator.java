package jaicore.basic;

public interface IObjectEvaluator<T,V extends Comparable<V>> {
	public V evaluate(T object) throws Exception;
}
