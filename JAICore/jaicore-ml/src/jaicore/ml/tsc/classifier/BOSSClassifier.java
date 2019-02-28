package jaicore.ml.tsc.classifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.filter.SFA;

public class BOSSClassifier extends ASimplifiedTSClassifier<Integer> {
	
	private int windowSize;
	private int wordLength;
	private int alphabetSize;
	private double[] alphabet; 
	private ArrayList<HashMap<Integer,Integer>> histograms = new ArrayList<HashMap<Integer,Integer>>();
	private SFA sfa;
	
	public void setSfa(SFA sfa) {
		this.sfa = sfa;
	}

	public void setHistograms(ArrayList<HashMap<Integer, Integer>> histograms) {
		this.histograms = histograms;
	}

	public BOSSClassifier(ASimplifiedTSCAlgorithm<Integer, ? extends ASimplifiedTSClassifier<Integer>> algorithm,int windowLength,int wordLength,int alphabetSize, double[] alphabet) {
		super(new BOSSAlgorithm(windowLength, wordLength, alphabetSize,alphabet));
		this.windowSize = windowLength;
		this.wordLength = wordLength;
		this.alphabetSize = alphabetSize;
		this.alphabet = alphabet;
	}

	@Override
	public Integer predict(double[] univInstance) throws PredictionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer predict(List<double[]> multivInstance) throws PredictionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Integer> predict(TimeSeriesDataset dataset) throws PredictionException {
		// TODO Auto-generated method stub
		return null;
	}

}
