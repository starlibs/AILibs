package ai.libs.jaicore.search.algorithms.standard.rstar;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;

import ai.libs.jaicore.search.core.interfaces.StandardORGraphSearchFactory;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithNumberBasedAdditivePathEvaluationAndSubPathHeuristic;

public class RStarFactory<I extends GraphSearchWithNumberBasedAdditivePathEvaluationAndSubPathHeuristic<T, A>, T, A> extends StandardORGraphSearchFactory<I, EvaluatedSearchGraphPath<T, A, Double>, T, A, Double, RStar<I, T, A>> {

	private int timeoutForFInMS;
	private IPathEvaluator<T, A, Double> timeoutEvaluator;
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
	public RStar<I, T, A> getAlgorithm() {
		return this.getAlgorithm(this.getInput());
	}

	@Override
	public RStar<I, T, A> getAlgorithm(final I input) {
		RStar<I, T, A> search = new RStar<>(input, this.w, this.k, this.delta);
		if (this.loggerName != null && this.loggerName.length() > 0) {
			search.setLoggerName(this.loggerName);
		}
		return search;
	}

	public void setTimeoutForFComputation(final int timeoutInMS, final IPathEvaluator<T, A, Double> timeoutEvaluator) {
		this.timeoutForFInMS = timeoutInMS;
		this.timeoutEvaluator = timeoutEvaluator;
	}

	public int getTimeoutForFInMS() {
		return this.timeoutForFInMS;
	}

	public IPathEvaluator<T, A, Double> getTimeoutEvaluator() {
		return this.timeoutEvaluator;
	}

	public String getLoggerName() {
		return this.loggerName;
	}

	public void setLoggerName(final String loggerName) {
		this.loggerName = loggerName;
	}
}
