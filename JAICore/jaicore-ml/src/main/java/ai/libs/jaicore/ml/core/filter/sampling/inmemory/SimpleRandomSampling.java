package ai.libs.jaicore.ml.core.filter.sampling.inmemory;

import java.util.Random;

import org.api4.java.ai.ml.core.dataset.IDataset;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;
import org.api4.java.algorithm.events.AlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;

import ai.libs.jaicore.ml.core.filter.sampling.SampleElementAddedEvent;

public class SimpleRandomSampling<D extends IDataset<?>> extends ASamplingAlgorithm<D> {

	private Random random;

	public SimpleRandomSampling(final Random random, final D input) {
		super(input);
		this.random = random;
	}

	@SuppressWarnings("unchecked")
	@Override
	public AlgorithmEvent nextWithException() throws AlgorithmException, InterruptedException {
		switch (this.getState()) {
		case CREATED:
			try {
				this.sample = (D)this.getInput().createCopy();
			} catch (DatasetCreationException e) {
				throw new AlgorithmException("Could not create a copy of the dataset.", e);
			}
			return this.activate();
		case ACTIVE:
			if (this.sample.size() > this.sampleSize) {
				int i = this.random.nextInt(this.sample.size());
				this.sample.remove(i);
				return new SampleElementAddedEvent(this.getId());
			} else {
				return this.terminate();
			}
		case INACTIVE:
			this.doInactiveStep();
			break;
		default:
			throw new IllegalStateException("Unknown algorithm state " + this.getState());
		}
		return null;
	}

}
