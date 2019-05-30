package jaicore.ml.tsc.classifier.trees;

import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.aeonbits.owner.ConfigCache;

import jaicore.basic.algorithm.IRandomAlgorithmConfig;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.ml.tsc.classifier.ASimplifiedTSCLearningAlgorithm;
import jaicore.ml.tsc.classifier.trees.TimeSeriesTreeLearningAlgorithm.ITimeSeriesTreeConfig;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;

/**
 * Algorithm to train a time series forest classifier as described in Deng,
 * Houtao et al. "A Time Series Forest for Classification and Feature
 * Extraction." Inf. Sci. 239 (2013): 142-153. Consists of mutliple
 * {@link TimeSeriesTreeClassifier} classifier.
 *
 * @author Julian Lienen
 *
 */
public class TimeSeriesForestLearningAlgorithm extends ASimplifiedTSCLearningAlgorithm<Integer, TimeSeriesForestClassifier> {

	public interface ITimeSeriesForestConfig extends IRandomAlgorithmConfig {
		public static final String K_NUMTREES = "numtrees";
		public static final String K_MAXDEPTH = "maxdepth";
		public static final String K_FEATURECACHING = "featurecaching";

		/**
		 * Number of trees to be trained.
		 */
		@Key(K_NUMTREES)
		@DefaultValue("-1")
		public int numTrees();

		/**
		 * Maximum depth of the trained trees.
		 */
		@Key(K_MAXDEPTH)
		@DefaultValue("-1")
		public int maxDepth();

		/**
		 * Indicator whether feature caching should be used. Usage for datasets with
		 * many attributes is not recommended due to a high number of possible
		 * intervals.
		 */
		@Key(K_FEATURECACHING)
		@DefaultValue("false")
		public boolean useFeatureCaching();
	}

	/**
	 * Constructor for a time series forest training algorithm.
	 */
	public TimeSeriesForestLearningAlgorithm(final ITimeSeriesForestConfig config, final TimeSeriesForestClassifier classifier, final TimeSeriesDataset data) {
		super(config, classifier, data);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerListener(final Object listener) {
		throw new UnsupportedOperationException("The operation to be performed is not supported.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AlgorithmEvent nextWithException() {
		throw new UnsupportedOperationException("The operation to be performed is not supported.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ITimeSeriesForestConfig getConfig() {
		return (ITimeSeriesForestConfig)super.getConfig();
	}

	/**
	 * Training procedure construction a time series tree using the given input
	 * data.
	 * @throws InterruptedException
	 * @throws AlgorithmException
	 */
	@Override
	public TimeSeriesForestClassifier call() throws InterruptedException, AlgorithmException {

		ITimeSeriesForestConfig config = this.getConfig();
		TimeSeriesDataset dataset = this.getInput();

		// Perform Training
		final TimeSeriesTreeClassifier[] trees = new TimeSeriesTreeClassifier[config.numTrees()];
		ExecutorService execService = Executors.newFixedThreadPool(config.cpus());
		@SuppressWarnings("unchecked")
		Future<TimeSeriesTreeClassifier>[] futures = new Future[config.numTrees()];
		for (int i = 0; i < config.numTrees(); i++) {
			ITimeSeriesTreeConfig configOfTree = ConfigCache.getOrCreate(ITimeSeriesTreeConfig.class);
			configOfTree.setProperty(ITimeSeriesTreeConfig.K_MAXDEPTH, "" + config.maxDepth());
			configOfTree.setProperty(ITimeSeriesTreeConfig.K_SEED, "" + config.seed() + i);
			configOfTree.setProperty(ITimeSeriesTreeConfig.K_FEATURECACHING, "" + config.useFeatureCaching());
			TimeSeriesTreeClassifier tst = new TimeSeriesTreeClassifier(configOfTree);
			futures[i] = execService.submit(new Callable<TimeSeriesTreeClassifier>() {
				@Override
				public TimeSeriesTreeClassifier call() throws Exception {
					tst.train(dataset);
					return tst;
				}
			});
		}

		// Wait for completion
		execService.shutdown();
		execService.awaitTermination(this.getTimeout().seconds(), TimeUnit.SECONDS);
		for (int i = 0; i < config.numTrees(); i++) {
			try {
				TimeSeriesTreeClassifier tst = futures[i].get();
				trees[i] = tst;
			} catch (ExecutionException e) {
				throw new AlgorithmException("Could not train time series tree due to training exception: " + e.getMessage());
			}
		}

		this.getClassifier().setTrees(trees);
		return this.getClassifier();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<AlgorithmEvent> iterator() {
		throw new UnsupportedOperationException("The operation to be performed is not supported.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasNext() {
		throw new UnsupportedOperationException("The operation to be performed is not supported.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AlgorithmEvent next() {
		throw new UnsupportedOperationException("The operation to be performed is not supported.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void cancel() {
		throw new UnsupportedOperationException("The operation to be performed is not supported.");
	}

}
