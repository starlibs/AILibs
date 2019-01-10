package jaicore.ml.core.dataset.sampling.casecontrol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import jaicore.basic.sets.SetUtil.Pair;
import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.dataset.sampling.ASamplingAlgorithm;
import jaicore.ml.core.dataset.standard.SimpleInstance;

public abstract class CaseControlLikeSampling <I extends IInstance> extends ASamplingAlgorithm<I>{
	
	protected Random rand;
	protected ArrayList<Pair<I, Double>> probabilityBoundaries;
	
	/**
	 * Count occurrences of every class. Needed to determine the probability for all instances of that class
	 * 
	 * @param dataset Dataset of the sample algorithm object
	 * @return HashMap of occurrences
	 * @throws ClassNotFoundException
	 */
	protected HashMap<Object, Integer> countClassOccurrences(IDataset<I> dataset) throws ClassNotFoundException {
		HashMap<Object, Integer> classOccurrences = new HashMap<Object, Integer>();
		for(I instance: dataset) {
			boolean classExists = false;
			for(Object clazz: classOccurrences.keySet()) {
				if(clazz.equals(instance.getTargetValue(new Object().getClass()).getValue())) {
					classExists = true;
				}
			}
			if(classExists) {
				classOccurrences.put(instance.getTargetValue(new Object().getClass()).getValue(), 
						new Integer(classOccurrences.get(instance.getTargetValue(new Object().getClass()).getValue()).intValue() + 1)); 
			}
			else {
				classOccurrences.put(instance.getTargetValue(new Object().getClass()).getValue(), new Integer(0));
			}
		}
		return classOccurrences;
	}
	
	protected ArrayList<Pair<I, Double>> calculateInstanceBoundaries(HashMap<Object, Integer> classOccurrences, int numberOfClasses) {
		double boundaryOfCurrentInstance = 0.0;
		ArrayList<Pair<I, Double>> probabilityBoundaries = new ArrayList<Pair<I, Double>>();
		for(I instance: this.getInput()) {
			boundaryOfCurrentInstance += ((double) 1 / 
					classOccurrences.get(instance.getTargetValue(new Object().getClass()).getValue()).intValue()) /
					numberOfClasses;
			probabilityBoundaries.add(new Pair<I, Double>(instance, new Double(boundaryOfCurrentInstance)));
		}
		return probabilityBoundaries;
	}
}
