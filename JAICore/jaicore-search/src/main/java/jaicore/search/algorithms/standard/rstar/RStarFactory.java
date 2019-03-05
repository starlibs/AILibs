package jaicore.search.algorithms.standard.rstar;

import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.core.interfaces.StandardORGraphSearchFactory;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.probleminputs.GraphSearchWithNumberBasedAdditivePathEvaluationAndSubPathHeuristic;

public class RStarFactory<T, A> extends StandardORGraphSearchFactory<GraphSearchWithNumberBasedAdditivePathEvaluationAndSubPathHeuristic<T, A>, EvaluatedSearchGraphPath<T, A, Double>, T, A, Double> {

	private int timeoutForFInMS;
	private INodeEvaluator<T, Double> timeoutEvaluator;
	private String loggerName;
	private double w = 1.0;
	private int k = 3;
	private double delta = 0.0;

	public RStarFactory() {
		super();
	}

	public RStarFactory(final int timeoutForFInMS) {
		this();
		if (timeoutForFInMS > 0) {
			this.timeoutForFInMS = timeoutForFInMS;
		}
	}

	public double getW() {
		return this.w;
	}

	public void setW(final double w) {
		this.w = w;
	}

	public int getK() {
		return this.k;
	}

	public void setK(final int k) {
		this.k = k;
	}

	public double getDelta() {
		return this.delta;
	}

	public void setDelta(final double delta) {
		this.delta = delta;
	}

	@Override
	public RStar<T, A> getAlgorithm() {
		return this.getAlgorithm(this.getInput());
	}

	@Override
	public RStar<T, A> getAlgorithm(final GraphSearchWithNumberBasedAdditivePathEvaluationAndSubPathHeuristic<T, A> input) {
		RStar<T, A> search = new RStar<>(input, this.w, this.k, this.delta);
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
