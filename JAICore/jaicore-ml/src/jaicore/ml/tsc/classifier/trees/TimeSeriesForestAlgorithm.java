package jaicore.ml.tsc.classifier.trees;

import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import jaicore.basic.TimeOut;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.IAlgorithm;
import jaicore.basic.algorithm.IAlgorithmConfig;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.ml.tsc.classifier.ASimplifiedTSCAlgorithm;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;

/**
 * Algorithm to train a time series forest classifier as described in Deng,
 * Houtao et al. “A Time Series Forest for Classification and Feature
 * Extraction.” Inf. Sci. 239 (2013): 142-153. Consists of mutliple
 * {@link TimeSeriesTree} classifier.
 * 
 * @author Julian Lienen
 *
 */
public class TimeSeriesForestAlgorithm extends ASimplifiedTSCAlgorithm<Integer, TimeSeriesForestClassifier> {
	/**
	 * Number of trees to be trained.
	 */
	private final int numTrees;

	/**
	 * Maximum depth of the trained trees.
	 */
	private final int maxDepth;

	/**
	 * Seed used for all randomized operations.
	 */
	private final int seed;

	/**
	 * Indicator whether feature caching should be used. Usage for datasets with
	 * many attributes is not recommended due to a high number of possible
	 * intervals.
	 */
	private boolean useFeatureCaching = false;

	/**
	 * See {@link IAlgorithm#getNumCPUs()}.
	 */
	private int cpus = 1;

	/**
	 * See {@link IAlgorithm#getTimeout()}.
	 */
	private TimeOut timeout = new TimeOut(Integer.MAX_VALUE, TimeUnit.SECONDS);

	/**
	 * Constructor for a time series forest training algorithm.
	 * 
	 * @param numTrees
	 *            Number of trees to be trained
	 * @param maxDepth
	 *            Maximal depth of each tree
	 * @param seed
	 *            Seed used for randomized operations
	 */
	public TimeSeriesForestAlgorithm(final int numTrees, final int maxDepth, final int seed) {
		this.numTrees = numTrees;
		this.maxDepth = maxDepth;
		this.seed = seed;
	}

	/**
	 * Constructor for a time series forest training algorithm.
	 * 
	 * @param numTrees
	 *            Number of trees to be trained
	 * @param maxDepth
	 *            Maximal depth of each tree
	 * @param seed
	 *            Seed used for randomized operations
	 * @param useFeatureCaching
	 *            Indicator whether feature caching should be used
	 */
	public TimeSeriesForestAlgorithm(final int numTrees, final int maxDepth, final int seed,
			final boolean useFeatureCaching) {
		this.numTrees = numTrees;
		this.maxDepth = maxDepth;
		this.seed = seed;
		this.useFeatureCaching = useFeatureCaching;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerListener(Object listener) {
		throw new UnsupportedOperationException("The operation to be performed is not supported.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getNumCPUs() {
		return this.cpus;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setNumCPUs(int numberOfCPUs) {
		this.cpus = numberOfCPUs;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setTimeout(long timeout, TimeUnit timeUnit) {
		this.timeout = new TimeOut(timeout, timeUnit);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setTimeout(TimeOut timeout) {
		this.timeout = timeout;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TimeOut getTimeout() {
		return this.timeout;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AlgorithmEvent nextWithException()
			throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException {
		throw new UnsupportedOperationException("The operation to be performed is not supported.");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IAlgorithmConfig getConfig() {
		throw new UnsupportedOperationException("The operation to be performed is not supported.");
	}

	/**
	 * Training procedure construction a time series tree using the given input
	 * data.
	 */
	@Override
	public TimeSeriesForestClassifier call()
			throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException {
		
		TimeSeriesDataset dataset = this.getInput();
		
		// Perform Training
		final TimeSeriesTree[] trees = new TimeSeriesTree[this.numTrees];
		ExecutorService execService = Executors.newFixedThreadPool(this.cpus);
		@SuppressWarnings("unchecked")
		Future<TimeSeriesTree>[] futures = new Future[this.numTrees];
		for (int i = 0; i < this.numTrees; i++) {
			TimeSeriesTree tst = new TimeSeriesTree(this.maxDepth, this.seed + i, this.useFeatureCaching);
			futures[i] = execService.submit(new Callable<TimeSeriesTree>() {
				@Override
				public TimeSeriesTree call() throws Exception {

					tst.train(dataset);
					tst.setTrained(true);
					return tst;

				}
			});
		}

		// Wait for completion
		execService.shutdown();
		execService.awaitTermination(this.timeout.seconds(), TimeUnit.SECONDS);
		for (int i = 0; i < this.numTrees; i++) {
			try {
				TimeSeriesTree tst = futures[i].get();
				trees[i] = tst;
			} catch (ExecutionException e) {
				throw new AlgorithmException(
						"Could not train time series tree due to training exception: " + e.getMessage());
			}
		}

		this.model.setTrees(trees);
		this.model.setTrained(true);

		return this.model;
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
