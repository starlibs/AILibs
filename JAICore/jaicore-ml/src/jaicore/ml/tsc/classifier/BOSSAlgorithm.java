package jaicore.ml.tsc.classifier;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import jaicore.basic.TimeOut;
import jaicore.basic.algorithm.AlgorithmEvent;
import jaicore.basic.algorithm.IAlgorithm;
import jaicore.ml.core.dataset.IDataset;

public class BOSSAlgorithm implements IAlgorithm<IDataset, BOSSClassifier>{

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
	public BOSSClassifier call() throws Exception {
		// TODO Auto-generated method stub
		
		//blabliblub training
		
		return new BOSSClassifier();
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IDataset getInput() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void registerListener(Object listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setNumCPUs(int numberOfCPUs) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getNumCPUs() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setTimeout(int timeout, TimeUnit timeUnit) {
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
	public AlgorithmEvent nextWithException() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
