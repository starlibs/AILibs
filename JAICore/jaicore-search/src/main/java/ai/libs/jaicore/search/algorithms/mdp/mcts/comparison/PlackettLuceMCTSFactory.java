package ai.libs.jaicore.search.algorithms.mdp.mcts.comparison;

import java.util.Random;

import ai.libs.jaicore.search.algorithms.mdp.mcts.MCTS;
import ai.libs.jaicore.search.algorithms.mdp.mcts.MCTSFactory;
import ai.libs.jaicore.search.probleminputs.IMDP;

public class PlackettLuceMCTSFactory<N, A> extends MCTSFactory<N, A, PlackettLuceMCTSFactory<N, A>> {

	private IPreferenceKernel<N, A> preferenceKernel;

	public PlackettLuceMCTSFactory () {
		this.withTabooExhaustedNodes(true);
	}

	public IPreferenceKernel<N, A> getPreferenceKernel() {
		return this.preferenceKernel;
	}

	public PlackettLuceMCTSFactory<N, A> withPreferenceKernel(final IPreferenceKernel<N, A> preferenceKernel) {
		this.preferenceKernel = preferenceKernel;
		return this;
	}

	@Override
	public MCTS<N, A> getAlgorithm(final IMDP<N, A, Double> input) {
		if (this.preferenceKernel == null) {
			throw new IllegalStateException("Cannot build PL-MCTS since no preference kernel has been set.");
		}
		return new PlackettLuceMCTS<>(input, this.preferenceKernel, this.getMaxIterations(), this.getGamma(), this.getEpsilon(), new Random(this.getRandom().nextLong()), new Random(this.getRandom().nextLong()), this.isTabooExhaustedNodes());
	}

}
