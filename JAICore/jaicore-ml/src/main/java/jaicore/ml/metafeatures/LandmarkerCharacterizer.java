package jaicore.ml.metafeatures;

import java.util.ArrayList;
import java.util.Arrays;

import org.openml.webapplication.fantail.dc.Characterizer;
import org.openml.webapplication.fantail.dc.landmarking.GenericLandmarker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.core.Utils;

/**
 * A Characterizer that applies several characterizers to a data set, but does
 * not use any probing.
 * 
 * @author Helena Graf, Mirko
 *
 */
public class LandmarkerCharacterizer extends GlobalCharacterizer {

	private Logger logger = LoggerFactory.getLogger(LandmarkerCharacterizer.class);

	/**
	 * Constructs a new LandmarkerCharacterizer. Construction is the same as for the
	 * {@link ranker.core.metafeatures.GlobalCharacterizer}, except that only
	 * Characterizers that do not use probing are initialized.
	 * 
	 * @throws DatasetCharacterizerInitializationFailedException
	 *             if the characterizer cannot be initialized properly
	 */
	public LandmarkerCharacterizer() throws DatasetCharacterizerInitializationFailedException {
		super();
		logger.trace("Initialize");
	}

	@Override
	protected void initializeCharacterizers() throws Exception {
		Characterizer[] characterizerArray = {
				new GenericLandmarker("CfsSubsetEval_DecisionStump", CP_ASC, 2,
						Utils.splitOptions(PREPROCESSING_PREFIX + CP_DS)),
				new GenericLandmarker("CfsSubsetEval_kNN1N", CP_ASC, 2, Utils.splitOptions(PREPROCESSING_PREFIX + CP_IBK)),
				new GenericLandmarker("CfsSubsetEval_NaiveBayes", CP_ASC, 2,
						Utils.splitOptions(PREPROCESSING_PREFIX + CP_NB)),
				new GenericLandmarker("DecisionStump", CP_DS, 2, null), new GenericLandmarker("kNN1N", CP_IBK, 2, null),
				new GenericLandmarker("NaiveBayes", CP_NB, 2, null), };
		ArrayList<Characterizer> characterizerList = new ArrayList<>(Arrays.asList(characterizerArray));
		StringBuilder zeroes = new StringBuilder();
		zeroes.append("0");
		for (int i = 1; i <= 3; ++i) {
			zeroes.append("0");
			String[] j48Option = { "-C", "." + zeroes.toString() + "1" };
			characterizerList.add(new GenericLandmarker("J48." + zeroes.toString() + "1.", "weka.classifiers.trees.J48",
					2, j48Option));

			String[] repOption = { "-L", "" + i };
			characterizerList
					.add(new GenericLandmarker("REPTreeDepth" + i, "weka.classifiers.trees.REPTree", 2, repOption));

			String[] randomtreeOption = { "-depth", "" + i };
			characterizerList.add(new GenericLandmarker("RandomTreeDepth" + i, "weka.classifiers.trees.RandomTree", 2,
					randomtreeOption));
		}
		characterizers = characterizerList;
	}

}