package jaicore.search.testproblems.npuzzle.parentDiscarding;

import java.util.ArrayList;
import java.util.List;

import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.model.travesaltree.NodeExpansionDescription;
import jaicore.search.model.travesaltree.NodeType;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

public class PDPuzzleGenerator implements GraphGenerator<PDPuzzleNode,String> {
	
	protected int dimension;
	private PDPuzzleNode root;
	

	
	public PDPuzzleGenerator(int[][] board, int emptyX, int emptyY) {
		this.dimension = board.length;
		this.root = new PDPuzzleNode(board, emptyX, emptyY, 0);
	}
	
	

	@Override
	public SingleRootGenerator<PDPuzzleNode> getRootGenerator() {
		return () -> root;
	}

	@Override
	public SuccessorGenerator<PDPuzzleNode, String> getSuccessorGenerator() {
		return n -> {
			List<NodeExpansionDescription<PDPuzzleNode, String>> successors = new ArrayList<>();
			
			//Possible successors
			if(n.getEmptyX()> 0)//move left
				successors.add(new NodeExpansionDescription<PDPuzzleNode, String>(n,move(n, "l"), "l", NodeType.OR));
			
			if(n.getEmptyX()< dimension-1)//move right
				successors.add(new NodeExpansionDescription<PDPuzzleNode, String>(n,move(n, "r"), "r", NodeType.OR));
			
			if(n.getEmptyY()>0)//move up
				successors.add(new NodeExpansionDescription<PDPuzzleNode, String>(n,move(n, "u"), "u", NodeType.OR));
			
			if(n.getEmptyY()< dimension -1)//move down
				successors.add(new NodeExpansionDescription<PDPuzzleNode, String>(n,move(n, "d"), "d", NodeType.OR));
			
			return successors;
		};
	}

	@Override
	public NodeGoalTester<PDPuzzleNode> getGoalTester() {
		return n->{
			int[][] board= n.getBoard();
			if(board[dimension-1][dimension-1]!= 0)
				return false;
			else 
				return true;
		};
	}

	@Override
	public boolean isSelfContained() {
		
		return false;
	}
	
	/**
	 * Moves the empty tile to another location.
	 * The possible parameters to move the empty tiles are:
	 * <code>l</code> for moving the empty space to the left.
	 * <code>right</code> for moving the empty space to the right.
	 * <code>u</code> for moving the empty space upwards.
	 * <code>d</down> for moving the empty space downwards.
	 * @param n
	 * 		The PDPuzzleNode which contains the boardconfiguration.
	 * 
	 * @param m
	 * 		The character which indicates the specific moves. Possible characters are given above.
	 * 		
	 */
	public PDPuzzleNode move(PDPuzzleNode n, String move) {
		switch(move) {
			case "l" : 
				return move(n, 0,-1);
			case "r" : 
				return move(n, 0, 1);
			case "d" : 
				return move(n, 1, 0);
			case "u" : 
				return move(n, -1, 0);
			default:
				System.out.println("No Valid move.");
				return null;
		}
	}
	
	/**
	 * The actual move of the empty tile.
	 * @param n
	 * 		The node which contains the boardconfiguration.
	 * @param y
	 * 		The movement on the y-axis. This value should be -1 if going upwards, 1 if going downwards.
	 * 		Otherwise it should be 0.
	 * @param x
	 * 		The movement on the y-axis. This value should be -1 if going left, 1 if going right.
	 * 		Otherwise it should be 0.
	 */
	public PDPuzzleNode move(PDPuzzleNode n,int y, int x) {
		//cloning the board for the new node
		
		if(x == y || Math.abs(x)>1 || Math.abs(y)>1) {
			System.out.println("No valid move. No move is executed");
			return null;
		}
		
		int[][] b = new int[dimension][dimension];
		int[][] board=n.getBoard();
		for(int i = 0; i< dimension; i++) {
			for(int j= 0; j < dimension ; j++) {
				b[i][j] = board[i][j];
			}
		}
		int eX = n.getEmptyX();
		int eY = n.getEmptyY();
//		int help = b[eY][eX];
		b[eY][eX] = b[eY +y][eX+x];
		b[eY+y][eX+x] = 0;
		
		PDPuzzleNode node = new PDPuzzleNode(b, eX+x, eY+y, n.getNumberOfMoves()+1);
		
		return node;		
	}



	@Override
	public void setNodeNumbering(boolean nodenumbering) {
		// TODO Auto-generated method stub
		
	}

}
