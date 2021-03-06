package ai.libs.jaicore.search.exampleproblems.npuzzle.standard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.api4.java.datastructure.graph.implicit.IGraphGenerator;
import org.api4.java.datastructure.graph.implicit.INewNodeDescription;
import org.api4.java.datastructure.graph.implicit.ISingleRootGenerator;
import org.api4.java.datastructure.graph.implicit.ISuccessorGenerator;

import ai.libs.jaicore.problems.npuzzle.NPuzzleState;
import ai.libs.jaicore.search.model.NodeExpansionDescription;

/**
 * A simple generator for the normal NPuzzleProblem.
 *
 * @author jkoepe
 *
 */
public class NPuzzleGraphGenerator implements IGraphGenerator<NPuzzleState, String> {

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
	public ISingleRootGenerator<NPuzzleState> getRootGenerator() {
		return () -> this.root;
	}

	@Override
	public ISuccessorGenerator<NPuzzleState, String> getSuccessorGenerator() {
		return n -> {
			List<INewNodeDescription<NPuzzleState, String>> successors = new ArrayList<>();

			// Possible successors
			if (n.getEmptyX() > 0) {
				successors.add(new NodeExpansionDescription<>(this.move(n, "l"), "l"));
			}

			if (n.getEmptyX() < this.dimension - 1) {
				successors.add(new NodeExpansionDescription<>(this.move(n, "r"), "r"));
			}

			if (n.getEmptyY() > 0) {
				successors.add(new NodeExpansionDescription<>(this.move(n, "u"), "u"));
			}

			if (n.getEmptyY() < this.dimension - 1) {
				successors.add(new NodeExpansionDescription<>(this.move(n, "d"), "d"));
			}

			return successors;
		};
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
			b[i] = Arrays.copyOf(board[i], board[i].length);
		}
		int eX = n.getEmptyX();
		int eY = n.getEmptyY();
		b[eY][eX] = b[eY + y][eX + x];
		b[eY + y][eX + x] = 0;

		return new NPuzzleState(b, eX + x, eY + y);
	}

}
