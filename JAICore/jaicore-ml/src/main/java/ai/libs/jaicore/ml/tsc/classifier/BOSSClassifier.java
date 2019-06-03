package ai.libs.jaicore.ml.tsc.classifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.aeonbits.owner.ConfigCache;

import ai.libs.jaicore.ml.core.exception.PredictionException;
import ai.libs.jaicore.ml.tsc.HistogramBuilder;
import ai.libs.jaicore.ml.tsc.classifier.BOSSLearningAlgorithm.IBossAlgorithmConfig;
import ai.libs.jaicore.ml.tsc.dataset.TimeSeriesDataset;
import ai.libs.jaicore.ml.tsc.filter.SFA;
import ai.libs.jaicore.ml.tsc.filter.SlidingWindowBuilder;
import ai.libs.jaicore.ml.tsc.filter.ZTransformer;

/**
 * @author Helen Beierling
 *         This class predicts labels for instances by cutting the instance into pieces and calculate for every piece the DFT values and
 *         assigns them to the corresponding letter to form the SFA words of that piece and afterwards creating a histogram for which the
 *         distance is calculated to all histograms of the training data and the label of the nearest one is returned as prediction.
 */
public class BOSSClassifier extends ASimplifiedTSClassifier<Integer> {

	private TimeSeriesDataset trainingData;
	// ---------------------------------------------------------------
	// All variables needed for the to predict instance and for the BOSS Algorithm or calculated by it .
	private final IBossAlgorithmConfig config;
	private List<Map<Integer, Integer>> univirateHistograms;

	// ---------------------------------------------------------------
	// All needed for every predict.
	private SlidingWindowBuilder slide = new SlidingWindowBuilder();
	private HistogramBuilder histoBuilder = new HistogramBuilder();
	private ZTransformer znorm = new ZTransformer();

	public BOSSClassifier(final int windowLength, final int alphabetSize, final double[] alphabet, final int wordLength, final boolean meanCorrected) {
		this.config = ConfigCache.getOrCreate(IBossAlgorithmConfig.class);
		this.config.setProperty(IBossAlgorithmConfig.K_WINDOW_SIZE, "" + windowLength);
		this.config.setProperty(IBossAlgorithmConfig.K_ALPHABET_SIZE, "" + alphabetSize);
		this.config.setProperty(IBossAlgorithmConfig.K_ALPHABET, "" + alphabet);
		this.config.setProperty(IBossAlgorithmConfig.K_WORDLENGTH, "" + wordLength);
		this.config.setProperty(IBossAlgorithmConfig.K_MEANCORRECTED, "" + meanCorrected);
		this.slide.setDefaultWindowSize(this.config.windowSize());
	}

	public BOSSClassifier(final IBossAlgorithmConfig config) {
		this.config = config;

		// This is the same window size as used for the training samples
		this.slide.setDefaultWindowSize(config.windowSize());
	}

	public List<Map<Integer, Integer>> getUnivirateHistograms() {
		return this.univirateHistograms;
	}

	public void setTrainingData(final TimeSeriesDataset trainingData) {
		this.trainingData = trainingData;
	}

	public void setHistogramUnivirate(final List<Map<Integer, Integer>> histograms) {
		this.univirateHistograms = histograms;
	}

	@Override
	public Integer predict(final double[] univInstance) throws PredictionException {
		SFA sfa = new SFA(this.config.alphabet(), this.config.wordLength());

		// create windows for test instance an there for a small dataset with
		// windows as instances.
		TimeSeriesDataset tmp = this.slide.specialFitTransform(univInstance);

		// need to call a new fit for each predict because each window gets z normalized by its own.
		// c.f.p. 1509 "The BOSS is concerned with time series classification in the presence of noise by Patrick Schäfer"
		for (int instance = 0; instance < tmp.getValues(0).length; instance++) {
			tmp.getValues(0)[instance] = this.znorm.fitTransform(tmp.getValues(0)[instance]);
		}

		TimeSeriesDataset tmpznormedsfaTransformed = sfa.fitTransform(tmp);
		HashMap<Integer, Integer> histogram = this.histoBuilder.histogramForInstance(tmpznormedsfaTransformed);

		// Calculate distance for all histograms for all instances in the training set.
		// Remember index of histogram with minimum distance in list because it corresponds to the
		// instance that produced that histogram with minimum distance.
		int indexOFminDistInstance = 0;
		double minDist = Double.MAX_VALUE;

		for (int i = 0; i < this.univirateHistograms.size(); i++) {
			double dist = this.getBossDistance(histogram, this.univirateHistograms.get(i));
			if (dist < minDist) {
				minDist = dist;
				indexOFminDistInstance = i;
			}
		}

		// return the target of that instance that had the minimum distance.
		return this.trainingData.getTargets()[indexOFminDistInstance];
	}

	@Override
	public Integer predict(final List<double[]> multivInstance) throws PredictionException {
		// The BOSS classifier only supports predictions for univariate instances.
		throw new UnsupportedOperationException("The BOSS classifier is a univariat classifer");
	}

	@Override
	public List<Integer> predict(final TimeSeriesDataset dataset) throws PredictionException {
		// For a list of instances a list of predictions are getting created and the list is than returned.
		List<Integer> predictions = new ArrayList<>();
		for (double[][] matrix : dataset.getValueMatrices()) {
			for (double[] instance : matrix) {
				predictions.add(this.predict(instance));
			}
		}
		return predictions;
	}

	/**
	 * @param a The distance starting point histogram.
	 * @param b The distance destination histogram.
	 * @return The distance between Histogram a and b.
	 *
	 *         The distance itself is calculated as 0 if the word does appear in "b" but not in "a" and
	 *         if the word exists in "a" but not in "b" it is the word count of "a" squared. For the "normal" case
	 *         where the word exists in "a" and "b" the distance is word count of "a" minus "b" and the result gets
	 *         squared for each word and summed up over the whole histogram.
	 *         Therefore the two histograms do not need to be of the same size and the distance of "a" to "b" must not
	 *         be equal to the distance of "b" to "a".
	 *         c.f. p. 1516 "The BOSS is concerned with time series classification in the presence of noise by Patrick Schäfer"
	 */
	private double getBossDistance(final Map<Integer, Integer> a, final Map<Integer, Integer> b) {
		double result = 0;
		for (Entry<Integer, Integer> entry : a.entrySet()) {
			int key = entry.getKey();
			int val = entry.getValue();
			if (b.containsKey(key)) {
				result += (Math.pow(val - (double) b.get(key), 2));
			} else {
				result += Math.pow(val, 2);
			}
		}
		return result;
	}

	@Override
	public BOSSLearningAlgorithm getLearningAlgorithm(final TimeSeriesDataset dataset) {
		return new BOSSLearningAlgorithm(this.config, this, dataset);
	}
}
