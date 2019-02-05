package jaicore.ml.metafeatures;

import java.util.ArrayList;
import java.util.Arrays;

import org.openml.webapplication.fantail.dc.Characterizer;
import org.openml.webapplication.fantail.dc.landmarking.GenericLandmarker;

import weka.core.Utils;

/**
 * A Characterizer that applies several characterizers to a data set, but does
 * not use any probing.
 * 
 * @author Helena Graf, Mirko
 *
 */
public class LandmarkerCharacterizer extends GlobalCharacterizer {

	/**
	 * Constructs a new LandmarkerCharacterizer. Construction is the same as for the
	 * {@link ranker.core.metafeatures.GlobalCharacterizer}, except that only
	 * Characterizers that do not use probing are initialized.
	 * 
	 * @throws Exception
	 */
	public LandmarkerCharacterizer() throws Exception {
		super();
	}

	@Override
	protected void initializeCharacterizers() throws Exception {
		Characterizer[] characterizerArray = {
				new GenericLandmarker("CfsSubsetEval_DecisionStump", cpASC, 2,
						Utils.splitOptions(preprocessingPrefix + cpDS)),
				new GenericLandmarker("CfsSubsetEval_kNN1N", cpASC, 2, Utils.splitOptions(preprocessingPrefix + cp1NN)),
				new GenericLandmarker("CfsSubsetEval_NaiveBayes", cpASC, 2,
						Utils.splitOptions(preprocessingPrefix + cpNB)),
				new GenericLandmarker("DecisionStump", cpDS, 2, null),
				new GenericLandmarker("kNN1N", cp1NN, 2, null),
				new GenericLandmarker("NaiveBayes", cpNB, 2, null), };
		characterizers = new ArrayList<>(Arrays.asList(characterizerArray));
	}

}