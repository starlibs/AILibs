package ai.libs.jaicore.search.algorithms.standard.bestfirst;

import java.util.Objects;

import org.api4.java.ai.graphsearch.problem.IOptimalPathInORGraphSearchFactory;
import org.api4.java.ai.graphsearch.problem.IPathSearchInput;
import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import ai.libs.jaicore.search.core.interfaces.StandardORGraphSearchFactory;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public class BestFirstFactory<P extends IPathSearchWithPathEvaluationsInput<N, A, V>, N, A, V extends Comparable<V>> extends StandardORGraphSearchFactory<P, EvaluatedSearchGraphPath<N, A, V>, N, A, V, BestFirst<P, N, A, V>>
implements IOptimalPathInORGraphSearchFactory<P, EvaluatedSearchGraphPath<N, A, V>, N, A, V, BestFirst<P, N, A, V>> {

	private int timeoutForFInMS;
	private IPathEvaluator<N, A, V> timeoutEvaluator;
	private Logger logger = LoggerFactory.getLogger(BestFirstFactory.class);
	private AlgorithmicProblemReduction<IPathSearchInput<N, A>, EvaluatedSearchGraphPath<N, A, V>, GraphSearchWithSubpathEvaluationsInput<N, A, V>, EvaluatedSearchGraphPath<N, A, V>> reduction;

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
		if (this.getInput().getPathEvaluator() == null) {
			throw new IllegalStateException("Cannot produce BestFirst searches before the node evaluator is set.");
		}
		return this.getAlgorithm(this.getInput());
	}

	public void setTimeoutForFComputation(final int timeoutInMS, final IPathEvaluator<N, A, V> timeoutEvaluator) {
		this.timeoutForFInMS = timeoutInMS;
		this.timeoutEvaluator = timeoutEvaluator;
	}

	public int getTimeoutForFInMS() {
		return this.timeoutForFInMS;
	}

	public IPathEvaluator<N, A, V> getTimeoutEvaluator() {
		return this.timeoutEvaluator;
	}

	public String getLoggerName() {
		return this.logger.getName();
	}

	public void setLoggerName(final String loggerName) {
		this.logger = LoggerFactory.getLogger(loggerName);
	}

	public AlgorithmicProblemReduction<IPathSearchInput<N, A>, EvaluatedSearchGraphPath<N, A, V>, GraphSearchWithSubpathEvaluationsInput<N, A, V>, EvaluatedSearchGraphPath<N, A, V>> getReduction() {
		return this.reduction;
	}

	public void setReduction(final AlgorithmicProblemReduction<IPathSearchInput<N, A>, EvaluatedSearchGraphPath<N, A, V>, GraphSearchWithSubpathEvaluationsInput<N, A, V>, EvaluatedSearchGraphPath<N, A, V>> reduction) {
		this.reduction = reduction;
	}

	@Override
	public BestFirst<P, N, A, V> getAlgorithm(final P problem) {
		P acceptedProblem = problem;
		if (!(problem instanceof GraphSearchWithSubpathEvaluationsInput)) {
			if (this.reduction == null) {
				throw new IllegalStateException("No reduction provided for inputs of type " + problem.getClass().getName() + ".");
			}
			Objects.requireNonNull(this.reduction);
			this.logger.info("Reducing the original problem {} using reduction {}", problem, this.reduction);
			acceptedProblem = (P) this.reduction.encodeProblem(problem);
		}
		if (problem.getPathEvaluator() == null) {
			throw new IllegalArgumentException("Cannot create BestFirst algorithm for node evaluator NULL");
		}
		BestFirst<P, N, A, V> search = new BestFirst<>(acceptedProblem);
		this.setupAlgorithm(search);
		return search;
	}

	protected void setupAlgorithm(final BestFirst<P, N, A, V> algorithm) {
		algorithm.setTimeoutForComputationOfF(this.timeoutForFInMS, this.timeoutEvaluator);
	}
}
