package jaicore.testproblems.gridworld;

import java.util.Arrays;

public class GridWorldProblem  {

	private final int[][] grid = {
			{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
			{1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 1, 1},
			{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 1},
			{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 1},
			{1, 1, 1, 1, 7, 7, 7, 7, 7, 7, 7, 1, 1, 1, 1, 1},
			{1, 1, 1, 1, 7, 7, 7, 8, 7, 7, 7, 1, 1, 1, 1, 1},
			{1, 1, 1, 1, 7, 7, 8, 8, 8, 7, 7, 1, 1, 1, 1, 1},
			{1, 1, 1, 1, 7, 8, 8, 9, 8, 8, 7, 1, 1, 1, 1, 1},
			{1, 1, 1, 1, 7, 7, 8, 8, 8, 7, 7, 1, 1, 1, 1, 1},
			{1, 1, 1, 1, 7, 7, 7, 8, 7, 7, 7, 1, 1, 1, 1, 1},
			{1, 1, 1, 1, 7, 7, 7, 7, 7, 7, 7, 4, 4, 4, 1, 1},
			{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 4, 4, 5, 4, 1, 1},
			{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 4, 5, 6, 5, 4, 1},
			{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 4, 4, 5, 4, 4, 1},
			{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 4, 4, 4, 4, 4, 1},
			{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
	};

	private final int startX;
	private final int startY;
	private final int goalX;
	private final int goaly;

	public GridWorldProblem(final int startX, final int startY, final int goalX, final int goaly) {
		super();
		this.startX = startX;
		this.startY = startY;
		this.goalX = goalX;
		this.goaly = goaly;
	}

	public int getStartX() {
		return this.startX;
	}

	public int getStartY() {
		return this.startY;
	}

	public int getGoalX() {
		return this.goalX;
	}

	public int getGoaly() {
		return this.goaly;
	}

	public int[][] getGrid() {
		return this.grid;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.goalX;
		result = prime * result + this.goaly;
		result = prime * result + Arrays.deepHashCode(this.grid);
		result = prime * result + this.startX;
		result = prime * result + this.startY;
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		GridWorldProblem other = (GridWorldProblem) obj;
		if (this.goalX != other.goalX) {
			return false;
		}
		if (this.goaly != other.goaly) {
			return false;
		}
		if (!Arrays.deepEquals(this.grid, other.grid)) {
			return false;
		}
		if (this.startX != other.startX) {
			return false;
		}
		if (this.startY != other.startY) {
			return false;
		}
		return true;
	}
}
