package de.upb.crc901.mlplan.search.evaluators;

import de.upb.crc901.mlplan.core.MLPipeline;

public class PipelineMeasurementEvent<V> {
	private MLPipeline pl;
	private V score;

	public PipelineMeasurementEvent(MLPipeline pl, V score) {
		super();
		this.pl = pl;
		this.score = score;
	}

	public MLPipeline getPl() {
		return pl;
	}

	public void setPl(MLPipeline pl) {
		this.pl = pl;
	}

	public V getScore() {
		return score;
	}

	public void setScore(V score) {
		this.score = score;
	}
}
