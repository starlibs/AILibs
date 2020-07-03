package ai.libs.jaicore.basic.metric;

/**
 * A util class for handling confusion matrix metrics that are solely based on counts of true positives (tp), false positives (fp), true negatives (tn), and false negatives (fn).
 *
 * @author mwever
 */
public class ConfusionMetrics {

	private ConfusionMetrics() {
		// hide constructor to preven instantiation
	}

	/**
	 * Precision aka. positive predictive value (PPV).
	 *
	 * @param tp Count of true positives.
	 * @param fp Count of false positives.
	 * @return The positive predictive value (PPV) aka. Precision. 1 iff both tp and fp are 0.
	 */
	public static double getPrecision(final int tp, final int fp) {
		return (tp + fp == 0) ? 1.0 : (double) tp / (tp + fp);
	}

	/**
	 * Recall aka. sensitivity aka. hit rate aka. true positive rate (TPR).
	 *
	 * @param tp Count of true positives.
	 * @param fn Count of false negatives.
	 * @return The recall/sensitivity/hit rate/TPR.
	 */
	public static double getRecall(final int tp, final int fn) {
		return (tp + fn == 0) ? 1.0 : (double) tp / (tp + fn);
	}

	/**
	 * Specificity, selectivity, true negative rate (TNR).
	 *
	 * @param fp Count of false positives.
	 * @param tn Count of true negatives.
	 * @return The specificity/selectivity/TNR
	 */
	public static double getTrueNegativeRate(final int fp, final int tn) {
		return (fp + tn == 0) ? 1.0 : (double) tn / (tn + fp);
	}

	/**
	 * Negative predictive value (NPV).
	 * @param tn Count of true negatives.
	 * @param fn Count of false negatives.
	 * @return The NPV.
	 */
	public static double getNegativePredictiveValue(final int tn, final int fn) {
		return (tn + fn == 0) ? 1.0 : (double) tn / (tn + fn);
	}

	/**
	 * Miss rate, false negative rate (FNR).
	 *
	 * @param tp Count of true positives.
	 * @param fn Count of false negatives.
	 * @return The miss rate/FNR.
	 */
	public static double getFalseNegativeRate(final int tp, final int fn) {
		return 1 - getRecall(tp, fn);
	}

	/**
	 * Fall-out aka. false positive rate (FPR).
	 * @param fp Count of false positives.
	 * @param tn Count of true negatives.
	 * @return The fall-out, FPR.
	 */
	public static double getFallOut(final int fp, final int tn) {
		return 1 - getTrueNegativeRate(fp, tn);
	}

	/**
	 * False discovery rate (FDR)
	 * @param tp Count of true positives.
	 * @param fp Count of false positives.
	 * @return The FDR.
	 */
	public static double getFalseDiscoveryRate(final int tp, final int fp) {
		return 1 - getPrecision(tp, fp);
	}

	/**
	 * The false omission rate (FOR).
	 * @param tn Count of true negatives.
	 * @param fn Count of false negatives.
	 * @return The FOR.
	 */
	public static double getFalseOmissionRate(final int tn, final int fn) {
		return 1 - getNegativePredictiveValue(tn, fn);
	}

	/**
	 * The prevalence threshold (PT).
	 *
	 * @param tp Count of true positives.
	 * @param fp Count of false positives.
	 * @param tn Count of true negatives
	 * @param fn Count of false negatives.
	 * @return The PT.
	 */
	public static double getPrevalenceThreshold(final int tp, final int fp, final int tn, final int fn) {
		return (Math.sqrt(getRecall(tp, fn) * (-getTrueNegativeRate(fp, tn) + 1)) + getTrueNegativeRate(fp, tn) - 1) / (getRecall(tp, fn) + getTrueNegativeRate(fp, tn) - 1);
	}

	/**
	 * The threat score (TS) aka. critical success index (CSI).
	 *
	 * @param tp Count of true positives.
	 * @param fp Count of false positives.
	 * @param tn Count of true negatives
	 * @param fn Count of false negatives.
	 * @return The TS/CSI.
	 */
	public static double getCriticalSuccessIndex(final int tp, final int fp, final int fn) {
		return (tp + fn + fp == 0) ? 1.0 : (double) tp / (tp + fn + fp);
	}

	/**
	 * The accuracy (ACC).
	 *
	 * @param tp Count of true positives.
	 * @param fp Count of false positives.
	 * @param tn Count of true negatives
	 * @param fn Count of false negatives.
	 * @return The ACC.
	 */
	public static double getAccuracy(final int tp, final int fp, final int tn, final int fn) {
		return (tp + tn + fp + fn == 0) ? 1.0 : (double) (tp + tn) / (tp + fp + tn + fn);
	}

	/**
	 * The misclassification rate, error rate.
	 *
	 * @param tp Count of true positives.
	 * @param fp Count of false positives.
	 * @param tn Count of true negatives
	 * @param fn Count of false negatives.
	 * @return The misclassification rate/error rate.
	 */
	public static double getErrorRate(final int tp, final int fp, final int tn, final int fn) {
		return 1 - getAccuracy(tp, fp, tn, fn);
	}

