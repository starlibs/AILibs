package jaicore.search.testproblems.npuzzle.standard;

import java.util.ArrayList;
import java.util.List;

import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.model.travesaltree.NodeExpansionDescription;
import jaicore.search.model.travesaltree.NodeType;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;
import jaicore.testproblems.npuzzle.NPuzzleState;

/**
 * A simple generator for the normal NPuzzleProblem.
 *
 * @author jkoepe
 *
 */
public class NPuzzleGraphGenerator implements GraphGenerator<NPuzzleState, String> {

	protected int dimension;
	private NPuzzleState root;

	public NPuzzleGraphGenerator(final int[][] board) {
		this.dimension = board.length;
		int emptyX = 0;
		int emptyY = 0;
		for (int x = 0; x < board.length; x++) {
			for (int y = 0; y < board[x].length; y++) {
				if (board[x][y] == 0) {
					emptyX = x;
					emptyY = y;
					break;
				}
			}
		}
		this.root = new NPuzzleState(board, emptyX, emptyY);
	}

	@Override
	public SingleRootGenerator<NPuzzleState> getRootGenerator() {
		return () -> this.root;
	}

	@Override
	public SuccessorGenerator<NPuzzleState, String> getSuccessorGenerator() {
		return n -> {
			List<NodeExpansionDescription<NPuzzleState, String>> successors = new ArrayList<>();

			// Possible successors
			if (n.getEmptyX() > 0) {
				successors.add(new NodeExpansionDescription<NPuzzleState, String>(n, this.move(n, "l"), "l", NodeType.OR));
			}

			if (n.getEmptyX() < this.dimension - 1) {
				successors.add(new NodeExpansionDescription<NPuzzleState, String>(n, this.move(n, "r"), "r", NodeType.OR));
			}

			if (n.getEmptyY() > 0) {
				successors.add(new NodeExpansionDescription<NPuzzleState, String>(n, this.move(n, "u"), "u", NodeType.OR));
			}

			if (n.getEmptyY() < this.dimension - 1) {
				successors.add(new NodeExpansionDescription<NPuzzleState, String>(n, this.move(n, "d"), "d", NodeType.OR));
			}

			return successors;
		};
	}

	@Override
	public NodeGoalTester<NPuzzleState> getGoalTester() {
		return n -> {
			int[][] board = n.getBoard();
			if (board[this.dimension - 1][this.dimension - 1] != 0) {
				return false;
			} else {
				int sol = 1;
				for (int i = 0; i < this.dimension; i++) {
					for (int j = 0; j < this.dimension; j++) {
						if (i != this.dimension - 1 && j != this.dimension - 1 && board[i][j] != sol) {
							return false;
						}
						sol++;
					}
				}
				return true;
			}
		};
	}

	@Override
	public boolean isSelfContained() {

		return false;
	}

	/**
	 * Moves the empty tile to another location.
	 * The possible parameters to move the empty tiles are:
	 * <code>l</code> for moving the empty space to the left.
	 * <code>right</code> for moving the empty space to the right.
	 * <code>u</code> for moving the empty space upwards.
	 * <code>d</down> for moving the empty space downwards.
	 *
	 * @param n
	 *            The NPuzzleNode which contains the boardconfiguration.
	 *
	 * @param m
	 *            The character which indicates the specific moves. Possible characters are given above.
	 *
	 */
	public NPuzzleState move(final NPuzzleState n, final String move) {
		switch (move) {
		case "l":
			return this.move(n, 0, -1);
		case "r":
			return this.move(n, 0, 1);
		case "d":
			return this.move(n, 1, 0);
		case "u":
			return this.move(n, -1, 0);
		default:
			throw new IllegalArgumentException(move + " is not a valid move. Valid moves: {l, r, d, u}");
		}
	}

	/**
	 * The actual move of the empty tile.
	 *
	 * @param n
	 *            The node which contains the boardconfiguration.
	 * @param y
	 *            The movement on the y-axis. This value should be -1 if going upwards, 1 if going downwards.
	 *            Otherwise it should be 0.
	 * @param x
	 *            The movement on the y-axis. This value should be -1 if going left, 1 if going right.
	 *            Otherwise it should be 0.
	 */
	public NPuzzleState move(final NPuzzleState n, final int y, final int x) {
		// cloning the board for the new node

		if (x == y || Math.abs(x) > 1 || Math.abs(y) > 1) {
			return null;
		}

		int[][] b = new int[this.dimension][this.dimension];
		int[][] board = n.getBoard();
		for (int i = 0; i < this.dimension; i++) {
			for (int j = 0; j < this.dimension; j++) {
				b[i][j] = board[i][j];
			}
		}
		int eX = n.getEmptyX();
		int eY = n.getEmptyY();
		b[eY][eX] = b[eY + y][eX + x];
		b[eY + y][eX + x] = 0;

		return new NPuzzleState(b, eX + x, eY + y);
	}

	@Override
	public void setNodeNumbering(final boolean nodenumbering) {

		/* not applicable */
	}

}
