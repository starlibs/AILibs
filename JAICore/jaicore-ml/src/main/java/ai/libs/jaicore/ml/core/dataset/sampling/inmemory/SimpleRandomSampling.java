package ai.libs.jaicore.ml.core.dataset.sampling.inmemory;

import java.util.Random;

import ai.libs.jaicore.basic.algorithm.events.AlgorithmEvent;
import ai.libs.jaicore.basic.algorithm.exceptions.AlgorithmException;
import ai.libs.jaicore.ml.core.dataset.DatasetCreationException;
import ai.libs.jaicore.ml.core.dataset.IOrderedDataset;
import ai.libs.jaicore.ml.core.dataset.sampling.SampleElementAddedEvent;

public class SimpleRandomSampling<I, D extends IOrderedDataset<I>> extends ASamplingAlgorithm<D> {

	private Random random;

	private D copyDataset;

	public SimpleRandomSampling(Random random, D input) {
		super(input);
		this.random = random;
	}

	@Override
	public AlgorithmEvent nextWithException() throws AlgorithmException {
		switch (this.getState()) {
		case CREATED:
			try {
				this.sample = (D) getInput().createEmpty();
				this.copyDataset = (D) this.getInput().createEmpty();
				this.copyDataset.addAll(this.getInput());
			} catch (DatasetCreationException e) {
				throw new AlgorithmException(e, "Could not create a copy of the dataset.");
			}
			return this.activate();
		case ACTIVE:
			if (this.sample.size() < this.sampleSize) {
				int i = random.nextInt(this.copyDataset.size());
				this.sample.add(this.copyDataset.get(i));
				this.copyDataset.remove(i);
				return new SampleElementAddedEvent(getId());
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
