//package jaicore.search.algorithms.parallel.parallelexploration.distributed.clustertest;
//
//import java.io.Serializable;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Random;
//
//import org.junit.Test;
//
//import jaicore.graphvisualizer.SimpleGraphVisualizationWindow;
//import jaicore.search.algorithms.parallel.parallelexploration.distributed.DistributedOrSearch;
//import jaicore.search.algorithms.parallel.parallelexploration.distributed.DistributedOrSearchCoworker;
//import jaicore.search.algorithms.parallel.parallelexploration.distributed.FolderBasedDistributedSearchCommunicationLayer;
//import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.DistributedSearchCommunicationLayer;
//import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.SerializableGraphGenerator;
//import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.SerializableNodeEvaluator;
//import jaicore.search.algorithms.standard.bestfirst.model.Node;
//import jaicore.search.algorithms.standard.bestfirst.model.NodeExpansionDescription;
//import jaicore.search.algorithms.standard.bestfirst.model.NodeType;
//import jaicore.search.structure.graphgenerator.NodeGoalTester;
//import jaicore.search.structure.graphgenerator.SingleRootGenerator;
//import jaicore.search.structure.graphgenerator.SuccessorGenerator;
//
//public class DistributedBestFirstTester implements Serializable {
//
//	static class TestNode implements Serializable {
//		private static final long serialVersionUID = 793618120417152627L;
//		final int depth;
//		final int min, max;
//
//		public TestNode(int depth, int min, int max) {
//			super();
//			this.depth = depth;
//			this.min = min;
//			this.max = max;
//		}
//
//		public String toString() {
//			return min + "/" + max;
//		}
//
//		@Override
//		public int hashCode() {
//			final int prime = 31;
//			int result = 1;
//			result = prime * result + max;
//			result = prime * result + min;
//			return result;
//		}
//
//		@Override
//		public boolean equals(Object obj) {
//			if (this == obj)
//				return true;
//			if (obj == null)
//				return false;
//			if (getClass() != obj.getClass())
//				return false;
//			TestNode other = (TestNode) obj;
//			if (max != other.max)
//				return false;
//			if (min != other.min)
//				return false;
//			return true;
//		}
//	}
//
//	@Test
//	public void test() throws InterruptedException {
//
//		Random rand = new Random(1);
//		int size = (int) Math.pow(2, 20);
//		int target = (int) Math.round(rand.nextDouble() * size);
//		System.out.println("Trying to find " + target + " within a space of " + size + " items.");
//
//		SerializableGraphGenerator<TestNode, String> gen = new SerializableGraphGenerator<TestNode, String>() {
//
//			@Override
//			public SingleRootGenerator<TestNode> getRootGenerator() {
//				return () -> new TestNode(0, 0, size);
//			}
//
//			@Override
//			public SuccessorGenerator<TestNode, String> getSuccessorGenerator() {
//				return n -> {
//					List<NodeExpansionDescription<TestNode, String>> l = new ArrayList<>();
//					TestNode parent = n;
//					try {
//						Thread.sleep(10 * n.depth);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//					if (parent.min < parent.max) {
//						int split = (int) Math.floor((parent.min + parent.max) / 2f);
//						l.add(new NodeExpansionDescription<>(parent, new TestNode(parent.depth + 1, parent.min, split), "edge label", NodeType.OR));
//						l.add(new NodeExpansionDescription<>(parent, new TestNode(parent.depth + 1, split + 1, parent.max), "edge label", NodeType.OR));
//					}
//					return l;
//				};
//			}
//
//			@Override
//			public NodeGoalTester<TestNode> getGoalTester() {
//				return n -> (n.min == n.max && n.min == target);
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
//		final Path folder = Paths.get("Z:/pc2/distsearch/testrsc/comm");
//		DistributedSearchCommunicationLayer<TestNode, String, Integer> masterCommunicationLayer = new FolderBasedDistributedSearchCommunicationLayer<>(folder, true);
//
//		SerializableNodeEvaluator<TestNode, Integer> evaluator = n -> -1 * n.externalPath().size();
//		DistributedOrSearch<TestNode, String, Integer> master = new DistributedOrSearch<>(gen, evaluator, masterCommunicationLayer);
//
//		/* setup coworkers */
//		int coworkers = 5;
//		for (int i = 1; i <= coworkers; i++) {
//			final String name = "cw" + i;
//			final String[] args = { folder.toFile().getAbsolutePath(), name, "5", "1000", "true" };
//			new Thread(() -> DistributedOrSearchCoworker.main(args)).start();
//		}
//
//		/* run master in separate thread */
//		SimpleGraphVisualizationWindow<Node<TestNode, Integer>> window = new SimpleGraphVisualizationWindow<>(master);
//		window.setTitle("Master");
//		window.getPanel().setTooltipGenerator(n -> (n.getPoint().min + "-" + n.getPoint().max));
//
//		long start = System.currentTimeMillis();
//		List<TestNode> solution = master.nextSolution();
//		long end = System.currentTimeMillis();
//		org.junit.Assert.assertNotNull(solution);
//		System.out.println(solution);
//		System.out.println("Found after " + (end - start) / 1000f + " seconds.");
//	}
//
//}
