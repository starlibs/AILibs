package ai.libs.jaicore.ml.weka.dataset.splitter;

import java.util.List;

import org.api4.java.ai.ml.core.dataset.splitter.SplitFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;

/**
 * Makes use of the WekaUtil to split the data into a class-oriented stratified split preserving the class distribution.
 *
 * @author mwever
 */
public class MulticlassClassStratifiedSplitter<I extends ILabeledInstance, D extends ILabeledDataset<I>> implements IDatasetSplitter<I, D> {

	@Override
	public List<D> split(final D data, final long seed, final double portions) throws SplitFailedException, InterruptedException {
//		return WekaUtil.getStratifiedSplit(data, seed, portions);
		throw new UnsupportedOperationException("Not supported operation");
	}

}
