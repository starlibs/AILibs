package ai.libs.hasco.pcsbasedoptimization;

import ai.libs.hasco.model.ComponentInstance;
import ai.libs.jaicore.basic.algorithm.events.ScoredSolutionCandidateFoundEvent;

public class PCSBasedOptimizationEvent implements ScoredSolutionCandidateFoundEvent<ComponentInstance, Double> {
	private ComponentInstance componentInstance;
	private Double score;
	private String algorithmId;
	private long timeStamp;

	public PCSBasedOptimizationEvent(ComponentInstance componentInstance, Double score, String algorithmId) {
		this.componentInstance = componentInstance;
		this.score = score;
		this.algorithmId = algorithmId;
		this.timeStamp = System.currentTimeMillis();
	}

	@Override
	public String getAlgorithmId() {
		return algorithmId;
	}

	@Override
	public long getTimestamp() {
		return timeStamp;
	}

	@Override
	public Double getScore() {
		return score;
	}

	@Override
	public ComponentInstance getSolutionCandidate() {
		return componentInstance;
	}

}
