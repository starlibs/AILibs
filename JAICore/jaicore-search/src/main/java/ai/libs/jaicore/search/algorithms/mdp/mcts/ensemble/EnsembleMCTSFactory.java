package ai.libs.jaicore.search.algorithms.mdp.mcts.ensemble;

import java.util.ArrayList;
import java.util.Collection;

import ai.libs.jaicore.search.algorithms.mdp.mcts.IPathUpdatablePolicy;
import ai.libs.jaicore.search.algorithms.mdp.mcts.MCTSFactory;
import ai.libs.jaicore.search.probleminputs.IMDP;

public class EnsembleMCTSFactory<N, A> extends MCTSFactory<N, A> {

	private Collection<IPathUpdatablePolicy<N, A, Double>> treePolicies = new ArrayList<>();

	public Collection<IPathUpdatablePolicy<N, A, Double>> getTreePolicies() {
		return this.treePolicies;
	}

	public void setTreePolicies(final Collection<IPathUpdatablePolicy<N, A, Double>> treePolicies) {
		this.treePolicies = treePolicies;
	}

	@Override
	public EnsembleMCTS<N, A> getAlgorithm(final IMDP<N, A, Double> input) {
		return new EnsembleMCTS<>(input, this.treePolicies, this.getMaxIterations(), this.getGamma(), this.getEpsilon(), this.getRandom(), this.isTabooExhaustedNodes());
	}

}
