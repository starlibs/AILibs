package jaicore.search.testproblems.nqueens;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class QueenNodeTester {

	QueenNode n1;
	QueenNode n2;

	@Before
	public void before() {
		this.n1 = new QueenNode(Arrays.asList(1, 3, 0), 2, 4);
		this.n2 = new QueenNode(Arrays.asList(1, 3), 0, 4);
	}

	@Test
	public void testGetPositions() {
		List<Integer> comp = Arrays.asList(1, 3, 0, 2);
		assertEquals(comp, this.n1.getPositions());
	}

	@Test
	public void testGetDimension() {
		assertEquals(4, this.n1.getDimension());
	}

	@Test
	public void testToString() {
		String s = "";
		s = "----------------\n";
		s += "|   | Q |   |   |\n";
		s += "----------------\n";
		s += "|   |   |   | Q |\n";
		s += "----------------\n";
		s += "| Q |   |   |   |\n";
		s += "----------------\n";
		s += "|   |   | Q |   |\n";
		s += "----------------";
		assertEquals(s, this.n1.boardVisualizationAsString());
	}

	@Test
	public void testAttack() {
		assertTrue(this.n1.attack(2, 2));
	}

	@Test
	public void testToStringAttack() {
		String s = "";
		s = "----------------\n";
		s += "| O | Q | O | O |\n";
		s += "----------------\n";
		s += "| O | O | O | Q |\n";
		s += "----------------\n";
		s += "| Q | O | O | O |\n";
		s += "----------------\n";
		s += "| O | O | Q | O |\n";
		s += "----------------";
		assertEquals(s, this.n1.toStringAttack());
	}

	@Test
	public void testGetNumberOfQueens() {
		assertEquals(4, this.n1.getNumberOfQueens());
	}

	@Test
	public void testGetNumberOfAttackedCells() {
		assertEquals(16, this.n1.getNumberOfAttackedCells());
	}

	@Test
	public void testGetNumberOfAttackedCellsInNextRow() throws InterruptedException {
		assertEquals(3, this.n2.getNumberOfAttackedCellsInNextRow());
	}

	@Test
	public void testEqual() {
		QueenNode n = new QueenNode(this.n2, 2);
		assertEquals(this.n1, n);
	}

	@Test
	public void testHashCode() {
		QueenNode n = new QueenNode(this.n2, 2);
		assertEquals(this.n1.hashCode(), n.hashCode());
	}

}
