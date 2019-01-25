package jaicore.ml.tsc.classifier.trees;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import jaicore.basic.TimeOut;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.IAlgorithmConfig;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.tsc.classifier.ASimplifiedTSCAlgorithm;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;

// As proposed by Deng et. al. (as opposed to simplified version by Bagnall et. al.)
public class TimeSeriesForestAlgorithm extends ASimplifiedTSCAlgorithm<Integer, TimeSeriesForestClassifier> {

	private final int numTrees;
	private final int maxDepth;
	private final int seed;

	public TimeSeriesForestAlgorithm(final int numTrees, final int maxDepth, final int seed) {
		this.numTrees = numTrees;
		this.maxDepth = maxDepth;
		this.seed = seed;
	}

	@Override
	public void registerListener(Object listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getNumCPUs() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setNumCPUs(int numberOfCPUs) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTimeout(long timeout, TimeUnit timeUnit) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTimeout(TimeOut timeout) {
		// TODO Auto-generated method stub

	}

	@Override
	public TimeOut getTimeout() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AlgorithmEvent nextWithException()
			throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IAlgorithmConfig getConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TimeSeriesForestClassifier call()
			throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException {
		
		TimeSeriesDataset dataset = this.getInput();
		
		// Training
		final TimeSeriesTree[] trees = new TimeSeriesTree[this.numTrees];
		for (int i = 0; i < this.numTrees; i++) {
			TimeSeriesTree tst = new TimeSeriesTree(this.maxDepth, this.seed + i);
			try {
				tst.train(dataset);
			} catch (TrainingException e) {
				throw new AlgorithmException(
						"Could not train time series tree due to training exception: " + e.getMessage());
			}
			trees[i] = tst;
		}

		this.model.setTrees(trees);

		return this.model;
	}

	@Override
	public Iterator<AlgorithmEvent> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasNext() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AlgorithmEvent next() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub

	}

}
