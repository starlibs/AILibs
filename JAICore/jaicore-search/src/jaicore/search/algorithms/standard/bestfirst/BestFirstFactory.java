package jaicore.search.algorithms.standard.bestfirst;

import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.core.interfaces.StandardORGraphSearchFactory;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.model.probleminputs.GeneralEvaluatedTraversalTree;
import jaicore.search.model.travesaltree.Node;

public class BestFirstFactory<P extends GeneralEvaluatedTraversalTree<N, A, V>, N, A, V extends Comparable<V>> extends StandardORGraphSearchFactory<P, EvaluatedSearchGraphPath<N, A, V>,N, A, V, Node<N,V>, A> {

	private int timeoutForFInMS;
	private INodeEvaluator<N, V> timeoutEvaluator;
	private String loggerName;

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
		if (getProblemInput().getGraphGenerator() == null)
			throw new IllegalStateException("Cannot produce BestFirst searches before the graph generator is set in the problem.");
		if (getProblemInput().getNodeEvaluator() == null)
			throw new IllegalStateException("Cannot produce BestFirst searches before the node evaluator is set.");
		BestFirst<P, N, A, V> search = new BestFirst<>(getProblemInput());
		search.setTimeoutForComputationOfF(this.timeoutForFInMS, this.timeoutEvaluator);
		if (loggerName != null && loggerName.length() > 0)
			search.setLoggerName(loggerName);
		return search;
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
		return loggerName;
	}

	public void setLoggerName(String loggerName) {
		this.loggerName = loggerName;
	}
}
