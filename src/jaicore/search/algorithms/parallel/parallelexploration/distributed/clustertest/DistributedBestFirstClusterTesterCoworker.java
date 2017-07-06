package jaicore.search.algorithms.parallel.parallelexploration.distributed.clustertest;

import java.nio.file.Path;
import java.nio.file.Paths;

import jaicore.search.algorithms.parallel.parallelexploration.distributed.CommunicationFolderBasedDistributionProcessor;
import jaicore.search.algorithms.parallel.parallelexploration.distributed.DistributedOrSearchCoworker;
import jaicore.search.algorithms.parallel.parallelexploration.distributed.DistributedSearchMaintainer;
import jaicore.search.algorithms.standard.core.NodeEvaluator;

public class DistributedBestFirstClusterTesterCoworker {

	public static void main(String[] args) {
		int id = Integer.parseInt(args[0]);
		Path folder = Paths.get("./testrsc/comm");
		DistributedBestFirstClusterTesterGenerator gen = new DistributedBestFirstClusterTesterGenerator((int)Math.pow(2, 25), 12345678);
		NodeEvaluator<TestNode,Integer> evaluator = n -> -1 * n.externalPath().size();
		DistributedSearchMaintainer<TestNode,Integer> communicationLayer = new CommunicationFolderBasedDistributionProcessor<>(folder);
		DistributedOrSearchCoworker<TestNode, String,Integer> coworker = new DistributedOrSearchCoworker<>(gen, evaluator, communicationLayer, "cw" + id, 60 * 1000, 1000, 1);
		coworker.cowork();
	}
}