	/**
	 * The balanced accuracy (BA)
	 *
	 * @param tp Count of true positives.
	 * @param fp Count of false positives.
	 * @param tn Count of true negatives.
	 * @param fn Count of false negatives.
	 * @return The BA.
	 */
	public static double getBalancedAccuracy(final int tp, final int fp, final int tn, final int fn) {
		return (getRecall(tp, fn) + getTrueNegativeRate(fp, tn)) / 2;
	}

	/**
	 * The F1-score, harmonic mean of precision and recall.
	 * @param tp Count of true positives.
	 * @param fp Count of false positives.
	 * @param fn Count of false negatives.
	 * @return The F1-score.
	 */
	public static double getF1Score(final int tp, final int fp, final int fn) {
		return getFMeasure(1.0, tp, fp, fn);
	}

	/**
	 * The general F-Measure being parameterized by a constant beta.
	 * @param beta The constant to weight precision and recall.
	 * @param tp Count of true positives.
	 * @param fp Count of false positives.
	 * @param fn Count of false negatives.
	 * @return The F-Measure score.
	 */
	public static double getFMeasure(final double beta, final int tp, final int fp, final int fn) {
		return (1 + Math.pow(beta, 2)) * getPrecision(tp, fp) * getRecall(tp, fn) / (Math.pow(beta, 2) * getPrecision(tp, fp) + getRecall(tp, fn));
	}

	/**
	 * The Matthews correlation coefficient (MCC).
	 * @param tp Count of true positives.
	 * @param fp Count of false positives.
	 * @param tn Count of true negatives.
	 * @param fn Count of false negatives.
	 * @return The MCC.
	 */
	public static double getMatthewsCorrelationCoefficient(final int tp, final int fp, final int tn, final int fn) {
		double nominator = (double) tp * tn - fp * fn;
		double denominator = Math.sqrt((double) (tp + fp) * (tp + fn) * (tn + fp) * (tn + fn));
		return nominator / denominator;
	}

	/**
	 * The Fowlkes-Mallows index (FMI).
	 * @param tp Count of true positives.
	 * @param fp Count of false positives.
	 * @param fn Count of false negatives.
	 * @return The FMI.
	 */
	public static double getFowlkesMallowsIndex(final int tp, final int fp, final int fn) {
		return Math.sqrt(getPrecision(tp, fp) * getRecall(tp, fn));
	}

	/**
	 * The informedness or bookmaker informedness(BM)
	 * @param tp Count of true positives.
	 * @param fp Count of false positives.
	 * @param tn Count of true negatives.
	 * @param fn Count of false negatives.
	 * @return The informedness/BM.
	 */
	public static double getInformedness(final int tp, final int fp, final int tn, final int fn) {
		return getRecall(tp, fn) + getTrueNegativeRate(fp, tn) - 1;
	}

	/**
	 * The markedness (MK) or deltaP.
	 *
	 * @param tp Count of true positives.
	 * @param fp Count of false positives.
	 * @param tn Count of true negatives.
	 * @param fn Count of false negatives.
	 *
	 * @return The MK/deltaP.
	 */
	public static double getMarkedness(final int tp, final int fp, final int tn, final int fn) {
		return getPrecision(tp, fp) + getNegativePredictiveValue(tn, fn) - 1;
	}

	/**
	 * The predicted positive condition rate (PPCR).
	 * @param tp Count of true positives.
	 * @param fp Count of false positives.
	 * @param tn Count of true negatives.
	 * @param fn Count of false negatives.
	 * @return The PPCR.
	 */
	public static double getPredictedPositiveConditionRate(final int tp, final int fp, final int tn, final int fn) {
		return (tp + fp + tn + fn == 0) ? 1.0 : (double) (tp + fp) / (tp + fp + tn + fn);
	}

	/**
	 * The positive likelihood ratio (PLR).
	 * @param tp Count of true positives.
	 * @param fp Count of false positives.
	 * @param tn Count of true negatives.
	 * @param fn Count of false negatives.
	 * @return The PLR.
	 */
	public static double getPositiveLikelihoodRatio(final int tp, final int fp, final int tn, final int fn) {
		return getRecall(tp, fn) / getFallOut(fp, tn);
	}

	/**
	 * The negative likelihood ratio (NLR).
	 * @param tp Count of true positives.
	 * @param fp Count of false positives.
	 * @param tn Count of true negatives.
	 * @param fn Count of false negatives.
	 * @return The NLR.
	 */
	public static double getNegativeLikelihoodRatio(final int tp, final int fp, final int tn, final int fn) {
		return getFalseNegativeRate(tp, fn) / getTrueNegativeRate(fp, tn);
	}

	/**
	 * The diagnostic odds ratio (DOR).
	 * @param tp Count of true positives.
	 * @param fp Count of false positives.
	 * @param tn Count of true negatives.
	 * @param fn Count of false negatives.
	 * @return The DOR.
	 */
	public static double getDiagnosticOddsRatio(final int tp, final int fp, final int tn, final int fn) {
		return getPositiveLikelihoodRatio(tp, fp, tn, fn) / getNegativeLikelihoodRatio(tp, fp, tn, fn);
	}
}
