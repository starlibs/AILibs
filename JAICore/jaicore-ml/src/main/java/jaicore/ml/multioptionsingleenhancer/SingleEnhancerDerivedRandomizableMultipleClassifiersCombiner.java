package jaicore.ml.multioptionsingleenhancer;

import weka.classifiers.Classifier;
import weka.classifiers.RandomizableMultipleClassifiersCombiner;
import weka.core.Instances;
import weka.core.PartitionGenerator;
import weka.core.Randomizable;
import weka.core.WeightedInstancesHandler;
/**
 * 
 * @author nino
 *
 * This class generalizes the ExtendedSingleEnhancer and provides function that they require
 */
public abstract class SingleEnhancerDerivedRandomizableMultipleClassifiersCombiner extends RandomizableMultipleClassifiersCombiner{
	
	/** for serialization */
	private static final long serialVersionUID = -815180860807481184L;

	/**
	 * Checks if any element of the elements of a given array is an instance of WeightedInstancesHandler
	 * 
	 * @param classifiers an array of classifiers
	 * @return boolean if any of the elements of the given array is an instance of WeightedInstancesHandler
	 */
	protected boolean checkIfAnyClassifierIsInstanceOfWeightedInstancesHandler(Classifier[] classifiers) {
		boolean isAnyBaseClassifierInstanceOfWeightedInstancesHandler = false;
		  
		for(Classifier baseClassifier: classifiers) {
			if(baseClassifier instanceof WeightedInstancesHandler) {
				isAnyBaseClassifierInstanceOfWeightedInstancesHandler = true;
			}
		}
		return isAnyBaseClassifierInstanceOfWeightedInstancesHandler;
	}
	
	/**
	 * Checks if any element of the elements of a given array is an instance of PartitionGenerator
	 * 
	 * @param classifiers an array of classifiers
	 * @return boolean if any of the elements of the given array is an instance of WeightedInstancesHandler
	 */
	protected boolean checkIfAllClassifierIsInstanceOfPartitionGenerator(Classifier[] classifiers) {
		boolean isAllBaseClassifierInstanceOfPartitionGenerator = true;
		  
		for(Classifier baseClassifier: classifiers) {
			if(!(baseClassifier instanceof PartitionGenerator)) {
				isAllBaseClassifierInstanceOfPartitionGenerator = false;
			}
		}
		return isAllBaseClassifierInstanceOfPartitionGenerator;
	}

	@Override
	public void buildClassifier(Instances data) throws Exception {
		
	}

}
