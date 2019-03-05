//package jaicore.search.algorithms.standard.awastar;
//
//import jaicore.graph.IGraphAlgorithmListener;
//import jaicore.search.core.interfaces.IORGraphSearchFactory;
//import jaicore.search.model.travesaltree.Node;
//import jaicore.search.testproblems.npuzzle.standard.NPuzzleNode;
//import jaicore.search.testproblems.npuzzle.standard.NPuzzleStandardTester;
//public class AwaStarNPuzzleTest extends NPuzzleStandardTester<Node<NPuzzleNode,Double>, String> {
//
//	@Override
//	public IORGraphSearchFactory<NPuzzleNode, String, Double, Node<NPuzzleNode, Double>, String, IGraphAlgorithmListener<Node<NPuzzleNode, Double>, String>> getFactory() {
//		AWAStarFactory<NPuzzleNode, String, Double> searchFactory = new AWAStarFactory<>();
//		searchFactory.setNodeEvaluator(node -> node.externalPath().size() + (double) node.getPoint().getNumberOfWrongTiles());
//		return searchFactory;
//	}
//}
