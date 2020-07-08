package ai.libs.jaicore.search.algorithms.standard.mcts;

import org.api4.java.ai.graphsearch.problem.IOptimalPathInORGraphSearchFactory;
import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;

import ai.libs.jaicore.search.algorithms.mdp.mcts.MCTSFactory;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;


/**
 *
 * @author Felix Mohr
 *
 * @param <N> Type of states (nodes)
 * @param <A> Type of actions
 */
public class MCTSPathSearchFactory<N, A>
implements IOptimalPathInORGraphSearchFactory<IPathSearchWithPathEvaluationsInput<N, A, Double>, EvaluatedSearchGraphPath<N, A, Double>, N, A, Double, MCTSPathSearch<IPathSearchWithPathEvaluationsInput<N, A, Double>, N, A>> {

	private IPathSearchWithPathEvaluationsInput<N, A, Double> problem;
	private MCTSFactory<N, A, ?> mctsFactory;

	@Override
	public MCTSPathSearch<IPathSearchWithPathEvaluationsInput<N, A, Double>, N, A> getAlgorithm() {
		if (this.problem == null) {
			throw new IllegalStateException("No problem has been defined.");
		}
		return this.getAlgorithm(this.problem);
	}

	@Override
	public MCTSPathSearch<IPathSearchWithPathEvaluationsInput<N, A, Double>, N, A> getAlgorithm(final IPathSearchWithPathEvaluationsInput<N, A, Double> input) {
		if (this.mctsFactory == null) {
			throw new IllegalStateException("No MCTS factory has been set. Please set a factory prior to building the MCTS path search.");
		}
		return new MCTSPathSearch<>(input, this.mctsFactory);
	}

	public IPathSearchWithPathEvaluationsInput<N, A, Double> getProblem() {
		return this.problem;
	}

	public MCTSPathSearchFactory<N, A> withProblem(final IPathSearchWithPathEvaluationsInput<N, A, Double> problem) {
		this.problem = problem;
		return this;
	}

	public MCTSFactory<N, A, ?> getMctsFactory() {
		return this.mctsFactory;
	}

	public MCTSPathSearchFactory<N, A> withMCTSFactory(final MCTSFactory<N, A, ?> mctsFactory) {
		this.mctsFactory = mctsFactory;
		return this;
	}
}
