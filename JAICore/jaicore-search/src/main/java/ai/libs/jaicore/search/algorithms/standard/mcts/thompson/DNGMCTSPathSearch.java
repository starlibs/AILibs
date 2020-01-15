package ai.libs.jaicore.search.algorithms.standard.mcts.thompson;

import java.util.Random;

import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;
import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.INodeGoalTester;

import ai.libs.jaicore.search.algorithms.standard.mcts.MCTSPathSearch;
import ai.libs.jaicore.search.algorithms.standard.mcts.UniformRandomPolicy;
import ai.libs.jaicore.search.model.other.SearchGraphPath;

public class DNGMCTSPathSearch<I extends IPathSearchWithPathEvaluationsInput<N, A, Double>, N, A> extends MCTSPathSearch<I, N, A, Double> {

	public DNGMCTSPathSearch(final I problem, final long seed, final double varianceFactor, final double initLambda) {
		super(problem, new DNGPolicy<>((INodeGoalTester<N, A>) problem.getGoalTester(), n -> problem.getPathEvaluator().evaluate(new SearchGraphPath<>(n)), varianceFactor, initLambda),
				new UniformRandomPolicy<>(new Random(seed + DNGMCTSPathSearch.class.hashCode())), 0.0);
	}
}
