package jaicore.search.algorithms.standard.npuzzle;

import java.util.ArrayList;
import java.util.Collection;

import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.NodeExpansionDescription;
import jaicore.search.structure.core.NodeType;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

public class NPuzzleRedundantGenerator implements GraphGenerator<NPuzzleRedundantNode,String> {
	
	private int dimension;
	private int shuffle;
	
	public NPuzzleRedundantGenerator(int dimension) {
		this.dimension = dimension;
		this.shuffle = 0;
		
	}
	
	public NPuzzleRedundantGenerator(int dimension, int shuffle) {
		this.dimension = dimension;
		this.shuffle = shuffle;
	}

	@Override
	public SingleRootGenerator<NPuzzleRedundantNode> getRootGenerator() {
		return ()-> {
			if(shuffle != 0)
				return new NPuzzleRedundantNode(dimension, shuffle);
			else
				return new NPuzzleRedundantNode(dimension);
		};
	}

	@Override
	public SuccessorGenerator<NPuzzleRedundantNode, String> getSuccessorGenerator() {
		return n -> {
			Collection<NodeExpansionDescription<NPuzzleRedundantNode, String>> successors = new ArrayList<>();
			
			//Possible successors
			if(n.getEmptyX()> 0)//move left
				successors.add(new NodeExpansionDescription<NPuzzleRedundantNode, String>(n,move(n, "l"), "l", NodeType.OR));
			
			if(n.getEmptyX()< dimension-1)//move right
				successors.add(new NodeExpansionDescription<NPuzzleRedundantNode, String>(n,move(n, "r"), "r", NodeType.OR));
			
			if(n.getEmptyY()>0)//move up
				successors.add(new NodeExpansionDescription<NPuzzleRedundantNode, String>(n,move(n, "u"), "u", NodeType.OR));
			
			if(n.getEmptyY()< dimension -1)//move down
				successors.add(new NodeExpansionDescription<NPuzzleRedundantNode, String>(n,move(n, "d"), "d", NodeType.OR));
			
			return successors;
		};
	}

	@Override
	public NodeGoalTester<NPuzzleRedundantNode> getGoalTester() {
		return n->{
			int[][] board= n.getBoard();
			if(board[dimension-1][dimension-1]!= 0)
				return false;
			else {
				int sol =1;
				for(int i= 0; i < dimension; i++) 
					for(int j = 0; j < dimension; j++){
						if(i != dimension -1 & j != dimension -1)
							if(board[i][j] != sol)
								return false;
						
						sol ++;
					}
				
				return true;
			}
		};
	}

	@Override
	public boolean isSelfContained() {
		return true;
	}
	
	
	
	/**
	 * A method which computes a new PuzzleNode after the empty tile was moved.
	 * @param n
	 * 		The node which contains the boardconfiguration.
	 * @param y
	 * 		The movement on the y-axis. This value should be -1 if going upwards, 1 if going downwards.
	 * 		Otherwise it should be 0.
	 * @param x
	 * 		The movement on the y-axis. This value should be -1 if going left, 1 if going right.
	 * 		Otherwise it should be 0.
	 */
	public NPuzzleRedundantNode move(NPuzzleRedundantNode n,String move) {
		//cloning the board for the new node
		int y;
		int x;
		
		switch(move) {
		case "l" : 
			y = 0;
			x = -1;
			break;
		case "r" : 
			y = 0;
			x = 1;
			break;
		case "d" : 
			y = 1;
			x = 0;
			break;
		case "u" : 
			y = -1;
			x = 0;
			break;
		default:
			System.out.println("No Valid move.");
			return null;
	}
		
		if(x == y || Math.abs(x)>1 || Math.abs(y)>1) {
			System.out.println("No valid move. No move is executed");
			return null;
		}
		
		int[][] b = new int[dimension][dimension];
		for(int i = 0; i< dimension; i++) {
			for(int j= 0; j < dimension ; j++) {
				b[i][j] = n.getBoard()[i][j];
			}
		}
		int eX = n.getEmptyX();
		int eY = n.getEmptyY();
//		int help = b[eY][eX];
		b[eY][eX] = b[eY +y][eX+x];
		b[eY+y][eX+x] = 0;
		
		NPuzzleRedundantNode node = new NPuzzleRedundantNode(b, eX+x, eY+y, n.getMoves()+move);
		
		return node;		
	}

}
