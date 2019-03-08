package jaicore.ml.tsc.classifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.tsc.HistogramBuilder;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.filter.SFA;
import jaicore.ml.tsc.filter.SlidingWindowBuilder;

public class BOSSClassifier extends ASimplifiedTSClassifier<Integer> {
	
	private TimeSeriesDataset trainingData;
	private int windowSize;
	private int wordLength;
	private int alphabetSize;
	private double[] alphabet; 
	private ArrayList<ArrayList<HashMap<Integer,Integer>>> multivirateHistograms = new ArrayList<ArrayList<HashMap<Integer,Integer>>> ();
	private SFA sfa;
	
	public void setTrainingData(TimeSeriesDataset trainingData) {
		this.trainingData = trainingData;
	}

	public void setSfa(SFA sfa) {
		this.sfa = sfa;
	}
	
	public void setMultivirateHistograms(ArrayList<ArrayList<HashMap<Integer, Integer>>> multivirateHistograms) {
		this.multivirateHistograms = multivirateHistograms;
	}

	public BOSSClassifier(ASimplifiedTSCAlgorithm<Integer, ? extends ASimplifiedTSClassifier<Integer>> algorithm,int windowLength,int wordLength,int alphabetSize, double[] alphabet, boolean meanCorrected) {
		super(new BOSSAlgorithm(windowLength, alphabetSize,alphabet,wordLength, meanCorrected));
		this.windowSize = windowLength;
		this.wordLength = wordLength;
		this.alphabetSize = alphabetSize;
		this.alphabet = alphabet;
	}
	
	/*
	 * In the empirical observations as described in paper: 
	 * "The BOSS is concerned with time series classification in the presence of noise Patrick Schäfer" p.1519,
	 * showed that most of
	 * the time a alphabet size of 4 works best.
	 */ 
	public BOSSClassifier(ASimplifiedTSCAlgorithm<Integer, ? extends ASimplifiedTSClassifier<Integer>> algorithm,int windowLength,int wordLength, double[] alphabet, boolean meanCorrected) {
		super(new BOSSAlgorithm(windowLength, 4,alphabet,wordLength, meanCorrected));
		this.windowSize = windowLength;
		this.wordLength = wordLength;
		this.alphabetSize = 4;
		this.alphabet = alphabet;
	}

	
	@Override
	public Integer predict(double[] univInstance) throws PredictionException {
		//TODO Exceptions 
		
		SlidingWindowBuilder slide = new SlidingWindowBuilder();
		HistogramBuilder histoBuilder = new HistogramBuilder();
		
		slide.setDefaultWindowSize(windowSize);
		
		TimeSeriesDataset tmp = slide.specialFitTransform(univInstance);
		HashMap<Integer,Integer> histogram = histoBuilder.histogramForInstance(tmp);
		
		int indexOFminDistInstance = 0;
		double minDist = Double.MAX_VALUE;
		
		
		for(int i = 0; i< multivirateHistograms.get(0).size(); i++) {
			double dist = BossDistance(histogram, multivirateHistograms.get(0).get(i));
			if(dist < minDist) {
				indexOFminDistInstance = i;
			}
		}
		
		return trainingData.getTargets()[indexOFminDistInstance];
	}

	@Override
	public Integer predict(List<double[]> multivInstance) throws PredictionException {
		throw new UnsupportedOperationException("The BOSS classifier is a univariat classifer");
	}

	@Override
	public List<Integer> predict(TimeSeriesDataset dataset) throws PredictionException {
		ArrayList<Integer> predictions = new ArrayList<Integer>();
		for(double[][] matrix: dataset.getValueMatrices()) {
			for(double[] instance : matrix) {
				predictions.add(predict(instance));
			}
		}
		return predictions;
	}

	/**
	 * @param a The distance starting point histogram.
	 * @param b	The distance destination histogram.
	 * @return The distance between Histogram a and b.
	 * 
	 * The distance itself is calculated as 0 if the word does not appear in b but not in a and 
	 * if the word exists in a but not in it is the wordcount of a squared. For the "normal" case 
	 * where the word exists in a and b the distance is wordcount of a minus b squared.
	 */
	private double BossDistance(HashMap<Integer,Integer> a, HashMap<Integer,Integer> b) {
		double result = 0;
		for(Integer key : a.keySet()) {
			if(b.containsKey(key)) {
				result += (Math.pow(a.get(key)-b.get(key),2));
			}
			else {
				result+=Math.pow(a.get(key),2);
			}
		}
		return result;
	}
}
