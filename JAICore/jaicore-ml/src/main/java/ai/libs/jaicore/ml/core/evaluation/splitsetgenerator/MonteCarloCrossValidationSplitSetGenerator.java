package ai.libs.jaicore.ml.core.evaluation.splitsetgenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.api4.java.ai.ml.classification.execution.IDatasetSplitSet;
import org.api4.java.ai.ml.classification.execution.IDatasetSplitSetGenerator;
import org.api4.java.ai.ml.core.dataset.splitter.IRandomDatasetSplitter;
import org.api4.java.ai.ml.core.dataset.splitter.SplitFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.common.control.ILoggingCustomizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.ml.core.dataset.DatasetSplitSet;

/**
 * A DatasetSplitSetGenerator that create k independent splits of the given dataset.
 * The type of split can be configured using the IDatasetSplitter.
 * The parameter k is configured over the variable "repeats"
 *
 * @author fmohr
 *
 */
public class MonteCarloCrossValidationSplitSetGenerator<D extends ILabeledDataset<?>> implements IDatasetSplitSetGenerator<D>, ILoggingCustomizable {

	private Logger logger = LoggerFactory.getLogger(MonteCarloCrossValidationSplitSetGenerator.class);
	private final IRandomDatasetSplitter<D> datasetSplitter;
	private final int repeats;
	private final long seed;

	public MonteCarloCrossValidationSplitSetGenerator(final IRandomDatasetSplitter<D> datasetSplitter, final int repeats, final Random random) {
		super();
		this.datasetSplitter = datasetSplitter;
		this.repeats = repeats;
		this.seed = random.nextLong(); // we do not want to use the random object any further, because the randomness inside should not be affected by outer operations
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger.info("Switching logger of {} from {} to {}", this, this.logger.getName(), name);
		this.logger = LoggerFactory.getLogger(name);
		this.logger.info("Switched logger of {} to {}", this, name);
	}

	@Override
	public int getNumSplitsPerSet() {
		return this.repeats;
	}

	@Override
	public int getNumFoldsPerSplit() {
		return this.datasetSplitter.getNumberOfFoldsPerSplit();
	}

	@Override
	public IDatasetSplitSet<D> nextSplitSet(final D data) throws InterruptedException, SplitFailedException {
		if (Thread.interrupted()) { // clear the interrupted field. This is Java a general convention when an
			// InterruptedException is thrown (see Java documentation for details)
			this.logger.info("MCCV has been interrupted, leaving MCCV.");
			throw new InterruptedException("MCCV has been interrupted.");
		}

		List<List<D>> splits = new ArrayList<>(this.repeats);
		for (int i = 0; i < this.repeats; i++) {
			splits.add(this.datasetSplitter.split(data, new Random(this.seed + i)));
		}
		return new DatasetSplitSet<>(splits);
	}
}
