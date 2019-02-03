package jaicore.ml.core.evaluation.measure.multilabel;

/**
 * Class containing performance measure names so that different evluators can be
 * referenced by them.
 * 
 * @author Helena Graf
 *
 */
public enum MultiLabelPerformanceMeasure {

	ZERO_ONE, INVERSE_F1_MACRO_AVERAGE_D, INVERSE_F1_MACRO_AVERAGE_L, HAMMING, JACCARD, RANK, INVERSE_FITNESS;

	private boolean sortAscendingly;

	static {
		// actual performance functions (when not inverted)
		INVERSE_F1_MACRO_AVERAGE_L.sortAscendingly = false;
		INVERSE_F1_MACRO_AVERAGE_D.sortAscendingly = false;
		INVERSE_FITNESS.sortAscendingly = false;

		// loss functions
		ZERO_ONE.sortAscendingly = true;
		HAMMING.sortAscendingly = true;
		RANK.sortAscendingly = true;
		JACCARD.sortAscendingly = true;
	}

	/**
	 * Get whether this measure is sorted ascendingly (from best to worst) or
	 * descendingly. I.e. this function returns false for performance functions such
	 * as F1 and false for loss measures such as rank loss. In the implementation,
	 * all the measures are sorted ascendingly.
	 * 
	 * @return whether this measure is sorted ascendingly
	 */
	public boolean isMeasureSortedAscendingly() {
		return sortAscendingly;
	}
}
