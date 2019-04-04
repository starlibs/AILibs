package jaicore.search.algorithms.standard.rstar;

import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.model.travesaltree.Node;

public class GridWorldHeuristic implements INodeEvaluator<GridWorld, Double> {

	private GridWorld end;

	public GridWorldHeuristic(GridWorld end) {
		super();
		this.end = end;
	}

	@Override
	public Double f(Node<GridWorld, ?> node) throws Exception {
		int x_ = Math.abs(end.getX() - node.getPoint().getX());
		int y_ = Math.abs(end.getY() - node.getPoint().getY());
		return new Double(x_ + y_);
	}
}
