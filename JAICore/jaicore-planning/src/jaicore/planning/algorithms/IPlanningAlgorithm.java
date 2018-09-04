package jaicore.planning.algorithms;

import java.util.Map;

import jaicore.basic.algorithm.IAlgorithm;
import jaicore.planning.model.core.PlanningProblem;

public interface IPlanningAlgorithm<P extends PlanningProblem, S> extends IAlgorithm<P, S> {
	public Map<String, Object> getAnnotationsOfSolution(S solution);
}
