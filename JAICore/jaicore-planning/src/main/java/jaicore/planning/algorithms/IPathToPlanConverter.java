package jaicore.planning.algorithms;

import java.util.List;

import jaicore.planning.model.core.Action;
import jaicore.planning.model.core.Plan;

public interface IPathToPlanConverter<N, PA extends Action> {
	public Plan<PA> getPlan(List<N> path);
}
