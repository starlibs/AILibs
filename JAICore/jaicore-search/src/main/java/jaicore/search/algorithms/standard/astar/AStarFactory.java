package jaicore.search.algorithms.standard.astar;

import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.core.interfaces.StandardORGraphSearchFactory;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.probleminputs.GraphSearchWithNumberBasedAdditivePathEvaluation;

public class AStarFactory<T, A> extends StandardORGraphSearchFactory<GraphSearchWithNumberBasedAdditivePathEvaluation<T,A>, EvaluatedSearchGraphPath<T, A, Double>, T, A, Double> {

	private int timeoutForFInMS;
	private INodeEvaluator<T, Double> timeoutEvaluator;
	private String loggerName;

	public AStarFactory() {
		super();
	}

	public AStarFactory(final int timeoutForFInMS) {
		this();
		if (timeoutForFInMS > 0) {
			this.timeoutForFInMS = timeoutForFInMS;
		}
	}

	@Override
	public AStar<T, A> getAlgorithm() {
		return this.getAlgorithm(this.getInput());
	}

	@Override
	public AStar<T, A> getAlgorithm(final GraphSearchWithNumberBasedAdditivePathEvaluation<T, A> input) {
		AStar<T, A> search = new AStar<>(input);
		search.setTimeoutForComputationOfF(this.timeoutForFInMS, this.timeoutEvaluator);
		if (this.loggerName != null && this.loggerName.length() > 0) {
			search.setLoggerName(this.loggerName);
		}
		return search;
	}



	public void setTimeoutForFComputation(final int timeoutInMS, final INodeEvaluator<T, Double> timeoutEvaluator) {
		this.timeoutForFInMS = timeoutInMS;
		this.timeoutEvaluator = timeoutEvaluator;
	}

	public int getTimeoutForFInMS() {
		return this.timeoutForFInMS;
	}

	public INodeEvaluator<T, Double> getTimeoutEvaluator() {
		return this.timeoutEvaluator;
	}

	public String getLoggerName() {
		return this.loggerName;
	}

	public void setLoggerName(final String loggerName) {
		this.loggerName = loggerName;
	}
}
