package ai.libs.jaicore.experiments;

import java.util.Map;

import org.api4.java.algorithm.events.AlgorithmEvent;

public interface IEventBasedResultUpdater {
	public void processEvent(AlgorithmEvent e, Map<String, Object> currentResults);
}
