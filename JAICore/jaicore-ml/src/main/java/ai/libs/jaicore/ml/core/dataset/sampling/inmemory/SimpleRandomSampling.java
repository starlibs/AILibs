package ai.libs.jaicore.ml.core.dataset.sampling.inmemory;

import java.util.Random;

import ai.libs.jaicore.basic.algorithm.events.AlgorithmEvent;
import ai.libs.jaicore.basic.algorithm.exceptions.AlgorithmException;
import ai.libs.jaicore.ml.core.dataset.IDataset;
import ai.libs.jaicore.ml.core.dataset.IInstance;
import ai.libs.jaicore.ml.core.dataset.sampling.SampleElementAddedEvent;

public class SimpleRandomSampling<I extends IInstance> extends ASamplingAlgorithm<I> {

	private Random random;

	private IDataset<I> copyDataset;

	public SimpleRandomSampling(Random random, IDataset<I> input) {
		super(input);
		this.random = random;
	}

	@Override
	public AlgorithmEvent nextWithException() throws AlgorithmException {
		switch (this.getState()) {
		case created:
			this.sample = getInput().createEmpty();
			this.copyDataset = this.getInput().createEmpty();
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
