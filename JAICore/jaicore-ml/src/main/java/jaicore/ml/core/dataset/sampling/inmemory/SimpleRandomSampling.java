package jaicore.ml.core.dataset.sampling.inmemory;

import java.util.Random;

import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.ml.core.dataset.IOrderedDataset;
import jaicore.ml.core.dataset.sampling.SampleElementAddedEvent;

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
		case created:
			this.sample = (D)getInput().createEmpty();
			this.copyDataset = (D)this.getInput().createEmpty();
			this.copyDataset.addAll(this.getInput());
			return this.activate();
		case active:
			if (this.sample.size() < this.sampleSize) {
				int i = random.nextInt(this.copyDataset.size());
				this.sample.add(this.copyDataset.get(i));
				this.copyDataset.remove(i);
				return new SampleElementAddedEvent(getId());
			} else {
				return this.terminate();
			}
		case inactive:
			this.doInactiveStep();
			break;
		default:
			throw new IllegalStateException("Unknown algorithm state " + this.getState());
		}
		return null;
	}

}
