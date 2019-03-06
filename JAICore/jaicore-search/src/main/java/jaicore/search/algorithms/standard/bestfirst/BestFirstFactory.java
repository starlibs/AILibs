package jaicore.search.algorithms.standard.bestfirst;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.core.interfaces.IOptimalPathInORGraphSearchFactory;
import jaicore.search.core.interfaces.StandardORGraphSearchFactory;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public class BestFirstFactory<P extends GraphSearchWithSubpathEvaluationsInput<N, A, V>, N, A, V extends Comparable<V>> extends StandardORGraphSearchFactory<P, EvaluatedSearchGraphPath<N, A, V>, N, A, V>
implements IOptimalPathInORGraphSearchFactory<P, N, A, V> {

	private int timeoutForFInMS;
	private INodeEvaluator<N, V> timeoutEvaluator;
	private Logger logger = LoggerFactory.getLogger(BestFirstFactory.class);

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
	public BestFirst<P, N, A, V> getAlgorithm() {
		if (this.getInput().getGraphGenerator() == null) {
			throw new IllegalStateException("Cannot produce BestFirst searches before the graph generator is set in the problem.");
		}
		if (this.getInput().getNodeEvaluator() == null) {
			throw new IllegalStateException("Cannot produce BestFirst searches before the node evaluator is set.");
		}
		return this.getAlgorithm(this.getInput());
	}

	public void setTimeoutForFComputation(final int timeoutInMS, final INodeEvaluator<N, V> timeoutEvaluator) {
		this.timeoutForFInMS = timeoutInMS;
		this.timeoutEvaluator = timeoutEvaluator;
	}

	public int getTimeoutForFInMS() {
		return this.timeoutForFInMS;
	}

	public INodeEvaluator<N, V> getTimeoutEvaluator() {
		return this.timeoutEvaluator;
	}

	public String getLoggerName() {
		return this.logger.getName();
	}

	public void setLoggerName(final String loggerName) {
		this.logger = LoggerFactory.getLogger(loggerName);
	}

	@Override
	public BestFirst<P, N, A, V> getAlgorithm(final P problem) {
		BestFirst<P, N, A, V> search = new BestFirst<>(problem);
		this.setupAlgorithm(search);
		return search;
	}

	protected void setupAlgorithm(final BestFirst<P, N, A, V> algorithm) {
		algorithm.setTimeoutForComputationOfF(this.timeoutForFInMS, this.timeoutEvaluator);
		if (this.getLoggerName() != null && this.getLoggerName().length() > 0) {
			algorithm.setLoggerName(this.getLoggerName());
		}
	}
}
