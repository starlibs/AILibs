package jaicore.basic;

public interface IBinaryAggregator<V> {
	public V aggregate(V a, V b);
}
