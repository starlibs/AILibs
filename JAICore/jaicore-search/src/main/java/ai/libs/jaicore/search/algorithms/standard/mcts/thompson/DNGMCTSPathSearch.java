package ai.libs.jaicore.search.algorithms.standard.mcts.thompson;

import java.util.Random;

import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.NodeGoalTester;

import ai.libs.jaicore.search.algorithms.standard.mcts.MCTSPathSearch;
import ai.libs.jaicore.search.algorithms.standard.mcts.UniformRandomPolicy;
import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;

public class DNGMCTSPathSearch<N, A> extends MCTSPathSearch<N, A, Double>{

	public DNGMCTSPathSearch(final GraphSearchWithPathEvaluationsInput<N, A, Double> problem, final long seed, final double varianceFactor) {
		super(problem, new DNGPolicy<>((NodeGoalTester<N, A>)problem.getGoalTester(), n -> problem.getPathEvaluator().evaluate(new SearchGraphPath<>(n)), varianceFactor), new UniformRandomPolicy<>(new Random(seed + DNGMCTSPathSearch.class.hashCode())), 0.0, true);
	}
}
