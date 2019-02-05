package jaicore.planning.hierarchical.algorithms;

import java.util.List;

import jaicore.planning.core.Plan;

public interface IPathToPlanConverter<N> {
	public Plan getPlan(List<N> path);
}
