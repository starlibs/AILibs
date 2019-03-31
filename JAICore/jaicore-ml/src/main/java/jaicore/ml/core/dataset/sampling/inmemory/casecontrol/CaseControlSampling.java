package jaicore.ml.core.dataset.sampling.inmemory.casecontrol;

import java.util.HashMap;
import java.util.Random;
import jaicore.ml.core.dataset.*;
import jaicore.ml.core.dataset.sampling.SampleElementAddedEvent;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;

/**
 * Case control sampling. Might be used as sampling algorithm or as subroutine
 * for Local Case Control Sampling
 * 
 * @author Nino Schnitker
 * @param <I>
 *
 */
public class CaseControlSampling<I extends IInstance> extends CaseControlLikeSampling<I> {

	/**
	 * Constructor
	 * 
	 * @param rand
	 *            RandomObject for reproducibility
	 */
	public CaseControlSampling(Random rand, IDataset<I> input) {
		super(input);
		this.rand = rand;
	}

	@Override
	public AlgorithmEvent nextWithException() throws AlgorithmException {
		switch (this.getState()) {
		case created:
			this.sample = getInput().createEmpty();

			HashMap<Object, Integer> classOccurrences = countClassOccurrences(this.getInput());

			// Count number of classes
			int numberOfClasses = classOccurrences.keySet().size();
			if (probabilityBoundaries == null) {
				// Calculate Boundaries that define which Instances is choose for which random
				// number
				probabilityBoundaries = calculateInstanceBoundaries(classOccurrences, numberOfClasses);
			}
			return this.activate();
		case active:
			if (this.sample.size() < this.sampleSize) {
				I choosenInstance = null;
				double r;
				do {
					r = this.rand.nextDouble();
					for (int i = 0; i < probabilityBoundaries.size(); i++) {
						if (probabilityBoundaries.get(i).getY().doubleValue() > r) {
							choosenInstance = probabilityBoundaries.get(i).getX();
							break;
						}
					}
					if (choosenInstance == null) {
						choosenInstance = probabilityBoundaries.get(probabilityBoundaries.size() - 1).getX();
					}
				} while (this.sample.contains(choosenInstance));
				this.sample.add(choosenInstance);
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
