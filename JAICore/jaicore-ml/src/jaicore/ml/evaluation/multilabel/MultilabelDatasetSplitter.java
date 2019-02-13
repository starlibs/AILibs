package jaicore.ml.evaluation.multilabel;

import weka.core.Instances;

/**
 * This class provides methods to obtain train and test splits for a given data
 * set and split technique.
 * 
 * @author Helena Graf
 *
 */
public class MultilabelDatasetSplitter {

	private static final String splitTechniqueAndDetailsSeparator = ":";

	/**
	 * Split the Instances object according to the given splitDescription. The
	 * splitDescription is composed of the used technique and details about the
	 * split for the used technique separated by special token obtainable by
	 * {@link #getSplitTechniqueAndDetailsSeparator()}. The returned data will only
	 * contain the testFold. The seed is given to the technique if it uses a seed.
	 * 
	 * @param data
	 *            The data to extract a test fold from
	 * @param splitDescription
	 *            The description of how the split shall be performed
	 * @param testFold
	 *            The number of the fold to be extracted (0...n)
	 * @param seed
	 *            The random seed to be used by the technique
	 * @return The test fold
	 */
	public static Instances getTestSplit(Instances data, String splitDescription, String testFold, String seed) {
		// Check if to be trained on whole data
		if (testFold.equals("-1")) {
			return data;
		}

		// Extract split technique and details
		String[] splitTechniqueAndDetails = splitDescription.split(getSplitTechniqueAndDetailsSeparator());

		// Split according to technique
		switch (splitTechniqueAndDetails[0]) {
		case "mccv": return new RandomMultilabelCrossValidation().getTestSplit(data, Integer.parseInt(seed),
				Integer.parseInt(testFold), splitTechniqueAndDetails[1]);
		case "2cv":
			return new RandomMultilabelCrossValidation().getTestSplit(data, Integer.parseInt(seed),
					Integer.parseInt(testFold),
					new RandomMultilabelCrossValidation().generateSplittingInfo(0.5,0.5));
		case "PseudoRandom":
			return new RandomMultilabelCrossValidation().getTestSplit(data, Integer.parseInt(seed),
					Integer.parseInt(testFold), splitTechniqueAndDetails[1]);
		}

		throw new IllegalArgumentException("Split technique " + splitTechniqueAndDetails[0] + " not supported.");
	}

	/**
	 * Split the Instances object according to the given splitDescription. The
	 * splitDescription is composed of the used technique and details about the
	 * split for the used technique separated by a special token obtainable by
	 * {@link #getSplitTechniqueAndDetailsSeparator()}. The returned data will not
	 * contain the testFold. The seed is given to the technique if it uses a seed.
	 * 
	 * @param data
	 *            The data to extract a train fold from
	 * @param splitDescription
	 *            The description of how the split shall be performed
	 * @param testFold
	 *            The number of the fold to be excluded (0..n)
	 * @param seed
	 *            The random seed to be used by the technique
	 * @return The train fold
	 */
	public static Instances getTrainSplit(Instances data, String splitDescription, String testFold, String seed) {
		// Check if to be tested on whole data
		if (testFold.equals("-1")) {
			return data;
		}

		// Extract split technique and details
		String[] splitTechniqueAndDetails = splitDescription.split(getSplitTechniqueAndDetailsSeparator());

		// Split according to technique
		switch (splitTechniqueAndDetails[0]) {
		case "mccv": return new RandomMultilabelCrossValidation().getTrainSplit(data, Integer.parseInt(seed),
				Integer.parseInt(testFold), splitTechniqueAndDetails[1]);
		case "2cv": return new RandomMultilabelCrossValidation().getTrainSplit(data, Integer.parseInt(seed),
				Integer.parseInt(testFold), new RandomMultilabelCrossValidation().generateSplittingInfo(0.5,0.5));
		case "PseudoRandom":
			return new RandomMultilabelCrossValidation().getTrainSplit(data, Integer.parseInt(seed),
					Integer.parseInt(testFold), splitTechniqueAndDetails[1]);
		}

		throw new IllegalArgumentException("Split technique " + splitTechniqueAndDetails[0] + " not supported.");
	}

	/**
	 * Obtain the token used to separate a split technique and the details about the
	 * split.
	 * 
	 * @return The separator token
	 */
	public static String getSplitTechniqueAndDetailsSeparator() {
		return splitTechniqueAndDetailsSeparator;
	}
}