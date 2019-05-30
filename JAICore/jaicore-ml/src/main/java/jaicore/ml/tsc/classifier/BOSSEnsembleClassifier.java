package jaicore.ml.tsc.classifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;

/* This class is just a sketch for the BOSS ensemble classifier it assumes that the grid
 * of the parameters window size and word length is already computed
 * and that the best ones according to a percentage of the best combination are already chosen
 * and put into the delivered HashMap.
 * cf.p.1520
 * "The BOSS is concerned with time series classification in the presence of noise by Patrick Schäfer" */
public class BOSSEnsembleClassifier extends ASimplifiedTSClassifier<Integer> {
	private ArrayList<BOSSClassifier> ensemble = new ArrayList<>();

	public BOSSEnsembleClassifier(final Map<Integer, Integer> windowLengthsandWordLength, final int alphabetSize, final double[] alphabet, final boolean meanCorrected) {
		for (Entry<Integer, Integer> lengthPair : windowLengthsandWordLength.entrySet()) {
			this.ensemble.add(new BOSSClassifier(lengthPair.getKey(), alphabetSize, alphabet, lengthPair.getValue(), meanCorrected));
		}
	}

	/*
	 * In the empirical observations as described in paper:
	 * "The BOSS is concerned with time series classification in the presence of noise Patrick Schäfer" p.1519,
	 * showed that most of
	 * the time a alphabet size of 4 works best.
	 */
	public BOSSEnsembleClassifier(final Map<Integer, Integer> windowLengthsandWordLength, final double[] alphabet, final boolean meanCorrected) {
		this(windowLengthsandWordLength, 4, alphabet, meanCorrected);
	}

	@Override
	public Integer predict(final double[] univInstance) throws PredictionException {
		HashMap<Integer, Integer> labelCount = new HashMap<>();
		int votedLabel = 0;
		int maxNumberOfVotes = Integer.MIN_VALUE;
		for (BOSSClassifier boss : this.ensemble) {
			Integer label = boss.predict(univInstance);
			if (labelCount.containsKey(label)) {
				labelCount.put(label, labelCount.get(label) + 1);
				if (labelCount.get(label) > maxNumberOfVotes) {
					votedLabel = label;
					maxNumberOfVotes = labelCount.get(label);
				}
			} else {
				labelCount.put(label, 1);
				if (labelCount.get(label) > maxNumberOfVotes) {
					votedLabel = label;
					maxNumberOfVotes = labelCount.get(label);
				}
			}
		}

		return votedLabel;
	}

	@Override
	public Integer predict(final List<double[]> multivInstance) throws PredictionException {
		throw new UnsupportedOperationException("The BOSS-Esamble Classifier is an univirate classifier.");
	}

	@Override
	public List<Integer> predict(final TimeSeriesDataset dataset) throws PredictionException {
		ArrayList<Integer> predicts = new ArrayList<Integer>();
		for (double[][] matrix : dataset.getValueMatrices()) {
			for (double[] instance : matrix) {
				predicts.add(this.predict(instance));
			}
		}
		return predicts;
	}

	@Override
	public <U extends ASimplifiedTSClassifier<Integer>> ASimplifiedTSCLearningAlgorithm<Integer, U> getLearningAlgorithm(final TimeSeriesDataset dataset) {
		throw new UnsupportedOperationException();
	}
}
