package jaicore.ml.tsc.classifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.tsc.HistogramBuilder;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.exceptions.NoneFittedFilterExeception;
import jaicore.ml.tsc.filter.SFA;
import jaicore.ml.tsc.filter.SlidingWindowBuilder;
import jaicore.ml.tsc.filter.ZTransformer;

/**
 * @author Helen Beierling 
 *	This class predicts labels for instances by cutting the instance into pieces and calculate for every piece the DFT values and 
 *  assigns them to the corresponding letter to form the SFA words of that piece and afterwards creating a histogram for which the
 *  distance is calculated to all histograms of the training data and the label of the nearest one is returned as prediction.
 */
public class BOSSClassifier extends ASimplifiedTSClassifier<Integer> {
	
	private TimeSeriesDataset trainingData;
	//---------------------------------------------------------------
	// All variables needed for the to predict instance and for the BOSS Algorithm or calculated by it .  
	private int windowLength;
	private int wordLength;
	private int alphabetSize;
	private double[] alphabet; 
	private ArrayList<ArrayList<HashMap<Integer,Integer>>> multivirateHistograms = new ArrayList<ArrayList<HashMap<Integer,Integer>>> ();
	private SFA sfa;
	private ArrayList<HashMap<Integer, Integer>> univirateHistograms;
	//---------------------------------------------------------------
	// All needed for every predict. 
	private SlidingWindowBuilder slide = new SlidingWindowBuilder();
	private HistogramBuilder histoBuilder = new HistogramBuilder();
	private ZTransformer znorm = new ZTransformer();
	
	public void setTrainingData(TimeSeriesDataset trainingData) {
		this.trainingData = trainingData;
	}

	public void setSfa(SFA sfa) {
		this.sfa = sfa;
	}
	
	public void setMultivirateHistograms(ArrayList<ArrayList<HashMap<Integer, Integer>>> multivirateHistograms) {
		this.multivirateHistograms = multivirateHistograms;
	}
	
	public void setHistogramUnivirate(ArrayList<HashMap<Integer, Integer>> histograms) {
		this.univirateHistograms = histograms;
	}

	public BOSSClassifier(ASimplifiedTSCAlgorithm<Integer, ? extends ASimplifiedTSClassifier<Integer>> algorithm,int windowLength,int wordLength,int alphabetSize, double[] alphabet, boolean meanCorrected) {
		super(new BOSSAlgorithm(windowLength, alphabetSize,alphabet,wordLength, meanCorrected));
		this.windowLength = windowLength;
		this.wordLength = wordLength;
		this.alphabetSize = alphabetSize;
		this.alphabet = alphabet;
		
		//This is the same window size as used for the training samples
		slide.setDefaultWindowSize(windowLength);
	}
	
	/*
	 * In the empirical observations as described in paper: 
	 * "The BOSS is concerned with time series classification in the presence of noise Patrick Schäfer" p.1519,
	 * showed that most of
	 * the time a alphabet size of 4 works best.
	 */ 
	public BOSSClassifier(ASimplifiedTSCAlgorithm<Integer, ? extends ASimplifiedTSClassifier<Integer>> algorithm,int windowLength,int wordLength, double[] alphabet, boolean meanCorrected) {
		super(new BOSSAlgorithm(windowLength, 4,alphabet,wordLength, meanCorrected));
		this.windowLength = windowLength;
		this.wordLength = wordLength;
		this.alphabetSize = 4;
		this.alphabet = alphabet;
		
		//This is the same window size as used for the training samples
		slide.setDefaultWindowSize(windowLength);
	}

	
	@Override
	public Integer predict(double[] univInstance) throws PredictionException {
		//TODO Exceptions 
		
		TimeSeriesDataset tmpznormedsfaTransformed = null;
		
		//create windows for test instance an there for a small dataset with 
		//windows as instances.
		TimeSeriesDataset tmp = slide.specialFitTransform(univInstance);
		try {
			TimeSeriesDataset tmpznormed = new TimeSeriesDataset(tmp.getValueMatrices(),null,null);
			// need to call a new fit for each predict because each window gets z normalized by its own.
			//c.f.p. 1509 "The BOSS is concerned with time series classification in the presence of noise by Patrick Schäfer"
			for(int instance = 0; instance < tmp.getValues(0).length; instance++) {
				tmpznormed.getValues(0)[instance] = znorm.fitTransform(tmp.getValues(0)[instance]);
			}
			
			 tmpznormedsfaTransformed= sfa.transform(tmpznormed);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoneFittedFilterExeception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HashMap<Integer,Integer> histogram = histoBuilder.histogramForInstance(tmpznormedsfaTransformed);
		
		// Calculate distance for all histograms for all instances in the training set.
		// Remember index of histogram with minimum distance in list because it corresponds to the 
		// instance that produced that histogram with minimum distance. 
		int indexOFminDistInstance = 0;
		double minDist = Double.MAX_VALUE;
		
		
		for(int i = 0; i< univirateHistograms.size(); i++) {
			double dist = BossDistance(histogram, univirateHistograms.get(i));
			if(dist < minDist) {
				indexOFminDistInstance = i;
			}
		}
		// return the target of that instance that had the minimum distance. 
		return trainingData.getTargets()[indexOFminDistInstance];
	}

	@Override
	public Integer predict(List<double[]> multivInstance) throws PredictionException {
		// The BOSS classifier only supports predictions for univariate instances.
		throw new UnsupportedOperationException("The BOSS classifier is a univariat classifer");
	}

	@Override
	public List<Integer> predict(TimeSeriesDataset dataset) throws PredictionException {
		// For a list of instances a list of predictions are getting created and the list is than returned. 
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
	 * The distance itself is calculated as 0 if the word does appear in "b" but not in "a" and 
	 * if the word exists in "a" but not in "b" it is the word count of "a" squared. For the "normal" case 
	 * where the word exists in "a" and "b" the distance is word count of "a" minus "b" and the result gets
	 * squared for each word and summed up over the whole histogram.
	 * Therefore the two histograms do not need to be of the same size and the distance of "a" to "b" must not
	 * be equal to the distance of "b" to "a".
	 * c.f. p. 1516 "The BOSS is concerned with time series classification in the presence of noise by Patrick Schäfer" 
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
