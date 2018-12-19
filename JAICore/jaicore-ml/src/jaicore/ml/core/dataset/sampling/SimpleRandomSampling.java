package jaicore.ml.core.dataset.sampling;

import java.util.Random;

import jaicore.basic.algorithm.AlgorithmEvent;
import jaicore.basic.algorithm.AlgorithmFinishedEvent;
import jaicore.basic.algorithm.AlgorithmInitializedEvent;
import jaicore.basic.algorithm.AlgorithmState;

public class SimpleRandomSampling extends ASamplingAlgorithm {
	
	private Random random;
	
	public SimpleRandomSampling(Random random) {
		this.random = random;
	}
	
	@Override
	public AlgorithmEvent nextWithException() throws Exception {
		switch (this.getState()) {
		case created:
			this.sample = this.createEmptyDatasetFromInputSchema();
			this.setState(AlgorithmState.active);
			return new AlgorithmInitializedEvent();			
		case active:
			if (this.sample.size() < this.sampleSize) {
				int i = random.nextInt(this.getInput().size());
				this.sample.add(this.getInput().get(i));
				this.getInput().remove(i);
				return new SampleElementAddedEvent();
			} else {
				this.setState(AlgorithmState.inactive);
				return new AlgorithmFinishedEvent();
			}
		case inactive: {
			if (this.sample.size() < this.sampleSize) {
				throw new Exception("Expected sample size was not reached before termination");
			} else {
				return new AlgorithmFinishedEvent();
			}
		}
		default:
			throw new IllegalStateException("Unknown algorithm state "+ this.getState());
		}
	}

}
