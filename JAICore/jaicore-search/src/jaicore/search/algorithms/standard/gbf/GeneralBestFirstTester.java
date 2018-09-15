//package jaicore.search.algorithms.standard.gbf;
//
//import static org.junit.Assert.assertNotNull;
//
//import java.util.ArrayList;
//
//import org.junit.Test;
//
//import jaicore.graph.Graph;
//import jaicore.graphvisualizer.gui.VisualizationWindow;
//import jaicore.search.algorithms.standard.bestfirst.model.GraphGenerator;
//import jaicore.search.algorithms.standard.bestfirst.model.Node;
//import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
//import jaicore.search.structure.graphgenerator.NodeGoalTester;
//import jaicore.search.structure.graphgenerator.SingleRootGenerator;
//import jaicore.search.structure.graphgenerator.SuccessorGenerator;
//
//public class GeneralBestFirstTester {
//	
//	private static final boolean VISUALIZE = true;
//
//	static class GameNode {
//		
//		final boolean active;
//		final int remaining;
//		public GameNode(boolean active, int remaining) {
//			super();
//			this.active = active;
//			this.remaining = remaining;
//		}
//		@Override
//		public int hashCode() {
//			final int prime = 31;
//			int result = 1;
//			result = prime * result + (active ? 1231 : 1237);
//			result = prime * result + remaining;
//			return result;
//		}
//		@Override
//		public boolean equals(Object obj) {
//			if (this == obj)
//				return true;
//			if (obj == null)
//				return false;
//			if (getClass() != obj.getClass())
//				return false;
//			GameNode other = (GameNode) obj;
//			if (active != other.active)
//				return false;
//			if (remaining != other.remaining)
//				return false;
//			return true;
//		}
//		
//		@Override
//		public String toString() {
//			return "GameNode [active=" + active + ", remaining=" + remaining + "]";
//		}
//	}
//	
//	static class GameAction {
//		final String name;
//
//		public GameAction(String name) {
//			super();
//			this.name = name;
//		}
//	}
//
//	@Test
//	public void test() {
//		
//		GraphGenerator<GameNode, GameAction> gen = new GraphGenerator<GeneralBestFirstTester.GameNode, GeneralBestFirstTester.GameAction>() {
//
//			@Override
//			public SingleRootGenerator<GameNode> getRootGenerator() {
//				return () -> new GameNode(true, 20);
//			}
//
//			@Override
//			public SuccessorGenerator<GameNode, GameAction> getSuccessorGenerator() {
//				return n -> {
//					return new ArrayList<>();	
////					if (n instanceof OrNode) { 
////						List<NodeExpansionDescription<GameNode,GameAction>> successors = new ArrayList<>();
////						GameNode g = n.getPoint();
////						for (int i = 0; i < 4; i++)
////							if (g.remaining > i)
////								successors.add(new NodeExpansionDescription<>(n.getPoint(), new GameNode(false, g.remaining - i - 1), new GameAction("Take " + (i + 1)), NodeType.AND));
////						return successors;
////					}
////					else {
////						List<NodeExpansionDescription<GameNode,GameAction>> successors = new ArrayList<>();
////						GameNode g = n.getPoint();
////						for (int i = 0; i < 2; i++)
////							if (g.remaining > i)
////								successors.add(new NodeExpansionDescription<>(n.getPoint(), new GameNode(true, g.remaining - i - 1), new GameAction("Enemy takes " + (i + 1)), NodeType.OR));
////						return successors;						
////					}
//				};
//			}
//
//			@Override
//			public NodeGoalTester<GameNode> getGoalTester() {
//				return l -> l.active && l.remaining == 0;
//			}
//			
//			@Override
//			public boolean isSelfContained() {
//				return false;
//			}
//
//			@Override
//			public void setNodeNumbering(boolean nodenumbering) {
//				// TODO Auto-generated method stub
//				
//			}
//		};
//		
//		INodeEvaluator<GameNode, Integer> evaluator = new INodeEvaluator<GameNode,Integer>() {
//			@Override
//			public Integer f(Node<GameNode,?> path) {
//				return 0;
//			}
//		};
//
//		GeneralBestFirst<GameNode,GameAction> gbf = new GeneralBestFirst<GameNode,GameAction>(gen, map -> new ArrayList<>(map.keySet()), l -> 0, evaluator);
//		
//		
//		/* find solution */
//		if (VISUALIZE) {
//			new VisualizationWindow<>(gbf);
//		}
//		Graph<GameNode> solutionGraph = gbf.getSolution();
//		assertNotNull(solutionGraph);
//		System.out.println("Generated " + gbf.getCreatedCounter() + " nodes.");
//		if (VISUALIZE) {
//			new VisualizationWindow<GameNode>(solutionGraph);
//			int j = 0;
//			int i = 0;
//			while (j >= 0)
//				i  = i + 1;
//		}
//	}
//	
//	
//
//}
