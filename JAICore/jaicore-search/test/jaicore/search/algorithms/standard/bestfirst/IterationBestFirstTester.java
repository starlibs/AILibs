package jaicore.search.algorithms.standard.bestfirst;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.junit.Test;

import jaicore.basic.PerformanceLogger;
import jaicore.basic.PerformanceLogger.PerformanceMeasure;
import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.Node;
import jaicore.search.structure.core.NodeExpansionDescription;
import jaicore.search.structure.core.NodeType;
import jaicore.search.structure.core.PriorityQueueOpen;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

public class IterationBestFirstTester {

	static class TestNode {
		static int size = 0;
		int value = size++;
		
		public String toString() { return "" + value; }

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			// TODO Auto-generated method stub
			return super.equals(obj);
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			// TODO Auto-generated method stub
			return super.hashCode();
		}
		
		
	}

	//@Test
	public void test() {
		
		GraphGenerator<TestNode,String> gen = new GraphGenerator<IterationBestFirstTester.TestNode,String>(){
			
			@Override
			public SingleRootGenerator<TestNode> getRootGenerator(){
				return ()->new TestNode();
			}
			
			@Override
			public SuccessorGenerator<TestNode,String> getSuccessorGenerator(){
				return n ->{
					List<NodeExpansionDescription<TestNode,String>> l = new ArrayList<>(3);
					for(int i =0; i < 3; i++){
						l.add(new NodeExpansionDescription<>(n, new TestNode(), "edge label", NodeType.OR));
					}
					return l;
				};
			}
			
			@Override
			public NodeGoalTester<TestNode> getGoalTester() {
				return l -> l.value == 100;
			}
			
			@Override
			public boolean isSelfContained() {
				return false;
			}

			@Override
			public void setNodeNumbering(boolean nodenumbering) {
				// TODO Auto-generated method stub
				
			}
		};
			
		
		
		BestFirst<TestNode,String> bf = new BestFirst<>(gen, n -> (double)Math.round(Math.random() * 100));
//		new SimpleGraphVisualizationWindow<>(bf.getEventBus()).getPanel().setTooltipGenerator(n -> String.valueOf(n.getInternalLabel()));
		
		/* find solution */
		PerformanceLogger.logStart("search");

		List<NodeExpansionDescription<TestNode,String>> nedList = bf.next();
		nedList = bf.next();
		//iterate  1000 times over node expansions
		int i = 0;
//		for(List<NodeExpansionDescription<TestNode, String>> n : bf){
////			System.out.println(n);
//			i++;
//			if(i==1000)
//				break;
//		}
		PerformanceLogger.logEnd("search");
		
		
		/*creating a test for selecting the node*/
		bf = new BestFirst<>(gen, n->(double)Math.round(Math.random() * 100));
		PerformanceLogger.logStart("second search");
		
		PriorityQueueOpen<Node<TestNode,Double>> open = new PriorityQueueOpen<>();
		
		bf.setOpen(open);
		
		bf.next();
		//Printing the nodes from open and choose one via console input
		Scanner sc = new Scanner(System.in);
		for(int j = 0; j < 3; j++) {
			System.out.println("Choose one of the following nodes");
			open.stream().forEach(n->System.out.println(n.getInternalLabel().toString()));
			String input = sc.nextLine();
			
			
		
		}
		PerformanceLogger.logEnd("search");
		
		System.out.println("Generated " + bf.getCreatedCounter() + " nodes.");
		PerformanceLogger.printStatsAndClear(PerformanceMeasure.TIME);
		while (true);
	}
}
