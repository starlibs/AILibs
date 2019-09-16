package ai.libs.jaicore.search.algorithms.standard.mcts.comparison;

import java.util.Random;

import ai.libs.jaicore.search.algorithms.standard.mcts.MCTSPathSearch;
import ai.libs.jaicore.search.algorithms.standard.mcts.UniformRandomPolicy;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;

public class BradleyTerryMCTSPathSearch<N, A> extends MCTSPathSearch<N, A, Double>{

	public BradleyTerryMCTSPathSearch(final GraphSearchWithPathEvaluationsInput<N, A, Double> problem, final long seed, final boolean sampling, final int maxIter) {
		super(problem, new BradleyTerryLikelihoodPolicy<>(maxIter, sampling ? new Random(seed + BradleyTerryLikelihoodPolicy.class.hashCode()) : null), new UniformRandomPolicy<>(new Random(seed + BradleyTerryMCTSPathSearch.class.hashCode())), 0.0, true);
	}
}
