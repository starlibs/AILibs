package ai.libs.jaicore.search.algorithms.mdp.mcts.old.comparison;

import java.util.Random;

import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;

import ai.libs.jaicore.search.algorithms.mdp.mcts.old.MCTSPathSearch;
import ai.libs.jaicore.search.algorithms.mdp.mcts.old.UniformRandomPolicy;

public class PlackettLuceMCTSPathSearch<I extends IPathSearchWithPathEvaluationsInput<N, A, Double>,N, A> extends MCTSPathSearch<I,N, A, Double>{

	public PlackettLuceMCTSPathSearch(final I problem, final IPreferenceKernel<N, A> preferenceKernel, final Random randomForTreePolicy, final Random randomForDefaultPolicy) {
		super(problem, new PlackettLucePolicy<>(preferenceKernel, randomForTreePolicy), new UniformRandomPolicy<>(randomForDefaultPolicy), 0.0);
	}
}
