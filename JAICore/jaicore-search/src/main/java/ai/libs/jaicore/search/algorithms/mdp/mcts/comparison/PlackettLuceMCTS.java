package ai.libs.jaicore.search.algorithms.mdp.mcts.comparison;

import java.util.Random;

import ai.libs.jaicore.search.algorithms.mdp.mcts.MCTS;
import ai.libs.jaicore.search.algorithms.mdp.mcts.UniformRandomPolicy;
import ai.libs.jaicore.search.probleminputs.IMDP;

public class PlackettLuceMCTS<N, A> extends MCTS<N, A> {

	public PlackettLuceMCTS(final IMDP<N, A, Double> input, final IPreferenceKernel<N, A> preferenceKernel, final int maxIterations, final double gamma, final double epsilon, final Random randomForTreePolicy, final Random randomForDefaultPolicy, final boolean tabooExhaustedNodes) {
		super(input, new PlackettLucePolicy<>(preferenceKernel, randomForTreePolicy), new UniformRandomPolicy<>(randomForDefaultPolicy), maxIterations, gamma, epsilon, tabooExhaustedNodes);
	}
}
