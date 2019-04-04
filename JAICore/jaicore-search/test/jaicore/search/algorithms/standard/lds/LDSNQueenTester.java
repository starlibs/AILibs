//package jaicore.search.algorithms.standard.lds;
//
//import jaicore.basic.algorithm.AlgorithmProblemReducer;
//import jaicore.graph.IGraphAlgorithmListener;
//import jaicore.graph.TreeNode;
//import jaicore.search.core.interfaces.IORGraphSearchFactory;
//import jaicore.search.model.other.EvaluatedSearchGraphPath;
//import jaicore.search.model.probleminputs.NodeRecommendedTree;
//import jaicore.search.testproblems.nqueens.NQueenTester;
//import jaicore.search.testproblems.nqueens.NQueensToNodeRecommendedTreeReducer;
//import jaicore.search.testproblems.nqueens.QueenNode;
//
//public class LDSNQueenTester extends NQueenTester<NodeRecommendedTree<QueenNode,String>,EvaluatedSearchGraphPath<QueenNode,String,Double>, TreeNode<QueenNode>,String> {
//
//	@Override
//	public AlgorithmProblemReducer<Integer, NodeRecommendedTree<QueenNode, String>> getProblemReducer() {
//		return new NQueensToNodeRecommendedTreeReducer();
//	}
//
//	@Override
//	public IORGraphSearchFactory<NodeRecommendedTree<QueenNode, String>, EvaluatedSearchGraphPath<QueenNode, String, Double>, QueenNode, String, Double, TreeNode<QueenNode>, String, IGraphAlgorithmListener<TreeNode<QueenNode>, String>> getFactory() {
//		return new LimitedDiscrepancySearchFactory<>();
//	}
//	
//}
