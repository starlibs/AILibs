package jaicore.search.testproblems.gridworld;

import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.model.travesaltree.Node;
import jaicore.testproblems.gridworld.GridWorldNode;

public class GridWorldHeuristic implements INodeEvaluator<GridWorldNode, Double> {

	private final int targetX;
	private final int targetY;

	public GridWorldHeuristic(final int targetX, final int targetY) {
		super();
		this.targetX = targetX;
		this.targetY = targetY;
	}

	@Override
	public Double f(final Node<GridWorldNode, ?> node) {
		int x_ = Math.abs(this.targetX - node.getPoint().getX());
		int y_ = Math.abs(this.targetY - node.getPoint().getY());
		return new Double(x_ + y_);
	}
}
