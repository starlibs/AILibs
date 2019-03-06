package jaicore.ml.tsc.classifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import jaicore.basic.TimeOut;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.IAlgorithmConfig;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.ml.tsc.HistogramBuilder;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.exceptions.NoneFittedFilterExeception;
import jaicore.ml.tsc.filter.SFA;
import jaicore.ml.tsc.filter.SlidingWindowBuilder;

public class BOSSAlgorithm extends ASimplifiedTSCAlgorithm<Integer, BOSSClassifier> {
	
	//TODO mean normalization by dropping first DFT Coefficient
	private int windowSize;
	private int alphabetSize;
	private double[] alphabet;
	private ArrayList<ArrayList<HashMap<Integer,Integer>>> multivirateHistograms = new ArrayList<ArrayList<HashMap<Integer,Integer>>> ();
	private ArrayList<HashMap<Integer,Integer>> histograms = new ArrayList<HashMap<Integer, Integer>>(); 
	
	
	public BOSSAlgorithm(int windowLength,int alphabetSize, double[] alphabet) {
		this.windowSize = windowLength;
		this.alphabetSize = alphabetSize;
		this.alphabet = alphabet;
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
	public BOSSClassifier call()
			throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException {
		
		
		SFA sfa = new SFA(alphabet, alphabetSize);
		HistogramBuilder histoBuilder = new HistogramBuilder();
		sfa.fit(input);
		
		
		SlidingWindowBuilder slide = new SlidingWindowBuilder();
		slide.setDefaultWindowSize(windowSize);
		
		for(int matrix = 0; matrix < input.getNumberOfVariables(); matrix++) {
			for(int instance = 0; instance < input.getNumberOfInstances(); instance++) {
				HashMap<Integer,Integer> histogram = null;
				TimeSeriesDataset tmp = slide.specialFitTransform(input.getValues(matrix)[instance]);
				try {
					TimeSeriesDataset tmpTransformed = sfa.transform(tmp);
					histogram = histoBuilder.histogramForInstance(tmpTransformed);
					}
				catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoneFittedFilterExeception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				histograms.add(histogram);
			}
			multivirateHistograms.add(histograms);
		}
		model.setMultivirateHistograms(multivirateHistograms);
		model.setSfa(sfa);
		model.setTrainingData(input);
		return model;
	
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
