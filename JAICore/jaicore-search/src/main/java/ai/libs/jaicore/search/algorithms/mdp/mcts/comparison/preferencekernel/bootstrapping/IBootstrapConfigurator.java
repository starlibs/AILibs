package ai.libs.jaicore.search.algorithms.mdp.mcts.comparison.preferencekernel.bootstrapping;

import java.util.Map;

import it.unimi.dsi.fastutil.doubles.DoubleList;

public interface IBootstrapConfigurator {

	public int getNumBootstraps(Map<?, DoubleList> observationsPerAction);

	public int getBootstrapSizePerChild(Map<?, DoubleList> observationsPerAction);
}
