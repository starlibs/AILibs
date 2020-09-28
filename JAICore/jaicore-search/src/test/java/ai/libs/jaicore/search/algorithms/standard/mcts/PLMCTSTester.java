package ai.libs.jaicore.search.algorithms.standard.mcts;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import ai.libs.jaicore.search.algorithms.mdp.mcts.comparison.PlackettLuceMCTSFactory;
import ai.libs.jaicore.search.algorithms.mdp.mcts.comparison.preferencekernel.bootstrapping.BootstrappingPreferenceKernel;
import ai.libs.jaicore.search.algorithms.mdp.mcts.comparison.preferencekernel.bootstrapping.DefaultBootsrapConfigurator;

public class PLMCTSTester extends MCTSForGraphSearchTester {

	@Override
	public <N, A> PlackettLuceMCTSFactory<N, A> getFactory() {
		PlackettLuceMCTSFactory<N, A> factory = new PlackettLuceMCTSFactory<>();
		factory.withPreferenceKernel(new BootstrappingPreferenceKernel<>(DescriptiveStatistics::getMean, new DefaultBootsrapConfigurator(), 10));
		return factory;
	}
}