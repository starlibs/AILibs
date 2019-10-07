package ai.libs.jaicore.ml.weka.dataset.splitter;

import java.util.List;
import java.util.Random;

import ai.libs.jaicore.ml.weka.WekaUtil;
import ai.libs.jaicore.ml.weka.dataset.WekaInstance;
import ai.libs.jaicore.ml.weka.dataset.WekaInstances;

/**
 * Generates a purely random split of the dataset depending on the seed and on the portions provided.
 *
 * @author mwever
 */
public class ArbitrarySplitter implements IDatasetSplitter<WekaInstance, WekaInstances> {

	@Override
	public List<WekaInstances> split(final WekaInstances data, final long seed, final double portions) {
		return WekaUtil.realizeSplit(data, WekaUtil.getArbitrarySplit(data, new Random(seed), portions));
	}

}
