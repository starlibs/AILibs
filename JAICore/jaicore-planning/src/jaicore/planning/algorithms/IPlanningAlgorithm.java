package jaicore.planning.algorithms;

import java.util.Map;

import jaicore.basic.ScoredItem;
import jaicore.basic.algorithm.AOptimizer;
import jaicore.planning.model.core.PlanningProblem;

public abstract class IPlanningAlgorithm<P extends PlanningProblem, S extends ScoredItem<V>, V extends Comparable<V>> extends AOptimizer<P, S, S, V> {
	public IPlanningAlgorithm(P input) {
		super(input);
	}

	public abstract Map<String, Object> getAnnotationsOfSolution(S solution);
}
