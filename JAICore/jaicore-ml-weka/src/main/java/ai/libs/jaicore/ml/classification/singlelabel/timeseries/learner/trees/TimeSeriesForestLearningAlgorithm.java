package ai.libs.jaicore.ml.classification.singlelabel.timeseries.learner.trees;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.aeonbits.owner.ConfigCache;
import org.api4.java.algorithm.exceptions.AlgorithmException;

import ai.libs.jaicore.basic.IOwnerBasedRandomizedAlgorithmConfig;
import ai.libs.jaicore.ml.classification.singlelabel.timeseries.dataset.TimeSeriesDataset2;
import ai.libs.jaicore.ml.classification.singlelabel.timeseries.learner.ASimplifiedTSCLearningAlgorithm;
import ai.libs.jaicore.ml.classification.singlelabel.timeseries.learner.trees.TimeSeriesTreeLearningAlgorithm.ITimeSeriesTreeConfig;

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

	public interface ITimeSeriesForestConfig extends IOwnerBasedRandomizedAlgorithmConfig {
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
	public TimeSeriesForestLearningAlgorithm(final ITimeSeriesForestConfig config, final TimeSeriesForestClassifier classifier, final TimeSeriesDataset2 data) {
		super(config, classifier, data);
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
		TimeSeriesDataset2 dataset = this.getInput();

		// Perform Training
		final TimeSeriesTreeClassifier[] trees = new TimeSeriesTreeClassifier[config.numTrees()];
		ExecutorService execService = Executors.newFixedThreadPool(config.cpus());
		@SuppressWarnings("unchecked")
		Future<TimeSeriesTreeClassifier>[] futures = new Future[config.numTrees()];
		for (int i = 0; i < config.numTrees(); i++) {
			ITimeSeriesTreeConfig configOfTree = ConfigCache.getOrCreate(ITimeSeriesTreeConfig.class);
			configOfTree.setProperty(ITimeSeriesTreeConfig.K_MAXDEPTH, "" + config.maxDepth());
			configOfTree.setProperty(IOwnerBasedRandomizedAlgorithmConfig.K_SEED, "" + config.seed() + i);
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
}
