package jaicore.search.algorithms.standard;

import java.util.ArrayList;
import java.util.List;

public class QueenNode {
	/*
	 * Helperclass to store the positions of the queen.
	 */
	public class Position{
		int x;
		int y;
		
		public Position(int x, int y) {
			this.x= x;
			this.y = y;
		}
	}

	List<Position> positions;
	int numberOfQueens;
	
	
	public QueenNode(int x, int y) {
		positions = new ArrayList<>();
		positions.add(new Position(x, y));
		this.numberOfQueens = 1;
	}
	
	public QueenNode(List<Position> pos, int x, int y) {
		this.positions = pos;
		positions.add(new Position(x, y));
	}
	

	
	
}
