package jaicore.search.testproblems.nqueens;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class QueenNode implements Serializable {
	private int dimension;
	private List<Integer> positions;

	/**
	 * Creates a QueenNode with a empty board
	 *
	 * @param dimension
	 *            The dimension of the board.
	 */
	public QueenNode(final int dimension) {
		this.positions = new ArrayList<>();
		this.dimension = dimension;
	}

	/**
	 * Creates a QueenNode with one Queen on it
	 *
	 * @param x
	 *            The row position of the queen.
	 * @param y
	 *            The column position of the queen.
	 * @param dimension
	 *            The dimension of the board.
	 */
	public QueenNode(final int x, final int y, final int dimension) {
		this.positions = new ArrayList<>();
		this.positions.add(x, y);
		this.dimension = dimension;

	}

	/**
	 * Creates a QueenNode with exiting positions of other queens
	 *
	 * @param pos
	 *            The positions of the other queens.
	 * @param y
	 *            The column position of the newly placed queen.
	 * @param dimension
	 *            The dimension of the board.
	 */
	public QueenNode(final List<Integer> pos, final int y, final int dimension) {
		this.positions = new ArrayList<>();
		for (Integer p : pos) {
			this.positions.add(p);
		}
		this.positions.add(y);
		this.dimension = dimension;
	}

	/**
	 * Creates a QueenNode with exiting positions of other queens
	 *
	 * @param pos
	 *            The positions of the other queens.
	 * @param x
	 *            The row position of the newly placed queen.
	 * @param y
	 *            The column position of the newly placed queen.
	 * @param dimension
	 *            The dimension of the board.
	 */
	public QueenNode(final List<Integer> pos, final int x, final int y, final int dimension) {
		for (Integer p : pos) {
			this.positions.add(p);
		}
		this.positions = pos;
		this.positions.add(x, y);
		this.dimension = dimension;
	}

	/**
	 * Creates a new QueenNode out of another QueenNode to add a new queen.
	 *
	 * @param n
	 *            The old QueenNode.
	 * @param y
	 *            The column position of the new queen.
	 */
	public QueenNode(final QueenNode n, final int y) {
		this.positions = new ArrayList<>(n.getPositions().size());
		for (Integer p : n.getPositions()) {
			this.positions.add(p);
		}
		this.positions.add(y);
		this.dimension = n.getDimension();
	}

	public List<Integer> getPositions() {
		return this.positions;
	}

	public int getDimension() {
		return this.dimension;
	}

	@Override
	public String toString() {
		return this.positions.toString();
	}

	public String boardVisualizationAsString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < this.dimension; i++) {
			sb.append("----");
		}
		sb.append("\n|");
		for (int i = 0; i < this.dimension; i++) {
			for (int j = 0; j < this.dimension; j++) {
				if (this.positions.size() > i && this.positions.get(i) == j) {
					sb.append(" Q |");
				} else {
					sb.append("   |");
				}
			}
			sb.append("\n");
			for (int j = 0; j < this.dimension; j++) {
				sb.append("----");
			}
			if (i < this.dimension - 1) {
				sb.append("\n|");
			}
		}
		return sb.toString();
	}

	/**
	 * Checks if a cell is attacked by the queens on the board
	 *
	 * @param i
	 *            The row of the cell to be checked.
	 * @param j
	 *            The collumn of the cell to be checked.
	 * @return
	 * 		<code>true</code> if the cell is attacked, <code>false</code> otherwise.
	 */
	public boolean attack(final int i, final int j) {
		for (Integer p : this.positions) {
			if (j == p) {
				return true;
			}
			int x = Math.abs(i - this.positions.indexOf(p));
			if (j == p + x || p - x == j) {
				return true;
			}
		}
		return false;
	}

	public String toStringAttack() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < this.dimension; i++) {
			sb.append("----");
		}
		sb.append("\n|");
		for (int i = 0; i < this.dimension; i++) {
			for (int j = 0; j < this.dimension; j++) {
				if (this.positions.get(i) == j) {
					sb.append(" Q |");
				} else {
					boolean attack = this.attack(i, j);
					if (attack) {
						sb.append(" O |");
					} else {
						sb.append("   |");
					}
				}
			}
			sb.append("\n");
			for (int j = 0; j < this.dimension; j++) {
				sb.append("----");
			}
			if (i < this.dimension - 1) {
				sb.append("\n|");
			}
		}
		return sb.toString();
	}

	public int getNumberOfQueens() {
		return this.positions.size();
	}

	/**
	 * Returns the number of attacked cells of the current boardconfiguration
	 *
	 * @return
	 * 		The number of attacked cells.
	 */
	public int getNumberOfAttackedCells() {
		int attackedCells = this.positions.size() * this.dimension;
		for (int i = this.positions.size(); i < this.dimension; i++) {
			for (int j = 0; j < this.dimension; j++) {
				if (this.attack(i, j)) {
					attackedCells++;
				}
			}
		}
		return attackedCells;
	}

	/**
	 * Returns the number of attacked cells in the next free row from top down.
	 *
	 * @return
	 * 		The number of attacked cells in the next row.
	 * @throws InterruptedException
	 */
	public int getNumberOfAttackedCellsInNextRow() throws InterruptedException {
		int attackedCells = 0;
		for (int i = 0; i < this.dimension; i++) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			if (this.attack(this.dimension - 1, i)) {
				attackedCells++;
			}
		}
		return attackedCells;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.dimension;
		result = prime * result + ((this.positions == null) ? 0 : this.positions.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
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
		QueenNode other = (QueenNode) obj;
		if (this.dimension != other.dimension) {
			return false;
		}
		if (this.positions == null) {
			if (other.positions != null) {
				return false;
			}
		} else if (!this.positions.equals(other.positions)) {
			return false;
		}
		return true;
	}

	public int getNumberOfNotAttackedCells() {
		return (this.dimension * this.dimension) - this.getNumberOfAttackedCells();
	}
}
