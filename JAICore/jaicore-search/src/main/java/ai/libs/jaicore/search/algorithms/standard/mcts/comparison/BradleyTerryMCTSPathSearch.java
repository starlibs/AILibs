package ai.libs.jaicore.search.algorithms.standard.mcts.comparison;

import java.util.Random;

import org.api4.java.ai.graphsearch.problem.IGraphSearchWithPathEvaluationsInput;

import ai.libs.jaicore.search.algorithms.standard.mcts.MCTSPathSearch;
import ai.libs.jaicore.search.algorithms.standard.mcts.UniformRandomPolicy;

public class BradleyTerryMCTSPathSearch<I extends IGraphSearchWithPathEvaluationsInput<N, A, Double>,N, A> extends MCTSPathSearch<I,N, A, Double>{

	public BradleyTerryMCTSPathSearch(final I problem, final long seed, final boolean sampling) {
		super(problem, new BradleyTerryLikelihoodPolicy<>(sampling ? new Random(seed + BradleyTerryLikelihoodPolicy.class.hashCode()) : null), new UniformRandomPolicy<>(new Random(seed + BradleyTerryMCTSPathSearch.class.hashCode())), 0.0, true);
	}
}
