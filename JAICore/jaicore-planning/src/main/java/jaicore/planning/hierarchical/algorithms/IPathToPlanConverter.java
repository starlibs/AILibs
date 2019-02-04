package jaicore.planning.hierarchical.algorithms;

import java.util.List;

import jaicore.planning.core.Action;
import jaicore.planning.core.Plan;

public interface IPathToPlanConverter<N, PA extends Action> {
	public Plan<PA> getPlan(List<N> path);
}
