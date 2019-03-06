package jaicore.search.algorithms.standard.bestfirst.npuzzle.parentDiscarding;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import jaicore.search.testproblems.npuzzle.parentdiscarding.PDPuzzleNode;

public class PDPuzzleNodeTester {
	PDPuzzleNode n;
	PDPuzzleNode n1;
	PDPuzzleNode n2;

	@Before
	public void before() {
		int[][] board = { { 0, 1 }, { 1, 1 } };
		this.n = new PDPuzzleNode(board, 0, 0);
		board = new int[][] { { 1, 1 }, { 1, 0 } };
		this.n1 = new PDPuzzleNode(board, 1, 1);
		this.n2 = new PDPuzzleNode(board, 1, 1);

	}

	@Test
	public void testGetDistance() {
		assertEquals(2, this.n.getDistance(), 0);
		assertEquals(0, this.n1.getDistance(), 0);
		assertEquals(this.n1, this.n2);
		assertEquals(this.n1.hashCode(), this.n2.hashCode());
	}
}
