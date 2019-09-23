package ai.libs.jaicore.ml.weka.dataset.splitter;

import java.util.List;

import org.api4.java.ai.ml.classification.singlelabel.dataset.ISingleLabelClassificationDataset;
import org.api4.java.ai.ml.classification.singlelabel.dataset.ISingleLabelClassificationInstance;

import ai.libs.jaicore.ml.weka.WekaUtil;
import weka.core.Instances;

/**
 * Makes use of the WekaUtil to split the data into a class-oriented stratified split preserving the class distribution.
 *
 * @author mwever
 */
public class MulticlassClassStratifiedSplitter<I extends ISingleLabelClassificationInstance, D extends ISingleLabelClassificationDataset> implements IDatasetSplitter<I, D> {

	@Override
	public List<Instances> split(final D data, final long seed, final double portions) throws SplitFailedException, InterruptedException {
		return WekaUtil.getStratifiedSplit(data, seed, portions);
	}

}
