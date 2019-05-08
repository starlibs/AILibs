package jaicore.ml.wekautil.dataset.splitter;

import java.util.List;
import java.util.stream.Collectors;

import jaicore.ml.WekaUtil;
import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.dataset.standard.SimpleInstance;
import jaicore.ml.core.dataset.weka.WekaInstancesUtil;
import weka.core.UnsupportedAttributeTypeException;

/**
 * Makes use of the WekaUtil to split the data into a class-oriented stratified split preserving the class distribution.
 *
 * @author mwever
 */
public class MulticlassClassStratifiedSplitter implements IDatasetSplitter<SimpleInstance> {

	@Override
	public <I extends IInstance> List<IDataset<SimpleInstance>> split(final IDataset<I> data, final long seed, final double... portions) {
		try {
			return WekaUtil.getStratifiedSplit(WekaInstancesUtil.datasetToWekaInstances(data), seed, portions).stream().map(ds -> WekaInstancesUtil.wekaInstancesToDataset(ds)).collect(Collectors.toList());
		} catch (UnsupportedAttributeTypeException e) {
			throw new IllegalArgumentException(e);
		}
	}

}
