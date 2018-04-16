package hasco.query;

public interface Benchmark<T,V extends Comparable<V>> {
	public V getScore(T candidate) throws Exception;
}
