package jaicore.ml.weka.dataset.splitter;

import java.util.List;
import java.util.Random;

import jaicore.ml.WekaUtil;
import weka.core.Instances;

/**
 * Generates a purely random split of the dataset depending on the seed and on the portions provided.
 *
 * @author mwever
 */
public class ArbitrarySplitter implements IDatasetSplitter {

	@Override
	public List<Instances> split(final Instances data, final long seed, final double... portions) {
		return WekaUtil.realizeSplit(data, WekaUtil.getArbitrarySplit(data, new Random(seed), portions));
	}

}
