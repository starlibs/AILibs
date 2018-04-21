package jaicore.planning.algorithms.forwarddecomposition;

import java.util.List;

import jaicore.planning.algorithms.PlannerSolution;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.planning.model.core.Action;

public class ForwardDecompositionSolution extends PlannerSolution {

	private final List<TFDNode> path;

	public ForwardDecompositionSolution(List<Action> plan, List<TFDNode> path) {
		super(plan);
		this.path = path;
	}

	public List<TFDNode> getPath() {
		return path;
	}
}
