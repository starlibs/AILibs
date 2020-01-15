package ai.libs.jaicore.search.testproblems.gridworld;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.datastructure.graph.ILabeledPath;

import ai.libs.jaicore.problems.gridworld.GridWorldNode;

public class GridWorldHeuristic implements IPathEvaluator<GridWorldNode, Object, Double> {

	private final int targetX;
	private final int targetY;

	public GridWorldHeuristic(final int targetX, final int targetY) {
		super();
		this.targetX = targetX;
		this.targetY = targetY;
	}

	@Override
	public Double evaluate(final ILabeledPath<GridWorldNode, Object> node) {
		int x_ = Math.abs(this.targetX - node.getHead().getX());
		int y_ = Math.abs(this.targetY - node.getHead().getY());
		return new Double(x_ + y_);
	}
}
