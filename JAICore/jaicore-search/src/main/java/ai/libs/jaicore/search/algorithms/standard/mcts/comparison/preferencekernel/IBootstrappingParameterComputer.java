package ai.libs.jaicore.search.algorithms.standard.mcts.comparison.preferencekernel;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public interface IBootstrappingParameterComputer {
	public double getParameter(DescriptiveStatistics stats);
}
