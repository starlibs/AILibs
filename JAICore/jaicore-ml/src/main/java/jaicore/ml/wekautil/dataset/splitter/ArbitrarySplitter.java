package jaicore.ml.wekautil.dataset.splitter;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import jaicore.ml.WekaUtil;
import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.weka.WekaInstancesUtil;
import weka.core.Instances;
import weka.core.UnsupportedAttributeTypeException;

/**
 * Generates a purely random split of the dataset depending on the seed and on the portions provided.
 *
 * @author mwever
 */
public class ArbitrarySplitter implements IDatasetSplitter {

	@Override
	public List<IDataset> split(final IDataset data, final long seed, final double... portions) {
		try {
			Instances wekaData = WekaInstancesUtil.datasetToWekaInstances(data);
			return WekaUtil.realizeSplit(wekaData, WekaUtil.getArbitrarySplit(wekaData, new Random(seed), portions)).stream().map(ds -> WekaInstancesUtil.wekaInstancesToDataset(ds)).collect(Collectors.toList());
		}
		catch (UnsupportedAttributeTypeException e) {
			throw new IllegalArgumentException(e);
		}
	}

}
