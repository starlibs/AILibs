package ai.libs.jaicore.ea.algorithm.moea.moeaframework.event;

import org.api4.java.algorithm.IAlgorithm;

import ai.libs.jaicore.basic.algorithm.AAlgorithmEvent;
import ai.libs.jaicore.ea.algorithm.moea.moeaframework.MOEAFrameworkAlgorithmResult;

public class MOEAFrameworkAlgorithmResultEvent extends AAlgorithmEvent {

	private final MOEAFrameworkAlgorithmResult result;

	public MOEAFrameworkAlgorithmResultEvent(final IAlgorithm<?, ?> algorithm, final MOEAFrameworkAlgorithmResult result) {
		super(algorithm);
		this.result = result;
	}

	public MOEAFrameworkAlgorithmResult getResult() {
		return this.result;
	}

}
