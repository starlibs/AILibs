package jaicore.search.algorithms.standard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.PathGoalTester;
import jaicore.search.structure.graphgenerator.RootGenerator;
import jaicore.search.structure.graphgenerator.SelfContained;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

public class NQueens {
	/**
	 * Helperclass which is used to store the positions of queens
	 * @author jkoepe
	 *
	 */
	public class Position{
		int x;
		int y;
		
		public Position(int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		int getX() {
			return this.x;
		}
		
		int getY() {
			return this.y;
		}
	}
	

	
	public static void main(String [] args) {
		int n = 1;
		if(args.length != 0)
			n = Integer.parseInt(args[0]);
		
		GraphGenerator<QueenNode,String> gen = 
	}
}

public class QueenNode{
	public List<Position> positions;
	int numberOfQueens;
	
	public QueenNode(int x, int y) {
		positions = new ArrayList<Position>();
		positions.add(new Position(x, y));
	}
		
}
