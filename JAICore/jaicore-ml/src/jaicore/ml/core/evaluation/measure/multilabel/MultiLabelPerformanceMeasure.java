package jaicore.ml.core.evaluation.measure.multilabel;

/**
 * Class containing performance measure names so that different evluators can be
 * referenced by them.
 * 
 * @author Helena Graf
 *
 */
public enum MultiLabelPerformanceMeasure {

	EXACT_MATCH, F1_AVERAGE, HAMMING, JACCARD, RANK;

	private boolean sortAscendingly;

	static {
		// performance functions
		EXACT_MATCH.sortAscendingly = false;
		F1_AVERAGE.sortAscendingly = false;

		// loss functions
		HAMMING.sortAscendingly = true;
		RANK.sortAscendingly = true;
		JACCARD.sortAscendingly = true;
	}

	/**
	 * Get whether this measure is sorted ascendingly (from best to worst) or
	 * descendingly. I.e. this function returns false for performance functions such
	 * as F1 and false for loss measures such as rank loss.
	 * 
	 * @return whether this measure is sorted ascendingly
	 */
	public boolean isMeasureSortedAscendingly() {
		return sortAscendingly;
	}
}
