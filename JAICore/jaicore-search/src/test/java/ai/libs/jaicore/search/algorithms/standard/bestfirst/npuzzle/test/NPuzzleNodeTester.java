package ai.libs.jaicore.search.algorithms.standard.bestfirst.npuzzle.test;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ai.libs.jaicore.problems.npuzzle.NPuzzleState;

public class NPuzzleNodeTester {

	private NPuzzleState n;
	private NPuzzleState n1;
	private NPuzzleState n2;

	@BeforeEach
	public void before() {
		int[][] board = { { 1, 2, 3 }, { 4, 5, 6 }, { 7, 8, 0 } };
		this.n = new NPuzzleState(board, 2, 2);
		board = new int[][] { { 1, 5, 2 }, { 7, 4, 3 }, { 0, 8, 6 } };
		this.n1 = new NPuzzleState(board, 2, 0);
		this.n2 = new NPuzzleState(board, 2, 0);

	}

	@Test
	public void testGetDistance() {
		assertEquals(0, this.n.getDistance(), 0);
		assertEquals(6, this.n1.getDistance(), 0);
		assertEquals(this.n1, this.n2);
	}

}
