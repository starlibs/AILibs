package ai.libs.jaicore.search.algorithms.standard.auxilliary.iteratingoptimizer;

import org.api4.java.ai.graphsearch.problem.IPathSearch;
import org.api4.java.ai.graphsearch.problem.IPathSearchInput;
import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;
import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.common.attributedobjects.ObjectEvaluationFailedException;

import com.google.common.eventbus.Subscribe;

import ai.libs.jaicore.search.algorithms.standard.bestfirst.events.EvaluatedSearchSolutionCandidateFoundEvent;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.events.GraphSearchSolutionCandidateFoundEvent;
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
public class IteratingGraphSearchOptimizer<I extends IPathSearchWithPathEvaluationsInput<N, A, V>, N, A, V extends Comparable<V>> extends AOptimalPathInORGraphSearch<I, N, A, V> {

	private final IPathSearch<IPathSearchInput<N, A>, SearchGraphPath<N, A>, N, A> baseAlgorithm;

	public IteratingGraphSearchOptimizer(final I problem, final IPathSearch<IPathSearchInput<N, A>, SearchGraphPath<N, A>, N, A> baseAlgorithm) {
		super(problem);
		this.baseAlgorithm = baseAlgorithm;
		baseAlgorithm.registerListener(new Object() {

			@Subscribe
			public void receiveEvent(final IAlgorithmEvent e) {
				IteratingGraphSearchOptimizer.this.post(e);
			}
		});
	}

	@Override
	public boolean hasNext() {
		return this.baseAlgorithm.hasNext();
	}

	@Override
	public IAlgorithmEvent nextWithException() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {
		IAlgorithmEvent parentEvent = this.baseAlgorithm.nextWithException();
		if (parentEvent instanceof GraphSearchSolutionCandidateFoundEvent) {
			try {
				SearchGraphPath<N, A> path = ((GraphSearchSolutionCandidateFoundEvent<N,A,?>) parentEvent).getSolutionCandidate();
				V score = this.getInput().getPathEvaluator().evaluate(path);
				EvaluatedSearchGraphPath<N, A, V> evaluatedPath = new EvaluatedSearchGraphPath<>(path.getNodes(), path.getArcs(), score);
				this.updateBestSeenSolution(evaluatedPath);
				EvaluatedSearchSolutionCandidateFoundEvent<N,A,V> event = new EvaluatedSearchSolutionCandidateFoundEvent<>(this, evaluatedPath);
				this.post(event);
				return event;
			} catch (ObjectEvaluationFailedException e) {
				throw new AlgorithmException("Object evaluation failed", e);
			}
		} else {
			return parentEvent;
		}
	}

	public IPathSearch<IPathSearchInput<N, A>, SearchGraphPath<N, A>, N, A> getBaseAlgorithm() {
		return this.baseAlgorithm;
	}


}
