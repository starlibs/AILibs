package jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces;

import java.util.Collection;

import jaicore.search.algorithms.parallel.parallelexploration.distributed.DistributedComputationResult;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.model.travesaltree.Node;

public interface DistributedSearchCommunicationLayer<T,A,V extends Comparable<V>> {
	
	/* infrastructural operations */
	public void close();
	
	/* master operations */
	public void init();
	public Collection<String> detectNewCoworkers();
	public void createNewJobForCoworker(String coworker, Collection<Node<T,V>> nodes);
	public void attachCoworker(String coworker);
	public void detachCoworker(String coworker);
	public DistributedComputationResult<T, V> readResult(String coworker);
	public void setGraphGenerator(SerializableGraphGenerator<T, A> generator) throws Exception;
	public void setNodeEvaluator(SerializableNodeEvaluator<T, V> evaluator) throws Exception;
	
	/* coworker operations */
	public void register(String coworker) throws InterruptedException; // registers the coworker on the bus and blocks him until it becomes attached
	public void unregister(String coworker);
	public boolean isAttached(String coworker);
	public Collection<Node<T,V>> nextJob(String coworker) throws InterruptedException;
	public SerializableGraphGenerator<T,A> getGraphGenerator() throws Exception;
	public INodeEvaluator<T,V> getNodeEvaluator() throws Exception;
	public void reportResult(String coworker, DistributedComputationResult<T, V> results);
}
