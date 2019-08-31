package ai.libs.jaicore.ml.core.filter.sampling.inmemory.casecontrol;

import java.util.HashMap;
import java.util.Random;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;
import org.api4.java.algorithm.events.AlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;

import ai.libs.jaicore.ml.core.filter.sampling.SampleElementAddedEvent;

/**
 * Case control sampling. Might be used as sampling algorithm or as subroutine
 * for Local Case Control Sampling
 *
 * @author Nino Schnitker
 * @param <I>
 *
 */
public class CaseControlSampling<I extends ILabeledInstance, D extends ILabeledDataset<I>> extends CaseControlLikeSampling<I, D> {

	/**
	 * Constructor
	 *
	 * @param rand
	 *            RandomObject for reproducibility
	 */
	public CaseControlSampling(final Random rand, final D input) {
		super(input);
		this.rand = rand;
	}

	@SuppressWarnings("unchecked")
	@Override
	public AlgorithmEvent nextWithException() throws AlgorithmException, InterruptedException {
		switch (this.getState()) {
		case CREATED:
			try {
				this.sample = (D) this.getInput().createEmptyCopy();
			} catch (DatasetCreationException e) {
				throw new AlgorithmException("Could not create a copy of the dataset.", e);
			}

			HashMap<Object, Integer> classOccurrences = this.countClassOccurrences(this.getInput());

			// Count number of classes
			int numberOfClasses = classOccurrences.keySet().size();
			if (this.probabilityBoundaries == null) {
				// Calculate Boundaries that define which Instances is choose for which random
				// number
				this.probabilityBoundaries = this.calculateInstanceBoundaries(classOccurrences, numberOfClasses);
			}
			return this.activate();
		case ACTIVE:
			if (this.sample.size() < this.sampleSize) {
				I choosenInstance = null;
				double r;
				do {
					r = this.rand.nextDouble();
					for (int i = 0; i < this.probabilityBoundaries.size(); i++) {
						if (this.probabilityBoundaries.get(i).getY().doubleValue() > r) {
							choosenInstance = this.probabilityBoundaries.get(i).getX();
							break;
						}
					}
					if (choosenInstance == null) {
						choosenInstance = this.probabilityBoundaries.get(this.probabilityBoundaries.size() - 1).getX();
					}
				} while (this.sample.contains(choosenInstance));
				this.sample.add(choosenInstance);
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
