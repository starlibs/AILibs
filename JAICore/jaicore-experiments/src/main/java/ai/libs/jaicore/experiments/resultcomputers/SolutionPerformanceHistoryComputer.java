package ai.libs.jaicore.experiments.resultcomputers;

import java.util.Map;

import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.events.result.IScoredSolutionCandidateFoundEvent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import ai.libs.jaicore.basic.MathExt;
import ai.libs.jaicore.experiments.IEventBasedResultUpdater;

public class SolutionPerformanceHistoryComputer implements IEventBasedResultUpdater {
	ArrayNode observations = new ObjectMapper().createArrayNode();
	final long start = System.currentTimeMillis();

	private final int saveRate;

	public SolutionPerformanceHistoryComputer(final int saveRate) {
		super();		this.saveRate = saveRate;
	}

	@Override
	public void processEvent(final IAlgorithmEvent e, final Map<String, Object> currentResults) {
		if (e instanceof IScoredSolutionCandidateFoundEvent) {
			@SuppressWarnings("rawtypes")
			double score = (double) ((IScoredSolutionCandidateFoundEvent) e).getScore();
			ArrayNode observation = new ObjectMapper().createArrayNode();
			observation.insert(0, System.currentTimeMillis() - this.start); // relative time
			observation.insert(1, MathExt.round(score, 5)); // score
			this.observations.add(observation);
			if (this.observations.size() % this.saveRate == 0) {
				currentResults.put("history", this.observations);
				System.out.println("Communicating history with: " + this.observations.size() + " entries.");
			}
		}
	}

	@Override
	public void finish(final Map<String, Object> currentResults) {
		System.out.println("Finishing with complete history.");
		currentResults.put("history", this.observations);
	}
}
