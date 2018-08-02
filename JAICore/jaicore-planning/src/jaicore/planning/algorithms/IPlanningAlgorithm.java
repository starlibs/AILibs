package jaicore.planning.algorithms;

import java.util.Map;

import jaicore.basic.IIterableAlgorithm;

public interface IPlanningAlgorithm<S extends IPlanningSolution> extends IIterableAlgorithm<S> {
	public Map<String,Object> getAnnotationsOfSolution(S solution);
}
