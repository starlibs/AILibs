package jaicore.ml.wekautil.dataset.splitter;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import jaicore.ml.WekaUtil;
import weka.core.Instances;

/**
* Class executing pseudo-random splits to enable multilabelcrossvalidation.
*
* @author Helena Graf
*
*/
public class RandomMultilabelCrossValidation implements IMultilabelCrossValidation {

	/**
	 * The name of this class (identifier for db)
	 */
	private static final String name = "PseudoRandom";

	/**
	 * The separator used by this class to separate split portions (from db split representation as string)
	 */
	private static final String splitSeparator = "/";

	@Override
	public Instances getTestSplit(final Instances data, final int seed, final int fold, final String splitInfo) {
		Instances test = this.getFolds(data, seed, splitInfo).get(fold);
		return test;
	}

	@Override
	public Instances getTrainSplit(final Instances data, final int seed, final int fold, final String splitInfo) {
		/* Get all the folds */
		List<Instances> folds = this.getFolds(data, seed, splitInfo);

		/* Copy meta data of original Instances object to the new training instances */
		Instances trainInstances = new Instances(data, 0);

		/* Merge all the training instances */
		for (int i = 0; i < folds.size(); i++) {
			if (i != fold) {
				trainInstances.addAll(folds.get(i));
			}
		}

		return trainInstances;
	}

	/**
	 * Get all of the folds of a split as given by the data, seed and splitInfo
	 *
	 * @param data
	 *            The Instances from which to derive the folds
	 * @param seed
	 *            The seed to use to split
	 * @param splitInfo
	 *            Information of portion sizes of folds
	 * @return All the folds deriving from this split
	 */
	private List<Instances> getFolds(final Instances data, final int seed, final String splitInfo) {
		Collection<Integer>[] instancesInFolds = WekaUtil.getArbitrarySplit(data, new Random(seed), Arrays.stream(splitInfo.split(this.getSplitSeparator())).mapToDouble(Double::parseDouble).toArray());
		List<Instances> folds = WekaUtil.realizeSplit(data, instancesInFolds);
		return folds;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getSplitSeparator() {
		return splitSeparator;
	}
}