package ai.libs.jaicore.ml.weka.dataset.splitter;

import java.util.List;
import java.util.Random;

import org.api4.java.ai.ml.core.dataset.splitter.IDatasetSplitter;
import org.api4.java.ai.ml.core.dataset.splitter.SplitFailedException;

import ai.libs.jaicore.ml.weka.WekaUtil;
import ai.libs.jaicore.ml.weka.dataset.IWekaInstances;

/**
 * Generates a purely random split of the dataset depending on the seed and on the portions provided.
 *
 * @author mwever
 */
public class ArbitrarySplitter implements IDatasetSplitter<IWekaInstances> {

	private final Random random;
	private final double portions;


	public ArbitrarySplitter(final Random random, final double portions) {
		super();
		this.random = random;
		this.portions = portions;
	}

	@Override
	public List<IWekaInstances> split(final IWekaInstances data) throws SplitFailedException, InterruptedException {
		return WekaUtil.realizeSplit(data, WekaUtil.getArbitrarySplit(data, this.random, this.portions));
	}

	@Override
	public int getNumberOfFoldsPerSplit() {
		return 2;
	}
}
