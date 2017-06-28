package util.search.distributed.clustertest;

import java.nio.file.Path;
import java.nio.file.Paths;

import util.search.core.NodeEvaluator;
import util.search.distributed.CommunicationFolderBasedDistributionProcessor;
import util.search.distributed.DistributedOrSearchCoworker;
import util.search.distributed.DistributedSearchMaintainer;

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