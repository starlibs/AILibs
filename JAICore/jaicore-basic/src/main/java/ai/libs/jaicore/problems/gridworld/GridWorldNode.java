package ai.libs.jaicore.problems.gridworld;

public class GridWorldNode {
	private final GridWorldProblem problem;
	private final int posx;
	private final int posy;

	public GridWorldNode(final GridWorldProblem problem, final int x, final int y) {
		if (x < 0 || x >= 16) {
			throw new IllegalArgumentException("x has to be greater equals zero and less 16. Given x = " + x);
		}
		if (y < 0 || x >= 16) {
			throw new IllegalArgumentException("y has to be greater equals zero and less 16. Given y = " + y);
		}

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
