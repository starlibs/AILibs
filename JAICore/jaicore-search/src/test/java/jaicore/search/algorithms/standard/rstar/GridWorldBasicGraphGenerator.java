package jaicore.search.algorithms.standard.rstar;

import java.util.ArrayList;
import java.util.List;

import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.SerializableGraphGenerator;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.model.travesaltree.NodeExpansionDescription;
import jaicore.search.model.travesaltree.NodeType;
import jaicore.search.structure.graphgenerator.GoalTester;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.RootGenerator;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

public class GridWorldBasicGraphGenerator implements SerializableGraphGenerator<GridWorld, String> {

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
	public SuccessorGenerator<GridWorld, String> getSuccessorGenerator() {
		return new SuccessorGenerator<GridWorld, String>() {
			@Override
			public List<NodeExpansionDescription<GridWorld, String>> generateSuccessors(GridWorld node) {
				ArrayList<NodeExpansionDescription<GridWorld, String>> succ = new ArrayList<>();
				for (int a = 4; a <= 9; a++) {
					GridWorld n_ = node.onAction(Integer.toString(a));
					if (n_ != null) {
						succ.add(new NodeExpansionDescription<>(node, n_, Integer.toString(a), NodeType.OR));
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
