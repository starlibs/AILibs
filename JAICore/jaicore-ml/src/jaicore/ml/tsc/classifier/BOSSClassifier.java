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

	public BOSSClassifier(ASimplifiedTSCAlgorithm<Integer, ? extends ASimplifiedTSClassifier<Integer>> algorithm,int windowLength,int wordLength,int alphabetSize, double[] alphabet) {
		super(new BOSSAlgorithm(windowLength, alphabetSize,alphabet));
		this.windowSize = windowLength;
		this.wordLength = wordLength;
		this.alphabetSize = alphabetSize;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Integer> predict(TimeSeriesDataset dataset) throws PredictionException {
		// TODO Auto-generated method stub
		return null;
	}

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
