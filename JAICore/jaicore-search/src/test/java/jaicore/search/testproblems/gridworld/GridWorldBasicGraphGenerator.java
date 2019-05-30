package jaicore.search.testproblems.gridworld;

import java.util.ArrayList;
import java.util.List;

import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.SerializableGraphGenerator;
import jaicore.search.model.travesaltree.NodeExpansionDescription;
import jaicore.search.model.travesaltree.NodeType;
import jaicore.search.structure.graphgenerator.GoalTester;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.RootGenerator;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;
import jaicore.testproblems.gridworld.GridWorldNode;
import jaicore.testproblems.gridworld.GridWorldProblem;

@SuppressWarnings("serial")
public class GridWorldBasicGraphGenerator implements SerializableGraphGenerator<GridWorldNode, String> {

	private final GridWorldProblem problem;

	public GridWorldBasicGraphGenerator(final GridWorldProblem problem) {
		super();
		this.problem = problem;
	}

	@Override
	public RootGenerator<GridWorldNode> getRootGenerator() {
		return new SingleRootGenerator<GridWorldNode>() {
			@Override
			public GridWorldNode getRoot() {
				return new GridWorldNode(GridWorldBasicGraphGenerator.this.problem, GridWorldBasicGraphGenerator.this.problem.getStartX(), GridWorldBasicGraphGenerator.this.problem.getStartY());
			}
		};
	}

	@Override
	public SuccessorGenerator<GridWorldNode, String> getSuccessorGenerator() {
		return new SuccessorGenerator<GridWorldNode, String>() {
			@Override
			public List<NodeExpansionDescription<GridWorldNode, String>> generateSuccessors(final GridWorldNode node) {
				ArrayList<NodeExpansionDescription<GridWorldNode, String>> succ = new ArrayList<>();
				for (int a = 4; a <= 9; a++) {

					// x direction movement
					int dx = 1;
					if (a==2 || a==7) {
						dx = 0;
					}
					if (a==1 || a==4 || a==6) {
						dx = -1;
					}

					// y direction movement
					int dy = 1;
					if (a==4 || a==5) {
						dy = 0;
					}
					if (a==1 || a==2 || a==3) {
						dy = -1;
					}

					int newPosX = node.getX() + dx;
					int newPosY = node.getY() + dy;
					if (newPosX < GridWorldBasicGraphGenerator.this.problem.getGrid().length && newPosY < GridWorldBasicGraphGenerator.this.problem.getGrid()[0].length) {
						succ.add(new NodeExpansionDescription<>(node, new GridWorldNode(GridWorldBasicGraphGenerator.this.problem, newPosX, newPosY), Integer.toString(a), NodeType.OR));
					}
				}
				return succ;
			}
		};
	}

	@Override
	public GoalTester<GridWorldNode> getGoalTester() {
		return new NodeGoalTester<GridWorldNode>() {
			@Override
			public boolean isGoal(final GridWorldNode node) {
				return node.getX() == GridWorldBasicGraphGenerator.this.problem.getGoalX() && node.getY() == GridWorldBasicGraphGenerator.this.problem.getGoaly();
			}
		};
	}

	@Override
	public boolean isSelfContained() {
		return false;
	}

	@Override
	public void setNodeNumbering(final boolean nodenumbering) {

	}
}
