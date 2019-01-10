package jaicore.ml.core.dataset.sampling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.basic.sets.SetUtil.Pair;
import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;

/**
 * Case control sampling. Might be used as sampling algorithm or as subroutine
 * for Local Case Control Sampling
 * 
 * @author Nino Schnitker
 *
 */
public class CaseControlSampling<I extends IInstance> extends ASamplingAlgorithm<I> {

	private Random rand;
	private ArrayList<Pair<I, Double>> probabilityBoundaries;

	/**
	 * Constructor
	 * 
	 * @param rand
	 *            RandomObject for reproducibility
	 */
	public CaseControlSampling(Random rand) {
		this.rand = rand;
	}

	@Override
	public AlgorithmEvent nextWithException() throws AlgorithmException {
		switch (this.getState()) {
		case created:
			this.sample = getInput().createEmpty();

			HashMap<String, Integer> classOccurrences;
			try {
				classOccurrences = countClassOccurrences(this.getInput());
			} catch (ClassNotFoundException e) {
				throw new AlgorithmException(e.getMessage());
			}

			// Count number of classes
			int numberOfClasses = 0;
			for (String clazz : classOccurrences.keySet()) {
				numberOfClasses++;
			}

			// Calculate Boundaries that define which Instances is choose for which random
			// number
			double boundaryOfCurrentInstance = 0.0;
			probabilityBoundaries = new ArrayList<Pair<I, Double>>();
			for (Object instance : this.getInput()) {
				try {
					boundaryOfCurrentInstance = ((double) 1 / classOccurrences
							.get((String) ((I) instance).getTargetValue(Class.forName(new String())).getValue()).intValue())
							/ numberOfClasses;
				} catch (ClassNotFoundException e) {
					throw new AlgorithmException(e.getMessage());
				}
				probabilityBoundaries.add(new Pair<I, Double>((I) instance, new Double(boundaryOfCurrentInstance)));
			}
			return this.activate();
		case active:
			if (this.sample.size() < this.sampleSize) {
				double r = this.rand.nextDouble();
				I choosenInstance = null;
				for (int i = 0; i < probabilityBoundaries.size(); i++) {
					if (probabilityBoundaries.get(i).getY().doubleValue() > r) {
						choosenInstance = probabilityBoundaries.get(i).getX();
					}
				}
				if (choosenInstance == null) {
					choosenInstance = probabilityBoundaries.get(probabilityBoundaries.size() - 1).getX();
				}
				this.sample.add(choosenInstance);
				return new SampleElementAddedEvent();
			} else {
				return this.terminate();
			}
		case inactive:
			if (this.sample.size() < this.sampleSize) {
				throw new RuntimeException("Expected sample size was not reached before termination");
			} else {
				return this.terminate();
			}
		default:
			throw new IllegalStateException("Unknown algorithm state " + this.getState());
		}
	}

	/**
	 * Count occurrences of every class. Needed to determine the probability for all
	 * instances of that class
	 * 
	 * @param dataset
	 *            Dataset of the sample algorithm object
	 * @return HashMap of occurrences
	 * @throws ClassNotFoundException
	 */
	private HashMap<String, Integer> countClassOccurrences(IDataset dataset) throws ClassNotFoundException {
		HashMap<String, Integer> classOccurrences = new HashMap<String, Integer>();
		for (Object instance : dataset) {
			boolean classExists = false;
			for (String clazz : classOccurrences.keySet()) {
				if (clazz.equals(((IInstance) instance).getTargetValue(Class.forName(new String())).getValue())) {
					classExists = true;
				}
			}
			if (classExists) {
				classOccurrences.put(
						(String) ((IInstance) instance).getTargetValue(Class.forName(new String())).getValue(),
						new Integer(classOccurrences.get(
								(String) ((IInstance) instance).getTargetValue(Class.forName(new String())).getValue())
								.intValue() + 1));
			} else {
				classOccurrences.put(
						(String) ((IInstance) instance).getTargetValue(Class.forName(new String())).getValue(),
						new Integer(0));
			}
		}
		return classOccurrences;
	}
}
