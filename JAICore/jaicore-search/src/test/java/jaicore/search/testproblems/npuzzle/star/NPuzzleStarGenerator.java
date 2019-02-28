//package jaicore.search.testproblems.npuzzle.star;
//
//import java.util.Set;
//
//import jaicore.search.structure.graphgenerator.NodeGoalTester;
//import jaicore.search.testproblems.npuzzle.standard.NPuzzleGenerator;
//import jaicore.search.testproblems.npuzzle.standard.NPuzzleNode;
//
//public class NPuzzleStarGenerator extends NPuzzleGenerator {
//	
//	private Set<String> needed;
//
//	public NPuzzleStarGenerator(int dim) {
//		super(dim);
//	}
//	
//	public NPuzzleStarGenerator(int dim, int shuffle) {
//		super(dim, shuffle);
//	}
//	
//	public NPuzzleStarGenerator(int[][] board, int emptyX, int emptyY) {
//		super(board, emptyX, emptyY);
//	}
//	
//	@Override
//	public NodeGoalTester<NPuzzleNode> getGoalTester() {
//		return n->{
//			if(needed.isEmpty()){
//				int[][] board= n.getBoard();
//				if(board[dimension-1][dimension-1]!= 0)
//					return false;
//				else {
//					int sol =1;
//					for(int i= 0; i < dimension; i++) 
//						for(int j = 0; j < dimension; j++){
//							if(i != dimension -1 & j != dimension -1)
//								if(board[i][j] != sol)
//									return false;
//							
//							sol ++;
//						}
//					
//					return true;
//				}
//			}
//			else
//				return false;
//		};
//	}
//	
//	
//	
//	@Override
//	public NPuzzleNode move(NPuzzleNode n, String m) {
//		if(needed.contains(m))
//			needed.remove(m);
//		return super.move(n, m);
//
//	}
//	
//	
//
//}
