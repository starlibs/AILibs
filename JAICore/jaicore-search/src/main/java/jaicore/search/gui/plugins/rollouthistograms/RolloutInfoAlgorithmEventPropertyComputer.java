package jaicore.search.gui.plugins.rollouthistograms;

import java.util.List;
import java.util.stream.Collectors;

import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.graphvisualizer.events.recorder.property.AlgorithmEventPropertyComputer;
import jaicore.graphvisualizer.events.recorder.property.PropertyComputationFailedException;
import jaicore.graphvisualizer.plugin.nodeinfo.NodeInfoAlgorithmEventPropertyComputer;
import jaicore.search.algorithms.standard.bestfirst.events.RolloutEvent;

public class RolloutInfoAlgorithmEventPropertyComputer implements AlgorithmEventPropertyComputer {

	public static final String ROLLOUT_SCORE_PROPERTY_NAME = "rollout_info";

	private NodeInfoAlgorithmEventPropertyComputer nodeInfoAlgorithmEventPropertyComputer;

	public RolloutInfoAlgorithmEventPropertyComputer(NodeInfoAlgorithmEventPropertyComputer nodeInfoAlgorithmEventPropertyComputer) {
		this.nodeInfoAlgorithmEventPropertyComputer = nodeInfoAlgorithmEventPropertyComputer;
	}

	@Override
	public Object computeAlgorithmEventProperty(AlgorithmEvent algorithmEvent) throws PropertyComputationFailedException {
		if (algorithmEvent instanceof RolloutEvent) {
			RolloutEvent<?, ?> rolloutEvent = (RolloutEvent<?, ?>) algorithmEvent;
			List<?> rolloutPath = rolloutEvent.getPath();
			return new RolloutInfo(convertNodeToNodeIds(rolloutPath), rolloutEvent.getScore());
		}
		return null;
	}

	private List<String> convertNodeToNodeIds(List<?> pathNodes) throws PropertyComputationFailedException {
		List<String> path = pathNodes.stream().map(n -> (Object) n).map(n -> nodeInfoAlgorithmEventPropertyComputer.getIdOfNodeIfExistent(n)).collect(Collectors.toList());
		if (path.contains(null)) {
			throw new PropertyComputationFailedException("Cannot compute rollout score due to null nodes in path: " + path);
		}
		return path;
	}

	@Override
	public String getPropertyName() {
		return ROLLOUT_SCORE_PROPERTY_NAME;
	}

}
