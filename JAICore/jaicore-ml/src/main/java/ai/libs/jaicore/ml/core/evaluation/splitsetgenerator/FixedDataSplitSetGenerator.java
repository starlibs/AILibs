package ai.libs.jaicore.ml.core.evaluation.splitsetgenerator;

import org.api4.java.ai.ml.core.dataset.IDataset;
import org.api4.java.ai.ml.core.dataset.splitter.SplitFailedException;
import org.api4.java.ai.ml.core.evaluation.execution.IDatasetSplitSet;
import org.api4.java.ai.ml.core.evaluation.execution.IDatasetSplitSetGenerator;
import org.api4.java.ai.ml.core.evaluation.execution.IFixedDatasetSplitSetGenerator;
import org.api4.java.common.control.ILoggingCustomizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an IDatasetSplitSetGenerator that produces splits for one initially fixed dataset.
 *
 * It can be used as a split generator in a context where the data are not passed along the usage of the splitter.
 *
 * @author Felix Mohr
 *
 * @param <D>
 */
public class FixedDataSplitSetGenerator<D extends IDataset<?>> implements IFixedDatasetSplitSetGenerator<D>, ILoggingCustomizable {
	private final D data;
	private final IDatasetSplitSetGenerator<D> generator;
	private Logger logger = LoggerFactory.getLogger(FixedDataSplitSetGenerator.class);

	public FixedDataSplitSetGenerator(final D data, final IDatasetSplitSetGenerator<D> generator) {
		super();
		this.data = data;
		this.generator = generator;
	}

	@Override
	public int getNumSplitsPerSet() {
		return this.generator.getNumFoldsPerSplit();
	}

	@Override
	public int getNumFoldsPerSplit() {
		return this.generator.getNumFoldsPerSplit();
	}

	@Override
	public IDatasetSplitSet<D> nextSplitSet() throws InterruptedException, SplitFailedException {
		return this.generator.nextSplitSet(this.data);
	}

	@Override
	public D getDataset() {
		return this.data;
	}

	@Override
	public String toString() {
		return "FixedDataSplitSetGenerator [data=" + this.data + ", generator=" + this.generator + "]";
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
		if (this.generator instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) this.generator).setLoggerName(name + ".splitgen");
			this.logger.info("Setting logger of base split generator {} to {}.splitgen", this.generator.getClass().getName(), name);
		}
		else {
			this.logger.info("Base split generator {} is not configurable for logging, so not configuring it.", this.generator.getClass().getName());
		}
	}
}
