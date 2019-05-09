package jaicore.ml.weka.dataset.splitter;

import weka.core.Instances;

/**
 * Represents an algorithm that realizes a split of a given multilabel instances in folds, given a seed, custom information about the split represented as a string, and the fold that is left out for testing.
 *
 * @author Helena Graf
 *
 */
public interface IMultilabelCrossValidation {

	/**
	 * Gets a test split from the given data based on the seed. The given fold is the test fold.
	 *
	 * @param data
	 *            The data from which to derive the split
	 * @param seed
	 *            The seed possibly used by the implementation class to derive the split
	 * @param fold
	 *            The number of the fold which is the testing fold
	 * @param splitInfo
	 *            Information about the split for the class executing the split (e.g. portion sizes of folds)
	 * @return A test Instances derived from the given instances
	 */
	public Instances getTestSplit(Instances data, int seed, int fold, String splitInfo);

	/**
	 * Gets a train split from the given data based on the seed. The given fold is the test fold, which is the left out fold for this case.
	 *
	 * @param data
	 *            The data from which to derive the split
	 * @param seed
	 *            The seed possibly used by the implementation class to derive the split
	 * @param fold
	 *            The number of the fold which is the testing fold (left out fold)
	 * @param splitInfo
	 *            Information about the split for the class executing the split (e.g. portion sized of folds)
	 * @return A train Instances derived from the given instances
	 */
	public Instances getTrainSplit(Instances data, int seed, int fold, String splitInfo);

	/**
	 * Generate a String that represents a split of a data set into portions from the given portions sizes (must add up to <1).
	 *
	 * @param portions
	 *            The portions sizes of the split
	 * @return The String representation of the split
	 */
	public default String generateSplittingString(final double... portions) {
		StringBuilder builder = new StringBuilder();
		builder.append(getName());
		builder.append(MultilabelDatasetSplitter.getSplitTechniqueAndDetailsSeparator());
		builder.append(generateSplittingInfo(portions));
		return builder.toString();
	}

	/**
	 * Generate a string representation that represents only the split info part of the split string.
	 *
	 * @param portions
	 *            portions sized of the split
	 * @return String representation of the splitInfo part of the split
	 */
	public default String generateSplittingInfo(final double... portions) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < portions.length - 1; i++) {
			builder.append(portions[i]);
			builder.append(getSplitSeparator());
		}
		builder.append(portions[portions.length - 1]);
		return builder.toString();
	}

	/**
	 * Get the name of the implementing multilabel cross validation technique.
	 *
	 * @return The name of the technique
	 */
	public String getName();

	/**
	 * Get the separator used to separate single portions of a split in a given splitInfo. E.g. if the splitInfo represents data portions, it could be 0.7/0.3 with "/" being the split separator.
	 *
	 * @return The slit separator used by the specific implementation of this interface
	 */
	public String getSplitSeparator();
}