package jaicore.ml.tsc.classifier.trees;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import jaicore.basic.TimeOut;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.IAlgorithmConfig;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.ml.tsc.classifier.ASimplifiedTSCAlgorithm;

// As proposed by Deng et. al. (as opposed to simplified version by Bagnall et. al.)
public class TimeSeriesForestAlgorithm extends ASimplifiedTSCAlgorithm<Integer, TimeSeriesForestClassifier> {

	private final int numTrees;

	public TimeSeriesForestAlgorithm(final int numTrees, final int numDepth) {
		this.numTrees = numTrees;
		// this.numDepth = numDepth;
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
		// TODO Auto-generated method stub
		return null;
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
