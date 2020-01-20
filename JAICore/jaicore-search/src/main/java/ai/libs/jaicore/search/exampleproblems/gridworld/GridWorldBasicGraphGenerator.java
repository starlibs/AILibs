package ai.libs.jaicore.search.exampleproblems.gridworld;

import java.util.ArrayList;

import org.api4.java.datastructure.graph.implicit.IGraphGenerator;
import org.api4.java.datastructure.graph.implicit.INewNodeDescription;
import org.api4.java.datastructure.graph.implicit.IRootGenerator;
import org.api4.java.datastructure.graph.implicit.ISingleRootGenerator;
import org.api4.java.datastructure.graph.implicit.ISuccessorGenerator;

import ai.libs.jaicore.problems.gridworld.GridWorldNode;
import ai.libs.jaicore.problems.gridworld.GridWorldProblem;
import ai.libs.jaicore.search.model.NodeExpansionDescription;

public class GridWorldBasicGraphGenerator implements IGraphGenerator<GridWorldNode, String> {

	private final GridWorldProblem problem;

	public GridWorldBasicGraphGenerator(final GridWorldProblem problem) {
		super();
		this.problem = problem;
	}

	@Override
	public IRootGenerator<GridWorldNode> getRootGenerator() {
		return new ISingleRootGenerator<GridWorldNode>() {
			@Override
			public GridWorldNode getRoot() {
				return new GridWorldNode(GridWorldBasicGraphGenerator.this.problem, GridWorldBasicGraphGenerator.this.problem.getStartX(), GridWorldBasicGraphGenerator.this.problem.getStartY());
			}
		};
	}

	@Override
	public ISuccessorGenerator<GridWorldNode, String> getSuccessorGenerator() {
		return node -> {
			ArrayList<INewNodeDescription<GridWorldNode, String>> succ = new ArrayList<>();
			for (int a = 4; a <= 9; a++) {

				// x direction movement
				int dx = 1;
				if (a == 2 || a == 7) {
					dx = 0;
				}
				if (a == 1 || a == 4 || a == 6) {
					dx = -1;
				}

				// y direction movement
				int dy = 1;
				if (a == 4 || a == 5) {
					dy = 0;
				}
				if (a == 1 || a == 2 || a == 3) {
					dy = -1;
				}

				int newPosX = node.getX() + dx;
				int newPosY = node.getY() + dy;
				if (newPosX < GridWorldBasicGraphGenerator.this.problem.getGrid().length && newPosY < GridWorldBasicGraphGenerator.this.problem.getGrid()[0].length) {
					succ.add(new NodeExpansionDescription<>(new GridWorldNode(GridWorldBasicGraphGenerator.this.problem, newPosX, newPosY), Integer.toString(a)));
				}
			}
			return succ;
		};
	}
}
