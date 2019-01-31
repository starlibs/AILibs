package de.upb.crc901.mlplan.metamining.dyadranking;

import org.openml.webapplication.fantail.dc.Characterizer;

import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.WekaMLPlanWekaClassifier;
import jaicore.ml.dyadranking.algorithm.ADyadRanker;
import jaicore.ml.metafeatures.GlobalCharacterizer;
import jaicore.ml.metafeatures.NoProbingCharacterizer;

/**
 * A configuration class that contains configurable variables for using ML-Plan
 * with best-first search and a dyad-ranked OPEN list instead of random
 * completions. Also is used to configure ML-Plan to behave this way.
 * 
 * @author Helena Graf
 *
 */
public class WEKADyadRankedOPENListConfig {
	
	/** the characterizer used to characterize new datasets, must produce dataset meta data of the same format the dyad ranker is trained with */
	//Characterizer characterizer = new GlobalCharacterizer(); TODO why does this not work?
	
	// TODO possibly also need some sort of configuration, or, instead of the ranker some
	// file that is loaded with a trained dyad ranker.
	ADyadRanker ranker;


	/**
	 * Configure WEKA ML-Plan to use the best-first search with dyad ranking instead
	 * of random completions.
	 * 
	 * @param mlPlan
	 *            the ML-Plan classifier to configure this way
	 */
	public void configureMLPlan(WekaMLPlanWekaClassifier mlPlan) {
		//TODO
	}
}
