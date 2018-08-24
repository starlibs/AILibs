package jaicore.search.algorithms.standard.bestfirst;

import jaicore.search.algorithms.interfaces.IObservableORGraphSearch;
import jaicore.search.algorithms.interfaces.IObservableORGraphSearchFactory;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.structure.core.GraphGenerator;

public class BestFirstFactory<T, A, V extends Comparable<V>> implements IObservableORGraphSearchFactory<T, A, V> {

	protected int timeoutForFInMS;
	protected INodeEvaluator<T, V> timeoutEvaluator;
	protected String loggerName;

	public BestFirstFactory() {
		super();
	}

	public BestFirstFactory(final int timeoutForFInMS) {
		this();
		if (timeoutForFInMS > 0) {
			this.timeoutForFInMS = timeoutForFInMS;
		}
	}

	@Override
	public IObservableORGraphSearch<T, A, V> createSearch(final GraphGenerator<T, A> graphGenerator, final INodeEvaluator<T, V> nodeEvaluator, final int numberOfCPUs) {
		BestFirst<T, A, V> search = new BestFirst<>(graphGenerator, nodeEvaluator);
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
