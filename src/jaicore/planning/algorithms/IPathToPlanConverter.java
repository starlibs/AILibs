package jaicore.planning.algorithms;

import java.util.List;

import jaicore.planning.model.core.Action;

public interface IPathToPlanConverter<N> {
	public List<Action> getPlan(List<N> path);
}
