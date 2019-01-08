package jaicore.ea.algorithm.moea.moeaframework.event;

import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.ea.algorithm.moea.moeaframework.MOEAFrameworkAlgorithmResult;

public class MOEAFrameworkAlgorithmResultEvent implements AlgorithmEvent {

	private final MOEAFrameworkAlgorithmResult result;

	public MOEAFrameworkAlgorithmResultEvent(final MOEAFrameworkAlgorithmResult result) {
		this.result = result;
	}

	public MOEAFrameworkAlgorithmResult getResult() {
		return this.result;
	}

}
