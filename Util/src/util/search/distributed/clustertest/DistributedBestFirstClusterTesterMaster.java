package util.search.distributed.clustertest;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import util.graphvisualizer.SimpleGraphVisualizationWindow;
import util.search.core.NodeEvaluator;
import util.search.distributed.CommunicationFolderBasedDistributionProcessor;
import util.search.distributed.DistributedOrSearchMaster;
import util.search.distributed.DistributedSearchMaintainer;

public class DistributedBestFirstClusterTesterMaster {

	public static void main(String[] args) {
		Path folder = Paths.get("Z:/pc2/distsearch/testrsc/comm");
		DistributedBestFirstClusterTesterGenerator gen = new DistributedBestFirstClusterTesterGenerator((int)Math.pow(2, 25), 12345678);
		NodeEvaluator<TestNode,Integer> evaluator = n -> -1 * n.externalPath().size();
		DistributedSearchMaintainer<TestNode,Integer> communicationLayer = new CommunicationFolderBasedDistributionProcessor<>(folder);
		DistributedOrSearchMaster<TestNode,String,Integer> master = new DistributedOrSearchMaster<>(gen, evaluator, communicationLayer, 1);
		new SimpleGraphVisualizationWindow<>(master.getEventBus());
		List<TestNode> solution = master.nextSolution();
		master.cancel();
		System.out.println(solution);
	}
}