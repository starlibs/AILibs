package ai.libs.jaicore.ml.core.filter.sampling.inmemory.casecontrol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;

import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.ASamplingAlgorithm;

public abstract class CaseControlLikeSampling<I extends ILabeledInstance, D extends ILabeledDataset<I>> extends ASamplingAlgorithm<D> {

	protected Random rand;
	protected List<Pair<I, Double>> probabilityBoundaries = null;

	protected CaseControlLikeSampling(final D input) {
		super(input);
	}

	public List<Pair<I, Double>> getProbabilityBoundaries() {
		return this.probabilityBoundaries;
	}

	public void setProbabilityBoundaries(final List<Pair<I, Double>> probabilityBoundaries) {
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
	protected HashMap<Object, Integer> countClassOccurrences(final D dataset) {
		HashMap<Object, Integer> classOccurrences = new HashMap<>();
		for (I instance : dataset) {
			boolean classExists = false;
			for (Object clazz : classOccurrences.keySet()) {
				if (clazz.equals(instance.getLabel())) {
					classExists = true;
				}
			}
			if (classExists) {
				classOccurrences.put(instance.getLabel(), classOccurrences.get(instance.getLabel()).intValue() + 1);
			} else {
				classOccurrences.put(instance.getLabel(), 0);
			}
		}
		return classOccurrences;
	}

	protected List<Pair<I, Double>> calculateInstanceBoundaries(final HashMap<Object, Integer> classOccurrences, final int numberOfClasses) {
		double boundaryOfCurrentInstance = 0.0;
		List<Pair<I, Double>> boundaries = new ArrayList<>();
		for (I instance : this.getInput()) {
			boundaryOfCurrentInstance += ((double) 1 / classOccurrences.get(instance.getLabel()).intValue()) / numberOfClasses;
			boundaries.add(new Pair<I, Double>(instance, boundaryOfCurrentInstance));
		}
		return boundaries;
	}
}
