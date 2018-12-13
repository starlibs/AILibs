package jaicore.ml.core.dataset.sampling;

import java.util.Random;

import org.apache.commons.math.stat.clustering.KMeansPlusPlusClusterer;

import jaicore.basic.algorithm.AlgorithmEvent;
import jaicore.basic.algorithm.AlgorithmFinishedEvent;
import jaicore.basic.algorithm.AlgorithmInitializedEvent;
import jaicore.basic.algorithm.AlgorithmState;

/**
 * Implementation of a sampling method using kmeans-clustering.
 * 
 * @author jnowack
 *
 */
public class KmeansSampling extends ASamplingAlgorithm{
	

	private Random random;
	private int k;
	
	/** 
	 * @param random Random Object for determining the initial cluster
	 */
	public KmeansSampling(Random random) {
		this.random = random;
		
	}
	
	
	
	@Override
	public AlgorithmEvent nextWithException() throws Exception {
		switch (this.getState()) {
		case created:
			// Initialize variables
			// TODO: create empty dataset
			this.sample = null;
			
			
			this.setState(AlgorithmState.active);
			return new AlgorithmInitializedEvent();			
		case active:
			// If the sample size is not reached yet, add the next datapoints. 
			//For each cluster:
				// if all points have the same attribute
					//add center
				// else add all points
			
		case inactive: {
			if (this.sample.size() < this.sampleSize) {
				throw new Exception("Expected sample size was not reached before termination");
			} else {
				return new AlgorithmFinishedEvent();
			}
		}
		default:
			throw new IllegalStateException("Unknown algorithm state "+ this.getState());
		}
	}
}
