package ai.libs.jaicore.experiments.resultcomputers;

import java.util.Map;

import org.api4.java.algorithm.events.AlgorithmEvent;
import org.api4.java.algorithm.events.ScoredSolutionCandidateFoundEvent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import ai.libs.jaicore.basic.MathExt;
import ai.libs.jaicore.experiments.IEventBasedResultUpdater;

public class SolutionPerformanceHistoryComputer implements IEventBasedResultUpdater {
	ArrayNode observations = new ObjectMapper().createArrayNode();
	final long start = System.currentTimeMillis();

	@Override
	public void processEvent(final AlgorithmEvent e, final Map<String, Object> currentResults) {
		if (e instanceof ScoredSolutionCandidateFoundEvent) {
			double score = (double)((ScoredSolutionCandidateFoundEvent<?, ?>)e).getScore();
			ArrayNode observation = new ObjectMapper().createArrayNode();
			observation.insert(0, System.currentTimeMillis() - this.start); // relative time
			observation.insert(1, MathExt.round(score, 5)); // score
			this.observations.add(observation);
			currentResults.put("history", this.observations);
		}
	}
}
