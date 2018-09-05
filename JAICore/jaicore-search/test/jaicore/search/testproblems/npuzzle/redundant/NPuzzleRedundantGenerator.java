//package jaicore.search.testproblems.npuzzle.redundant;
//
//import jaicore.search.testproblems.npuzzle.standard.NPuzzleGenerator;
//
//public class NPuzzleRedundantGenerator extends NPuzzleGenerator {
//
//	public NPuzzleRedundantGenerator(int dim, int shuffle) {
//		super(dim, shuffle);
//	}
//
//	public NPuzzleRedundantGenerator(int dim) {
//		super(dim);
//		
//	}
//
//	public NPuzzleRedundantGenerator(int[][] board, int emptyX, int emptyY) {
//		super(board, emptyX, emptyY);
//	}
//	
//	
//	/**
//	 * A method which computes a new PuzzleNode after the empty tile was moved.
//	 * @param n
//	 * 		The node which contains the boardconfiguration.
//	 * @param y
//	 * 		The movement on the y-axis. This value should be -1 if going upwards, 1 if going downwards.
//	 * 		Otherwise it should be 0.
//	 * @param x
//	 * 		The movement on the y-axis. This value should be -1 if going left, 1 if going right.
//	 * 		Otherwise it should be 0.
//	 */
//	public NPuzzleRedundantNode move(NPuzzleRedundantNode n,String move) {
//		//cloning the board for the new node
//		int y;
//		int x;
//		
//		switch(move) {
//		case "l" : 
//			y = 0;
//			x = -1;
//			break;
//		case "r" : 
//			y = 0;
//			x = 1;
//			break;
//		case "d" : 
//			y = 1;
//			x = 0;
//			break;
//		case "u" : 
//			y = -1;
//			x = 0;
//			break;
//		default:
//			System.out.println("No Valid move.");
//			return null;
//	}
//		
//		if(x == y || Math.abs(x)>1 || Math.abs(y)>1) {
//			System.out.println("No valid move. No move is executed");
//			return null;
//		}
//		
//		int[][] b = new int[dimension][dimension];
//		for(int i = 0; i< dimension; i++) {
//			for(int j= 0; j < dimension ; j++) {
//				b[i][j] = n.getBoard()[i][j];
//			}
//		}
//		int eX = n.getEmptyX();
//		int eY = n.getEmptyY();
////		int help = b[eY][eX];
//		b[eY][eX] = b[eY +y][eX+x];
//		b[eY+y][eX+x] = 0;
//		
//		NPuzzleRedundantNode node = new NPuzzleRedundantNode(b, eX+x, eY+y, n.getMoves()+move);
//		
//		return node;		
//	}
//
//}
