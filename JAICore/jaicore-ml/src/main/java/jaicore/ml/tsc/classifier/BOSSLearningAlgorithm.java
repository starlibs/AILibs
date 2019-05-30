package jaicore.ml.tsc.classifier;

import java.util.ArrayList;
import java.util.HashMap;

import jaicore.basic.algorithm.IAlgorithmConfig;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.ml.tsc.HistogramBuilder;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.exceptions.NoneFittedFilterExeception;
import jaicore.ml.tsc.filter.SFA;
import jaicore.ml.tsc.filter.SlidingWindowBuilder;
import jaicore.ml.tsc.filter.ZTransformer;

/**
 * @author Helen Beierling
 *         This class calculates all needed informations for the BOSS classifier. A fitted SFA and all
 *         histograms for the training samples.
 */
public class BOSSLearningAlgorithm extends ASimplifiedTSCLearningAlgorithm<Integer, BOSSClassifier> {

	public interface IBossAlgorithmConfig extends IAlgorithmConfig {

		public static final String K_WINDOW_SIZE = "boss.windowsize";
		public static final String K_ALPHABET_SIZE = "boss.alphabetsize";
		public static final String K_ALPHABET = "boss.alphabet";
		public static final String K_WORDLENGTH = "boss.wordlength";
		public static final String K_MEANCORRECTED = "boss.meancorrected";

		/**
		 * The size of the sliding window that is used over each instance and splits it into multiple
		 * smaller instances.
		 */
		@Key(K_WINDOW_SIZE)
		public int windowSize();

		/**
		 * The alphabet size determines the number of Bins for the SFA Histograms. Four was determined empirical
		 * as an optimal value for the alphabet size.
		 * cf.p. 1519 "The BOSS is concerned with time series classification in the presence of noise by Patrick Schäfer"
		 *
		 */
		@Key(K_ALPHABET_SIZE)
		@DefaultValue("4")
		public int alphabetSize();

		/**
		 * The alphabet consists of doubles representing letters and defines each word.
		 */
		@Key(K_ALPHABET)
		public double[] alphabet();

		/**
		 * The word length determines the number of used DFT-coefficients. Where the DFT-coefficients are
		 * half the word length.
		 */
		@Key(K_WORDLENGTH)
		public int wordLength();

		/**
		 * If mean corrected is set to true than the first DFT coefficient is dropped to normalize the mean.
		 * c.f.p. 1519 "The BOSS is concerned with time series classification in the presence of noise by Patrick Schäfer"
		 */
		@Key(K_MEANCORRECTED)
		public boolean meanCorrected();
	}

	/**
	 * The list contains the list of Histograms in which every matrix of the multivariate dataset results in.
	 */
	private ArrayList<ArrayList<HashMap<Integer, Integer>>> multivirateHistograms = new ArrayList<ArrayList<HashMap<Integer, Integer>>>();
	/**
	 * Constians the histograms of one matrix for each instance one. Where the keys are the words which are double value
	 * sequences converted to an integer hash code and the values are the corresponding word counts.
	 */
	private ArrayList<HashMap<Integer, Integer>> histograms = new ArrayList<>();

	// This class assumes that the optimal proportion of word length to window size is determined elsewhere and the corresponding
	// drop of SFA words.
	public BOSSLearningAlgorithm(final IBossAlgorithmConfig config, final BOSSClassifier classifier, final TimeSeriesDataset data) {
		super(config, classifier, data);
	}

	@Override
	public AlgorithmEvent nextWithException() {
		return null;
	}

	@Override
	public BOSSClassifier call() {

		this.multivirateHistograms.clear();
		IBossAlgorithmConfig config = (IBossAlgorithmConfig) this.getConfig();

		HistogramBuilder histoBuilder = new HistogramBuilder();
		SFA sfa = new SFA(config.alphabet(), config.wordLength(), config.meanCorrected());

		/* calculates the lookup table for the alphabet for the whole input dataset. */
		SlidingWindowBuilder slide = new SlidingWindowBuilder();
		slide.setDefaultWindowSize(config.windowSize());

		TimeSeriesDataset data = this.getInput();
		for (int matrix = 0; matrix < data.getNumberOfVariables(); matrix++) {
			this.histograms.clear();
			for (int instance = 0; instance < data.getNumberOfInstances(); instance++) {
				/*
				 * Every instance results in an own histogram there for has its own HashMap of
				 * the the from key: word value: count of word.
				 */

				/*
				 * By the special fit transform an instance is transformed to a dataset. This
				 * is done because every instance creates a list of new smaller instances when
				 * split into sliding windows.
				 */
				TimeSeriesDataset tmp = slide.specialFitTransform(data.getValues(matrix)[instance]);
				try {
					/* The from one instance resulting dataset is z-normalized. */
					ZTransformer znorm = new ZTransformer();
					for (int i = 0; i < tmp.getValues(0).length; i++) {
						tmp.getValues(0)[i] = znorm.fitTransform(tmp.getValues(0)[i]);
					}

					// The SFA words for that dataset are computed using the precomputed MCB quantisation intervals
					TimeSeriesDataset tmpTransformed = sfa.fitTransform(tmp);
					// The occurring SFA words of the instance are getting counted with a parallel numerosity reduction.

					HashMap<Integer, Integer> histogram = histoBuilder.histogramForInstance(tmpTransformed);
					// Each instance in the dataset has its own histogram so the original dataset results in a list of histograms.
					this.histograms.add((HashMap<Integer, Integer>) histogram.clone());
				}

				catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (NoneFittedFilterExeception e) {
					e.printStackTrace();
				}

			}
			// In the case of a multivariate dataset each matrix would have a list of histograms which than results
			// in a list of lists of histograms.
			// The Boss classifier however can not handle multivariate datasets.
			this.multivirateHistograms.add(this.histograms);
		}

		// In the end all calculated and needed algortihms are set for the classifier.
		BOSSClassifier model = this.getClassifier();
		model.setMultivirateHistograms(this.multivirateHistograms);
		model.setHistogramUnivirate(this.multivirateHistograms.get(0));
		model.setTrainingData(this.getInput());
		return model;
	}
}
