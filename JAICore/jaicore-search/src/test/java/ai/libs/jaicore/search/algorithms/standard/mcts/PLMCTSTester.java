package ai.libs.jaicore.search.algorithms.standard.mcts;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import ai.libs.jaicore.search.algorithms.mdp.mcts.MCTSFactory;
import ai.libs.jaicore.search.algorithms.mdp.mcts.comparison.PlackettLuceMCTSFactory;
import ai.libs.jaicore.search.algorithms.mdp.mcts.comparison.preferencekernel.BootstrappingPreferenceKernel;

public class PLMCTSTester extends MCTSForGraphSearchTester {

	@Override
	public <N, A> MCTSFactory<N, A> getFactory() {
		PlackettLuceMCTSFactory<N, A> factory = new PlackettLuceMCTSFactory<>();
		factory.withPreferenceKernel(new BootstrappingPreferenceKernel<>(DescriptiveStatistics::getMean, 10));
		return factory;
	}
}