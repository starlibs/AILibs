package jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces;

import java.util.Collection;

import jaicore.search.algorithms.parallel.parallelexploration.distributed.DistributedComputationResult;
import jaicore.search.model.travesaltree.Node;

public interface DistributionSearchAdapter<T, V extends Comparable<V>> {
	public Collection<Node<T,V>> nextJob();
	public void processResult(Collection<Node<T, V>> job, DistributedComputationResult<T, V> result);
}
