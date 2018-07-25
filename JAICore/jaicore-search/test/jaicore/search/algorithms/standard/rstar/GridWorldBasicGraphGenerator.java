package jaicore.search.algorithms.standard.rstar;

import java.util.ArrayList;
import java.util.List;

import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.NodeExpansionDescription;
import jaicore.search.structure.core.NodeType;
import jaicore.search.structure.graphgenerator.GoalTester;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.RootGenerator;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

public class GridWorldBasicGraphGenerator implements GraphGenerator<GridWorld, Integer> {

	private final int startX, startY, endX, endY;

	public GridWorldBasicGraphGenerator(int startX, int startY, int endX, int endY) {
		super();
		this.startX = startX;
		this.startY = startY;
		this.endX = endX;
		this.endY = endY;
	}
	
	public GridWorldBasicGraphGenerator(GridWorld from, GridWorld to) {
		super();
		this.startX = from.getX();
		this.startY = from.getY();
		this.endX = to.getX();
		this.endY = to.getY();
	}

	@Override
	public RootGenerator<GridWorld> getRootGenerator() {
		return new SingleRootGenerator<GridWorld>() {
			@Override
			public GridWorld getRoot() {
				return new GridWorld(startX, startY);
			}
		};
	}

	@Override
	public SuccessorGenerator<GridWorld, Integer> getSuccessorGenerator() {
		return new SuccessorGenerator<GridWorld, Integer>() {
			@Override
			public List<NodeExpansionDescription<GridWorld, Integer>> generateSuccessors(GridWorld node) {
				ArrayList<NodeExpansionDescription<GridWorld, Integer>> succ = new ArrayList<>();
				for (int a = 1; a <= 9; a++) {
					GridWorld n_ = node.onAction(a);
					if (n_ != null) {
						succ.add(new NodeExpansionDescription<>(node, n_, a, NodeType.OR));
					}
				}
				return succ;
			}
		};
	}

	@Override
	public GoalTester<GridWorld> getGoalTester() {
		return new NodeGoalTester<GridWorld>() {
			@Override
			public boolean isGoal(GridWorld node) {
				return node.getX() == endX && node.getY() == endY;
			}
		};
	}

	@Override
	public boolean isSelfContained() {
		return false;
	}

	@Override
	public void setNodeNumbering(boolean nodenumbering) {

	}
}
