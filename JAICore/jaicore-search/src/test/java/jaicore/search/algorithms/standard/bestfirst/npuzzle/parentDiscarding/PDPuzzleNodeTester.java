package jaicore.search.algorithms.standard.bestfirst.npuzzle.parentDiscarding;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import jaicore.search.testproblems.npuzzle.parentDiscarding.PDPuzzleNode;
import jaicore.search.testproblems.npuzzle.standard.NPuzzleNode;

public class PDPuzzleNodeTester {
	PDPuzzleNode n;
	PDPuzzleNode n1;
	PDPuzzleNode n2;

	
	@Before
	public void before() {
		int [][] board = {{0,1},{1,1}};
		n = new PDPuzzleNode(board,0,0,0);
		board  = new int[][] {{1,1},{1,0}};
		n1 = new PDPuzzleNode(board,1,1,0);
		n2 = new PDPuzzleNode(board,1,1,0);
		
	}
	@Test
	public void testGetDistance() {
		assertEquals(2,n.getDistance(),0);
		assertEquals(0, n1.getDistance(),0);
		assertEquals(n1,n2);
		assertEquals(n1.hashCode(), n2.hashCode());
	}
}
