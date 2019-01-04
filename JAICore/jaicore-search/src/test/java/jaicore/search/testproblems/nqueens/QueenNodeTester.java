package jaicore.search.testproblems.nqueens;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class QueenNodeTester {
		
	QueenNode n1;
	QueenNode n2;
	
	@Before
	public void before() {
		n1 = new QueenNode(Arrays.asList(1,3,0),2, 4);
		n2 = new QueenNode(Arrays.asList(1,3),0,4);
	}


	@Test
	public void testGetPositions() {
		List<Integer> comp = Arrays.asList(1,3,0,2);	
		assertEquals(n1.getPositions(), comp);
	}

	@Test
	public void testGetDimension() {
		assertEquals(n1.getDimension(), 4);
	}

//	@Test
//	public void testToString() {
//		String s = "";
//		s = "----------------\n";
//		s+= "|   | Q |   |   |\n";
//		s+= "----------------\n";
//		s+= "|   |   |   | Q |\n";
//		s+= "----------------\n";
//		s+= "| Q |   |   |   |\n";
//		s+= "----------------\n";
//		s+= "|   |   | Q |   |\n";
//		s+= "----------------";
//		
//		System.out.println(s);
//		System.out.println(n1.toString());
//		assertEquals(n1,s);
//	}

	@Test
	public void testAttack() {
			assertEquals(n1.attack(2, 2), true);
	}

//	@Test
//	public void testToStringAttack() {
//		String s = "";
//		s = "----------------\n";
//		s+= "| O | Q | O | O |\n";
//		s+= "----------------\n";
//		s+= "| O | O | O | Q |\n";
//		s+= "----------------\n";
//		s+= "| Q | O | O | O |\n";
//		s+= "----------------\n";
//		s+= "| O | O | Q | O |\n";
//		s+= "----------------";
//		
//		System.out.println(s);
//		System.out.println(n1.toString());
//		assertEquals(n1,s);
//	}

	@Test
	public void testGetNumberOfQueens() {
		assertEquals(n1.getNumberOfQueens(),4);
	}

	@Test
	public void testGetNumberOfAttackedCells() {
		assertEquals(n1.getNumberOfAttackedCells(), 16);
	}

	@Test
	public void testGetNumberOfAttackedCellsInNextRow() throws InterruptedException {
		assertEquals(n2.getNumberOfAttackedCellsInNextRow(),3);
	}
	
	@Test
	public void testEqual() {
		QueenNode n = new QueenNode(n2, 2);
		assertEquals(n1, n);
	}
	
	@Test 
	public void testHashCode() {
		QueenNode n = new QueenNode(n2,2);
		assertEquals(n1.hashCode(),n.hashCode());
	}

}
