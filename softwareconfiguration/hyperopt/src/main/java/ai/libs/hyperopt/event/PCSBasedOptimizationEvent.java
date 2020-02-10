package ai.libs.hyperopt.event;

import ai.libs.hasco.model.ComponentInstance;

public class PCSBasedOptimizationEvent implements ScoredSolutionCandidateFoundEvent<ComponentInstance, Double> {
	private ComponentInstance componentInstance;
	private Double score;
	private String algorithmId;
	private long timeStamp;

	public PCSBasedOptimizationEvent(final ComponentInstance componentInstance, final Double score, final String algorithmId) {
		this.componentInstance = componentInstance;
		this.score = score;
		this.algorithmId = algorithmId;
		this.timeStamp = System.currentTimeMillis();
	}

	@Override
	public String getAlgorithmId() {
		return this.algorithmId;
	}

	@Override
	public long getTimestamp() {
		return this.timeStamp;
	}

	@Override
	public Double getScore() {
		return this.score;
	}

	@Override
	public ComponentInstance getSolutionCandidate() {
		return this.componentInstance;
	}

}
