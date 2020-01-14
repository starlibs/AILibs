package ai.libs.jaicore.ml.core.filter.sampling.inmemory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.api4.java.ai.ml.core.dataset.IDataset;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;
import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;

import ai.libs.jaicore.ml.core.dataset.DatasetDeriver;

public class SimpleRandomSampling<D extends IDataset<?>> extends ASamplingAlgorithm<D> {

	private Random random;
	private Collection<Integer> chosenIndices;
	private boolean isLargeSample;
	private int numberOfLastSample = 0;

	public SimpleRandomSampling(final Random random, final D input) {
		super(input);
		this.random = random;
	}

	@Override
	public IAlgorithmEvent nextWithException() throws AlgorithmException, InterruptedException, AlgorithmTimeoutedException, AlgorithmExecutionCanceledException {
		int n = this.getInput().size();
		switch (this.getState()) {
		case CREATED:
			this.isLargeSample = this.sampleSize * 1.0 / n > .3; // if the sample contains more than 30%, we consider it large
			return this.activate();
		case ACTIVE:

			/* if we have a large sample, we just create a shuffled list of indices, which will be the chosen elements */
			if (this.isLargeSample) {
				this.chosenIndices = new ArrayList<>(n);
				for (int i = 0; i < n; i++) {
					if (i % 100 == 0) {
						this.checkAndConductTermination();
					}
					this.chosenIndices.add(i);
				}
				Collections.shuffle((List<Integer>) this.chosenIndices, this.random);
				this.chosenIndices = ((List<Integer>) this.chosenIndices).subList(0, this.sampleSize);
			}

			/* if we have a small sample, randomly draw unchosen elements */
			else {
				this.chosenIndices = new HashSet<>();
				while (this.numberOfLastSample < this.sampleSize) {
					int i;
					if (this.numberOfLastSample % 100 == 0) {
						this.checkAndConductTermination();
					}
					do {
						i = this.random.nextInt(this.sampleSize);
					} while (this.chosenIndices.contains(i));
					this.chosenIndices.add(i);
					this.numberOfLastSample ++;
				}
			}

			/* create sample */
			DatasetDeriver<D> deriver = new DatasetDeriver<>(this.getInput());
			deriver.addIndices(this.chosenIndices);
			try {
				this.sample = deriver.build();
			} catch (DatasetCreationException e) {
				throw new AlgorithmException("Could not create sample.", e);
			}
			if (this.chosenIndices == null) {
				throw new IllegalStateException("Chosen indices must not be null!");
			}
			return this.terminate();
		case INACTIVE:
			this.doInactiveStep();
			break;
		default:
			throw new IllegalStateException("Unknown algorithm state " + this.getState());
		}
		return null;
	}

	public Collection<Integer> getChosenIndices() {
		if (this.chosenIndices == null) {
			throw new IllegalStateException("The algorithm has not run, so no indices have been chosen!");
		}
		return Collections.unmodifiableCollection(this.chosenIndices);
	}

}
