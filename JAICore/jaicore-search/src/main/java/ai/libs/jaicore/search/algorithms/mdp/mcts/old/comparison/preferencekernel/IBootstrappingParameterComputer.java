package ai.libs.jaicore.search.algorithms.mdp.mcts.old.comparison.preferencekernel;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public interface IBootstrappingParameterComputer {
	public double getParameter(DescriptiveStatistics stats);
}
