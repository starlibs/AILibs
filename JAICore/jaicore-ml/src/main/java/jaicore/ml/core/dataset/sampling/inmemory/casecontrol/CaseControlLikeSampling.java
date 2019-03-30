package jaicore.ml.core.dataset.sampling.inmemory.casecontrol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import jaicore.basic.sets.SetUtil.Pair;
import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.dataset.sampling.inmemory.ASamplingAlgorithm;

public abstract class CaseControlLikeSampling<I extends IInstance> extends ASamplingAlgorithm<I> {

	protected Random rand;
	protected List<Pair<I, Double>> probabilityBoundaries = null;

	protected CaseControlLikeSampling(IDataset<I> input) {
		super(input);
	}

	public List<Pair<I, Double>> getProbabilityBoundaries() {
		return probabilityBoundaries;
	}

	public void setProbabilityBoundaries(List<Pair<I, Double>> probabilityBoundaries) {
		this.probabilityBoundaries = probabilityBoundaries;
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
	protected HashMap<Object, Integer> countClassOccurrences(IDataset<I> dataset) {
		HashMap<Object, Integer> classOccurrences = new HashMap<>();
		for (I instance : dataset) {
			boolean classExists = false;
			for (Object clazz : classOccurrences.keySet()) {
				if (clazz.equals(instance.getTargetValue(Object.class).getValue())) {
					classExists = true;
				}
			}
			if (classExists) {
				classOccurrences.put(instance.getTargetValue(Object.class).getValue(),
						classOccurrences.get(instance.getTargetValue(Object.class).getValue()).intValue() + 1);
			} else {
				classOccurrences.put(instance.getTargetValue(Object.class).getValue(), 0);
			}
		}
		return classOccurrences;
	}

	protected List<Pair<I, Double>> calculateInstanceBoundaries(HashMap<Object, Integer> classOccurrences,
			int numberOfClasses) {
		double boundaryOfCurrentInstance = 0.0;
		List<Pair<I, Double>> boundaries = new ArrayList<>();
		for (I instance : this.getInput()) {
			boundaryOfCurrentInstance += ((double) 1
					/ classOccurrences.get(instance.getTargetValue(Object.class).getValue()).intValue())
					/ numberOfClasses;
			boundaries.add(new Pair<I, Double>(instance, boundaryOfCurrentInstance));
		}
		return boundaries;
	}
}
