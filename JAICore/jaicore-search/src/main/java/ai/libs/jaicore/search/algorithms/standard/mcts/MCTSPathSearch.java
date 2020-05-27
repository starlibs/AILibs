package ai.libs.jaicore.search.algorithms.standard.mcts;

import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;
import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;

import ai.libs.jaicore.search.algorithms.mdp.mcts.GraphBasedMDP;
import ai.libs.jaicore.search.algorithms.mdp.mcts.MCTS;
import ai.libs.jaicore.search.algorithms.mdp.mcts.MCTSFactory;
import ai.libs.jaicore.search.core.interfaces.AOptimalPathInORGraphSearch;
import ai.libs.jaicore.search.probleminputs.IMDP;

public class MCTSPathSearch<I extends IPathSearchWithPathEvaluationsInput<N, A, Double>, N, A> extends AOptimalPathInORGraphSearch<I, N, A, Double> {

	private final IMDP<N, A, Double> mdp;
	private final MCTS<N, A> mcts;

	public MCTSPathSearch(final I problem, final MCTSFactory<N, A> mctsFactory) {
		super(problem);
		this.mdp = new GraphBasedMDP<>(problem);
		this.mcts = mctsFactory.getAlgorithm(this.mdp);
	}

	@Override
	public IAlgorithmEvent nextWithException() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {
		switch (this.getState()) {
		case CREATED:
			return this.activate();
		case ACTIVE:
			this.mcts.call();
			return this.terminate();
		default:
			throw new IllegalStateException();
		}
	}

}
