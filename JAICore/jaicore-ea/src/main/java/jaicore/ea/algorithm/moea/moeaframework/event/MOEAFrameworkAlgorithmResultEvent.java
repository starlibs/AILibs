package jaicore.ea.algorithm.moea.moeaframework.event;

import jaicore.basic.algorithm.events.AAlgorithmEvent;
import jaicore.ea.algorithm.moea.moeaframework.MOEAFrameworkAlgorithmResult;

public class MOEAFrameworkAlgorithmResultEvent extends AAlgorithmEvent {

	private final MOEAFrameworkAlgorithmResult result;

	public MOEAFrameworkAlgorithmResultEvent(final String algorithmId, final MOEAFrameworkAlgorithmResult result) {
		super(algorithmId);
		this.result = result;
	}

	public MOEAFrameworkAlgorithmResult getResult() {
		return this.result;
	}

}
