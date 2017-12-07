package jaicore.search.algorithms.standard.npuzzle.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import jaicore.search.algorithms.standard.npuzzle.NPuzzleNode;

public class NPuzzleNodeTester {

	NPuzzleNode n;
	NPuzzleNode n1;

	
	@Before
	public void before() {
		int [][] board = {{1,2,3},{4,5,6},{7,8,0}};
		n = new NPuzzleNode(board,2,2);
		board  = new int[][] {{1,5,2},{7,4,3},{0,8,6}};
		n1 = new NPuzzleNode(board,2,0);
		
	}
	@Test
	public void testGetDistance() {
		assertEquals(0,n.getDistance(),0);
		assertEquals(8, n1.getDistance(),0);
	}

}
