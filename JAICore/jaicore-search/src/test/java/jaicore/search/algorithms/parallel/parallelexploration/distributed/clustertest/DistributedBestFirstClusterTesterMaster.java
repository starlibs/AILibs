//package jaicore.search.algorithms.parallel.parallelexploration.distributed.clustertest;
//
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.List;
//
//import jaicore.graphvisualizer.SimpleGraphVisualizationWindow;
//import jaicore.search.algorithms.parallel.parallelexploration.distributed.DistributedOrSearch;
//import jaicore.search.algorithms.parallel.parallelexploration.distributed.FolderBasedDistributedSearchCommunicationLayer;
//import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.DistributedSearchCommunicationLayer;
//import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.SerializableNodeEvaluator;
//
//public class DistributedBestFirstClusterTesterMaster {
//
//	public static void main(String[] args) throws InterruptedException {
//		Path folder = Paths.get("Z:/pc2/distsearch/testrsc/comm");
//		DistributedBestFirstClusterTesterGenerator gen = new DistributedBestFirstClusterTesterGenerator((int) Math.pow(2, 25), 12345678);
//		SerializableNodeEvaluator<TestNode, Integer> evaluator = n -> -1 * n.externalPath().size();
//		DistributedSearchCommunicationLayer<TestNode, String, Integer> communicationLayer = new FolderBasedDistributedSearchCommunicationLayer<>(folder, true);
//		DistributedOrSearch<TestNode, String, Integer> master = new DistributedOrSearch<>(gen, evaluator, communicationLayer);
//		new SimpleGraphVisualizationWindow<>(master);
//		List<TestNode> solution = master.nextSolution();
//		master.cancel();
//		System.out.println(solution);
//	}
//}