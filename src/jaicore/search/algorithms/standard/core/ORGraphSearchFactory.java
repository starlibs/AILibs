package jaicore.search.algorithms.standard.core;

import jaicore.search.algorithms.interfaces.IObservableORGraphSearch;
import jaicore.search.algorithms.interfaces.IObservableORGraphSearchFactory;
import jaicore.search.structure.core.GraphGenerator;

public class ORGraphSearchFactory<T,A, V extends Comparable<V>> implements IObservableORGraphSearchFactory<T, A, V>{

	private int timeoutForFInMS;
	private INodeEvaluator<T,V> timeoutEvaluator;
	
	@Override
	public IObservableORGraphSearch<T, A, V> createSearch(GraphGenerator<T, A> graphGenerator, INodeEvaluator<T, V> nodeEvaluator, int numberOfCPUs) {
		ORGraphSearch<T, A, V> search = new ORGraphSearch<>(graphGenerator, nodeEvaluator);
		search.parallelizeNodeExpansion(numberOfCPUs);
		search.setTimeoutForComputationOfF(timeoutForFInMS, timeoutEvaluator);
		return search;
	}

	public void setTimeoutForFComputation(int timeoutInMS, INodeEvaluator<T, V> timeoutEvaluator) {
		this.timeoutForFInMS = timeoutInMS;
		this.timeoutEvaluator = timeoutEvaluator;
	}

	public int getTimeoutForFInMS() {
		return timeoutForFInMS;
	}

	public INodeEvaluator<T, V> getTimeoutEvaluator() {
		return timeoutEvaluator;
	}
}
