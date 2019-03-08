package jaicore.ml.tsc.classifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;


/*This class is just a sketch for the BOSS ensemble classifier it assumes that the grid
 * of the parameters window size and word length is already computed
 * and that the best ones according to a percentage of the best combination are already chosen
 * and put into the delivered HashMap.
 * cf.p.1520
 * "The BOSS is concerned with time series classification in the presence of noise by Patrick Schäfer"
*/
public class BOSSEnsembleClassifier extends ASimplifiedTSClassifier<Integer> {
	private ArrayList<BOSSClassifier> ensemble = new ArrayList<BOSSClassifier>(); 
	

	public BOSSEnsembleClassifier(HashMap<Integer,Integer> windowLengthsandWordLength,int alphabetSize, double[] alphabet, boolean meanCorrected) {
		super(null);
		for(Integer windowLength : windowLengthsandWordLength.keySet()) {
			ensemble.add(new BOSSClassifier(new BOSSAlgorithm(windowLength, alphabetSize, alphabet,windowLengthsandWordLength.get(windowLength), meanCorrected), windowLength,windowLengthsandWordLength.get(windowLength) ,alphabetSize, alphabet, meanCorrected));
		}
		
	}
	
	/*
	 * In the empirical observations as described in paper: 
	 * "The BOSS is concerned with time series classification in the presence of noise Patrick Schäfer" p.1519,
	 * showed that most of
	 * the time a alphabet size of 4 works best.
	 */ 
	public BOSSEnsembleClassifier(HashMap<Integer,Integer> windowLengthsandWordLength, double[] alphabet, boolean meanCorrected) {
		super(null);
		for(Integer windowLength : windowLengthsandWordLength.keySet()) {
			ensemble.add(new BOSSClassifier(new BOSSAlgorithm(windowLength, 4, alphabet,windowLengthsandWordLength.get(windowLength),meanCorrected), windowLength,windowLengthsandWordLength.get(windowLength) ,4, alphabet, meanCorrected));
		}
		
	}
	
	
	@Override
	public Integer predict(double[] univInstance) throws PredictionException {
		//TODO Exceptions
		HashMap<Integer,Integer> labelCount = new HashMap<Integer,Integer>(); 
		int votedLabel = 0;
		int maxNumberOfVotes = Integer.MIN_VALUE;
		
		for(BOSSClassifier boss : ensemble) {
			Integer label = boss.predict(univInstance);
			if(labelCount.containsKey(label)) {
				labelCount.put(label,labelCount.get(label) +1);
				if(labelCount.get(label)>maxNumberOfVotes) {
					votedLabel =  label;
					maxNumberOfVotes = labelCount.get(label);
				}
			}
			else {
				labelCount.put(label,1);
				if(labelCount.get(label) > maxNumberOfVotes) {
					votedLabel =  label;
					maxNumberOfVotes = labelCount.get(label);
				}
			}
		}
		
		return votedLabel;
	}

	@Override
	public Integer predict(List<double[]> multivInstance) throws PredictionException {
		throw new UnsupportedOperationException("The BOSS-Esamble Classifier is an univirate classifier.");
	}

	@Override
	public List<Integer> predict(TimeSeriesDataset dataset) throws PredictionException {
		ArrayList<Integer> predicts = new ArrayList<Integer>();
		for(double[][] matrix : dataset.getValueMatrices()) {
			for(double[] instance : matrix) {
				predicts.add(predict(instance));
			}
		}
		return predicts;
	}

}
