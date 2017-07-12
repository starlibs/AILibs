package jaicore.search.algorithms.parallel.parallelexploration.distributed.clustertest;

import java.nio.file.Path;
import java.nio.file.Paths;

import jaicore.search.algorithms.parallel.parallelexploration.distributed.DistributedOrSearchCoworker;
import jaicore.search.algorithms.parallel.parallelexploration.distributed.FolderBasedDistributedSearchCommunicationLayer;
import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.DistributedSearchCommunicationLayer;
import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.SerializableNodeEvaluator;

public class DistributedBestFirstClusterTesterCoworker {

	public static void main(String[] args) {
		int id = Integer.parseInt(args[0]);
		Path folder = Paths.get("./testrsc/comm");
		DistributedBestFirstClusterTesterGenerator gen = new DistributedBestFirstClusterTesterGenerator((int)Math.pow(2, 25), 12345678);
		SerializableNodeEvaluator<TestNode,Integer> evaluator = n -> -1 * n.externalPath().size();
		DistributedSearchCommunicationLayer<TestNode,String,Integer> communicationLayer = new FolderBasedDistributedSearchCommunicationLayer<>(folder);
		DistributedOrSearchCoworker<TestNode, String,Integer> coworker = new DistributedOrSearchCoworker<>(communicationLayer, "cw" + id, 60 * 1000, 1000, 1);
		coworker.cowork();
	}
}