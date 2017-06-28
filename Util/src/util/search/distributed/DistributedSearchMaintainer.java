package util.search.distributed;

import java.util.Collection;

import util.search.core.Node;

public interface DistributedSearchMaintainer<T,V extends Comparable<V>> {
	
	/* master operations */
	public Collection<String> detectNewCoworkers();
	public void createNewJobForCoworker(String coworker, Collection<Node<T,V>> nodes);
	public void attachCoworker(String coworker);
	public void detachCoworker(String coworker);
	public DistributedComputationResult<T, V> readResult(String coworker);
	
	/* coworker operations */
	public void register(String coworker);
	public void unregister(String coworker);
	public boolean isAttached(String coworker);
	public boolean hasNewJob(String coworker);
	public Collection<Node<T,V>> getJobDescription(String coworker);
	public void reportResult(String coworker, DistributedComputationResult<T, V> results);
}
