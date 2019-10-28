package ai.libs.jaicore.search.algorithms.standard.auxilliary.iteratingoptimizer;

import org.api4.java.ai.graphsearch.problem.IGraphSearch;
import org.api4.java.ai.graphsearch.problem.IGraphSearchInput;
import org.api4.java.ai.graphsearch.problem.IGraphSearchWithPathEvaluationsInput;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.algorithm.events.AlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.common.attributedobjects.ObjectEvaluationFailedException;
import org.api4.java.common.control.ILoggingCustomizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import ai.libs.jaicore.search.algorithms.standard.bestfirst.events.EvaluatedSearchSolutionCandidateFoundEvent;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.events.GraphSearchSolutionCandidateFoundEvent;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.events.RolloutEvent;
import ai.libs.jaicore.search.core.interfaces.AOptimalPathInORGraphSearch;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.model.other.SearchGraphPath;

/**
 * This is a wrapper class to turn non-optimization algorithms into (uninformed working) optimizers.
 * The algorithm just iterates over all solutions, evaluates them with the given scoring function and eventually returns the best scored solution.
 *
 * @author fmohr
 *
 * @param <I>
 * @param <N>
 * @param <A>
 * @param <V>
 */
public class IteratingGraphSearchOptimizer<I extends IGraphSearchWithPathEvaluationsInput<N, A, V>, N, A, V extends Comparable<V>> extends AOptimalPathInORGraphSearch<I, N, A, V> {

	private final IGraphSearch<IGraphSearchInput<N, A>, SearchGraphPath<N, A>, N, A> baseAlgorithm;
	private int numberOfSeenSolutions = 0;
	private Logger logger = LoggerFactory.getLogger(IteratingGraphSearchOptimizer.class);

	public IteratingGraphSearchOptimizer(final I problem, final IGraphSearch<IGraphSearchInput<N, A>, SearchGraphPath<N, A>, N, A> baseAlgorithm) {
		super(problem);
		this.baseAlgorithm = baseAlgorithm;
		baseAlgorithm.registerListener(new Object() {

			@Subscribe
			public void receiveEvent(final AlgorithmEvent e) throws PathEvaluationException, InterruptedException {
				if (e instanceof RolloutEvent) {
					IteratingGraphSearchOptimizer.this.post(IteratingGraphSearchOptimizer.this.recomputeRolloutEventWithScore((RolloutEvent<N, V>)e));
				}
				else {
					IteratingGraphSearchOptimizer.this.post(e);
				}
			}
		});
	}

	private RolloutEvent<N, V> recomputeRolloutEventWithScore(final RolloutEvent<N, V> e) throws PathEvaluationException, InterruptedException {
		V score = this.getInput().getPathEvaluator().evaluate(new SearchGraphPath<>(e.getPath()));
		return new RolloutEvent<>(e.getAlgorithmId(), e.getPath(), score);
	}

	@Override
	public boolean hasNext() {
		return this.baseAlgorithm.hasNext();
	}

	@Override
	public AlgorithmEvent nextWithException() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {

		this.checkTermination(true);

		switch (this.getState()) {
		case CREATED:
			return this.activate();
		case ACTIVE:
			AlgorithmEvent parentEvent = this.baseAlgorithm.nextWithException();
			if (parentEvent instanceof GraphSearchSolutionCandidateFoundEvent) {
				try {
					SearchGraphPath<N, A> path = ((GraphSearchSolutionCandidateFoundEvent<N,A,?>) parentEvent).getSolutionCandidate();
					V score = this.getInput().getPathEvaluator().evaluate(path);
					EvaluatedSearchGraphPath<N, A, V> evaluatedPath = new EvaluatedSearchGraphPath<>(path.getNodes(), path.getArcs(), score);
					this.updateBestSeenSolution(evaluatedPath);
					this.numberOfSeenSolutions ++;
					EvaluatedSearchSolutionCandidateFoundEvent<N,A,V> event = new EvaluatedSearchSolutionCandidateFoundEvent<>(this.getId(), evaluatedPath);
					this.post(event);
					return event;
				} catch (ObjectEvaluationFailedException e) {
					throw new AlgorithmException("Object evaluation failed", e);
				}
			} else {
				return parentEvent;
			}
		default:
			throw new IllegalStateException("Illegal state " + this.getState());
		}
	}

	public IGraphSearch<IGraphSearchInput<N, A>, SearchGraphPath<N, A>, N, A> getBaseAlgorithm() {
		return this.baseAlgorithm;
	}

	public int getNumberOfSeenSolutions() {
		return this.numberOfSeenSolutions;
	}

	@Override
	public void registerListener(final Object listener) {
		super.registerListener(listener);
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
		if (this.baseAlgorithm instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) this.baseAlgorithm).setLoggerName(name + ".base");
		}
		else {
			this.logger.info("Cannot configure logger of base algorithm, because it does not implement the {} interface.", ILoggingCustomizable.class);
		}
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}
}
