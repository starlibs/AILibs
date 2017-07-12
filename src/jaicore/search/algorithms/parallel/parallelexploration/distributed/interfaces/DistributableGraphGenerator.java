package jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces;

public interface DistributableGraphGenerator<T, A> extends SerializableGraphGenerator<T, A> {
	
	public void setRootGenerator(SerializableRootGenerator<T> generator);
}
