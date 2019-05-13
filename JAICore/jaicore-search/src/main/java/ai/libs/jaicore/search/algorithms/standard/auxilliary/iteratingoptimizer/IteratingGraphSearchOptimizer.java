package ai.libs.jaicore.search.algorithms.standard.auxilliary.iteratingoptimizer;

import com.google.common.eventbus.Subscribe;

import ai.libs.jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import ai.libs.jaicore.basic.algorithm.events.AlgorithmEvent;
import ai.libs.jaicore.basic.algorithm.exceptions.AlgorithmException;
import ai.libs.jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;
import ai.libs.jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.events.EvaluatedSearchSolutionCandidateFoundEvent;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.events.GraphSearchSolutionCandidateFoundEvent;
import ai.libs.jaicore.search.core.interfaces.AOptimalPathInORGraphSearch;
import ai.libs.jaicore.search.core.interfaces.IGraphSearch;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;

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
public class IteratingGraphSearchOptimizer<I extends GraphSearchWithPathEvaluationsInput<N, A, V>, N, A, V extends Comparable<V>> extends AOptimalPathInORGraphSearch<I, N, A, V> {

	private final IGraphSearch<GraphSearchInput<N, A>, SearchGraphPath<N, A>, N, A> baseAlgorithm;

	public IteratingGraphSearchOptimizer(final I problem, final IGraphSearch<GraphSearchInput<N, A>, SearchGraphPath<N, A>, N, A> baseAlgorithm) {
		super(problem);
		this.baseAlgorithm = baseAlgorithm;
		baseAlgorithm.registerListener(new Object() {

			@Subscribe
			public void receiveEvent(final AlgorithmEvent e) {
				post(e);
			}
		});
	}

	@Override
	public boolean hasNext() {
		return baseAlgorithm.hasNext();
	}

	@Override
	public AlgorithmEvent nextWithException() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {
		AlgorithmEvent parentEvent = baseAlgorithm.nextWithException();
		if (parentEvent instanceof GraphSearchSolutionCandidateFoundEvent) {
			try {
				SearchGraphPath<N, A> path = ((GraphSearchSolutionCandidateFoundEvent<N,A,?>) parentEvent).getSolutionCandidate();
				V score = getInput().getPathEvaluator().evaluate(path);
				EvaluatedSearchGraphPath<N, A, V> evaluatedPath = new EvaluatedSearchGraphPath<>(path.getNodes(), path.getEdges(), score);
				updateBestSeenSolution(evaluatedPath);
				EvaluatedSearchSolutionCandidateFoundEvent<N,A,V> event = new EvaluatedSearchSolutionCandidateFoundEvent<>(getId(), evaluatedPath);
				post(event);
				return event;
			} catch (ObjectEvaluationFailedException e) {
				throw new AlgorithmException(e, "Object evaluation failed");
			}
		} else {
			return parentEvent;
		}
	}

	public IGraphSearch<GraphSearchInput<N, A>, SearchGraphPath<N, A>, N, A> getBaseAlgorithm() {
		return baseAlgorithm;
	}
	
	
}
