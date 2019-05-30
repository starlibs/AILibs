package jaicore.testproblems.gridworld;

public class GridWorldNode {
	private final GridWorldProblem problem;
	private final int posx, posy;

	public GridWorldNode(final GridWorldProblem problem, final int x, final int y) {
		assert x >= 0 && x < 16 : "x has to be greater equals zero and less 16. Given x = " + x;
		assert y >= 0 && x < 16 : "y has to be greater equals zero and less 16. Given y = " + y;

		this.problem = problem;
		this.posx = x;
		this.posy = y;
	}

	public GridWorldProblem getProblem() {
		return this.problem;
	}

	public int getX() {
		return this.posx;
	}

	public int getY() {
		return this.posy;
	}
}
