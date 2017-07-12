package jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces;

import java.util.Collection;

import jaicore.search.algorithms.parallel.parallelexploration.distributed.DistributedComputationResult;
import jaicore.search.structure.core.Node;

public interface DistributionSearchResultProcessor<T, V extends Comparable<V>> {
	public void processResult(Collection<Node<T, V>> job, DistributedComputationResult<T, V> result);
}
