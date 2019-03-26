package jaicore.search.algorithms.standard.bestfirst.npuzzle.test;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import jaicore.testproblems.npuzzle.NPuzzleState;

public class NPuzzleNodeTester {

	NPuzzleState n;
	NPuzzleState n1;
	NPuzzleState n2;

	@Before
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
