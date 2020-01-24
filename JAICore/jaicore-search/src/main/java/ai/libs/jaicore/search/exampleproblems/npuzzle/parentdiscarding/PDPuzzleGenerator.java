package ai.libs.jaicore.search.exampleproblems.npuzzle.parentdiscarding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.api4.java.datastructure.graph.implicit.IGraphGenerator;
import org.api4.java.datastructure.graph.implicit.INewNodeDescription;
import org.api4.java.datastructure.graph.implicit.ISingleRootGenerator;
import org.api4.java.datastructure.graph.implicit.ISuccessorGenerator;

import ai.libs.jaicore.search.model.NodeExpansionDescription;

public class PDPuzzleGenerator implements IGraphGenerator<PDPuzzleNode, String> {

	protected int dimension;
	private PDPuzzleNode root;

	public PDPuzzleGenerator(final int[][] board, final int emptyX, final int emptyY) {
		this.dimension = board.length;
		this.root = new PDPuzzleNode(board, emptyX, emptyY);
	}

	@Override
	public ISingleRootGenerator<PDPuzzleNode> getRootGenerator() {
		return () -> this.root;
	}

	@Override
	public ISuccessorGenerator<PDPuzzleNode, String> getSuccessorGenerator() {
		return n -> {
			List<INewNodeDescription<PDPuzzleNode, String>> successors = new ArrayList<>();

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
	 *            The PDPuzzleNode which contains the boardconfiguration.
	 *
	 * @param m
	 *            The character which indicates the specific moves. Possible characters are given above.
	 *
	 */
	public PDPuzzleNode move(final PDPuzzleNode n, final String move) {
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
	public PDPuzzleNode move(final PDPuzzleNode n, final int y, final int x) {
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

		return new PDPuzzleNode(b, eX + x, eY + y);
	}
}
