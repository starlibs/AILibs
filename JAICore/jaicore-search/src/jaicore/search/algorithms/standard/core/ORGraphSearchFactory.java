package jaicore.search.algorithms.standard.core;

import jaicore.search.algorithms.interfaces.IObservableORGraphSearch;
import jaicore.search.algorithms.interfaces.IObservableORGraphSearchFactory;
import jaicore.search.structure.core.GraphGenerator;

public class ORGraphSearchFactory<T, A, V extends Comparable<V>> implements IObservableORGraphSearchFactory<T, A, V> {

	protected int timeoutForFInMS;
	protected INodeEvaluator<T, V> timeoutEvaluator;
	protected String loggerName;

	public ORGraphSearchFactory() {
		super();
	}

	public ORGraphSearchFactory(final int timeoutForFInMS) {
		this();
		if (timeoutForFInMS > 0) {
			this.timeoutForFInMS = timeoutForFInMS;
		}
	}

	@Override
	public IObservableORGraphSearch<T, A, V> createSearch(final GraphGenerator<T, A> graphGenerator, final INodeEvaluator<T, V> nodeEvaluator, final int numberOfCPUs) {
		ORGraphSearch<T, A, V> search = new ORGraphSearch<>(graphGenerator, nodeEvaluator);
		search.parallelizeNodeExpansion(numberOfCPUs);
		search.setTimeoutForComputationOfF(this.timeoutForFInMS, this.timeoutEvaluator);
		if (loggerName != null && loggerName.length() > 0)
			search.setLoggerName(loggerName);
		return search;
	}

	public void setTimeoutForFComputation(final int timeoutInMS, final INodeEvaluator<T, V> timeoutEvaluator) {
		this.timeoutForFInMS = timeoutInMS;
		this.timeoutEvaluator = timeoutEvaluator;
	}

	public int getTimeoutForFInMS() {
		return this.timeoutForFInMS;
	}

	public INodeEvaluator<T, V> getTimeoutEvaluator() {
		return this.timeoutEvaluator;
	}

	public String getLoggerName() {
		return loggerName;
	}

	public void setLoggerName(String loggerName) {
		this.loggerName = loggerName;
	}
}
