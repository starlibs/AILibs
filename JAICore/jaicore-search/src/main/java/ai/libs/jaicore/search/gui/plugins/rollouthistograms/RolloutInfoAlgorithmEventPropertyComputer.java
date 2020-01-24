package ai.libs.jaicore.search.gui.plugins.rollouthistograms;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.api4.java.algorithm.events.IAlgorithmEvent;

import ai.libs.jaicore.graphvisualizer.events.recorder.property.AlgorithmEventPropertyComputer;
import ai.libs.jaicore.graphvisualizer.events.recorder.property.PropertyComputationFailedException;
import ai.libs.jaicore.graphvisualizer.plugin.nodeinfo.NodeInfoAlgorithmEventPropertyComputer;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.events.RolloutEvent;

public class RolloutInfoAlgorithmEventPropertyComputer implements AlgorithmEventPropertyComputer {

	public static final String ROLLOUT_SCORE_PROPERTY_NAME = "rollout_info";

	private NodeInfoAlgorithmEventPropertyComputer nodeInfoAlgorithmEventPropertyComputer;

	public RolloutInfoAlgorithmEventPropertyComputer() {
		this.nodeInfoAlgorithmEventPropertyComputer = new NodeInfoAlgorithmEventPropertyComputer();
	}

	@Override
	public Object computeAlgorithmEventProperty(final IAlgorithmEvent algorithmEvent) throws PropertyComputationFailedException {
		if (algorithmEvent instanceof RolloutEvent) {
			RolloutEvent<?, ?> rolloutEvent = (RolloutEvent<?, ?>) algorithmEvent;
			List<?> rolloutPath = rolloutEvent.getPath();
			return new RolloutInfo(this.convertNodeToNodeIds(rolloutPath), rolloutEvent.getScore());
		}
		return null;
	}

	private List<String> convertNodeToNodeIds(final List<?> pathNodes) throws PropertyComputationFailedException {
		List<String> path = pathNodes.stream().map(n -> (Object) n).map(n -> this.nodeInfoAlgorithmEventPropertyComputer.getIdOfNodeIfExistent(n)).collect(Collectors.toList());
		if (path.contains(null)) {
			throw new PropertyComputationFailedException("Cannot compute rollout score due to null nodes in path: " + path);
		}
		return path;
	}

	@Override
	public String getPropertyName() {
		return ROLLOUT_SCORE_PROPERTY_NAME;
	}

	@Override
	public List<AlgorithmEventPropertyComputer> getRequiredPropertyComputers() {
		return Arrays.asList(this.nodeInfoAlgorithmEventPropertyComputer);
	}

	@Override
	public void overwriteRequiredPropertyComputer(final AlgorithmEventPropertyComputer computer) {
		this.nodeInfoAlgorithmEventPropertyComputer = (NodeInfoAlgorithmEventPropertyComputer)computer;
	}
}
