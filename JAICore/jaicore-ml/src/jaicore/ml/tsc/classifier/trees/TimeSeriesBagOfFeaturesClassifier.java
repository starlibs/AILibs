package jaicore.ml.tsc.classifier.trees;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.sets.SetUtil.Pair;
import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.tsc.classifier.ASimplifiedTSClassifier;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.features.TimeSeriesFeature;
import jaicore.ml.tsc.util.WekaUtil;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;

// Implementation of the TSBF classifier
public class TimeSeriesBagOfFeaturesClassifier extends ASimplifiedTSClassifier<Integer> {

	/**
	 * Log4j logger.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(TimeSeriesBagOfFeaturesClassifier.class);

	private RandomForest subseriesClf;
	private RandomForest finalClf;
	private int numBins;
	private int numClasses;
	private int[][][] subsequences;

	public TimeSeriesBagOfFeaturesClassifier(final int seed, final int numBins, final int numFolds,
			final double zProp, final int minIntervalLength) {
		super(new TimeSeriesBagOfFeaturesAlgorithm(seed, numBins, numFolds, zProp, minIntervalLength));
		// TODO Auto-generated constructor stub
	}


	@Override
	public Integer predict(double[] univInstance) throws PredictionException {
		// TODO Auto-generated method stub

		// Generate features and interval instances
		// double[][][] generatedFeatures = new
		// double[subsequences.length][subsequences[0].length][TimeSeriesFeature.NUM_FEATURE_TYPES];
		double[][] intervalFeatures = new double[subsequences.length][subsequences[0].length * 3];
		// int[] intervalTargets = new int[subsequences.length];

		for (int i = 0; i < subsequences.length; i++) {
			for (int j = 0; j < subsequences[i].length; j++) {
				double[] tmpFeatures = TimeSeriesFeature.getFeatures(univInstance, subsequences[i][j][0],
						subsequences[i][j][1] - 1, true);

				intervalFeatures[i][j * 3] = tmpFeatures[0];
				intervalFeatures[i][j * 3 + 1] = tmpFeatures[1];
				intervalFeatures[i][j * 3 + 2] = tmpFeatures[2];
			}
		}

		ArrayList<double[][]> subseriesValueMatrices = new ArrayList<>();
		subseriesValueMatrices.add(intervalFeatures);
		TimeSeriesDataset subseriesDataset = new TimeSeriesDataset(subseriesValueMatrices);
		Instances subseriesInstances = WekaUtil.simplifiedTimeSeriesDatasetToWekaInstances(subseriesDataset, IntStream
				.rangeClosed(0, this.numClasses - 1).boxed().map(i -> String.valueOf(i)).collect(Collectors.toList()));

		double[][] probs = null;
		int[] predictedTargets = new int[subseriesInstances.numInstances()];
		try {
			probs = this.subseriesClf.distributionsForInstances(subseriesInstances);
			for (int i = 0; i < subseriesInstances.numInstances(); i++) {
				predictedTargets[i] = (int) this.subseriesClf.classifyInstance(subseriesInstances.get(i));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			throw new PredictionException("");
		}

		int[][] discretizedProbs = TimeSeriesBagOfFeaturesAlgorithm.discretizeProbs(this.numBins, probs);
		Pair<int[][][], int[][]> histFreqPair = TimeSeriesBagOfFeaturesAlgorithm
				.formHistogramsAndRelativeFreqs(discretizedProbs, predictedTargets, 1,
				this.numClasses, this.numBins);
		int[][][] histograms = histFreqPair.getX();
		int[][] relativeFrequencies = histFreqPair.getY();

		double[][] finalHistogramInstances = TimeSeriesBagOfFeaturesAlgorithm.generateHistogramInstances(histograms,
				relativeFrequencies);
		ArrayList<double[][]> finalMatrices = new ArrayList<>();
		finalMatrices.add(finalHistogramInstances);
		TimeSeriesDataset finalDataset = new TimeSeriesDataset(finalMatrices);
		Instances finalInstances = WekaUtil.simplifiedTimeSeriesDatasetToWekaInstances(finalDataset,
				IntStream.rangeClosed(0, this.numClasses - 1).boxed().map(i -> String.valueOf(i))
						.collect(Collectors.toList()));
		
		if(finalInstances.size() != 1) {
			// TODO
			throw new PredictionException("There should be only one instance.");
		}
			
		try {
			double pred = this.finalClf.classifyInstance(finalInstances.firstInstance());
			// System.out.println(pred);
			return (int) pred;
		} catch (Exception e) {
			throw new PredictionException("Could not predict instance due to a Weka exception.", e);
		}
	}

	@Override
	public Integer predict(List<double[]> multivInstance) throws PredictionException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Multivariate prediction is not supported yet.");
	}

	@Override
	public List<Integer> predict(TimeSeriesDataset dataset) throws PredictionException {
		// TODO
		final List<Integer> result = new ArrayList<>();
		for (int i = 0; i < dataset.getValues(0).length; i++) {
			result.add(this.predict(dataset.getValues(0)[i]));
		}
		return result;
	}

	/**
	 * @return the subseriesClf
	 */
	public RandomForest getSubseriesClf() {
		return subseriesClf;
	}

	/**
	 * @param subseriesClf
	 *            the subseriesClf to set
	 */
	public void setSubseriesClf(RandomForest subseriesClf) {
		this.subseriesClf = subseriesClf;
	}

	/**
	 * @return the finalClf
	 */
	public RandomForest getFinalClf() {
		return finalClf;
	}

	/**
	 * @param finalClf
	 *            the finalClf to set
	 */
	public void setFinalClf(RandomForest finalClf) {
		this.finalClf = finalClf;
	}

	/**
	 * @return the numBins
	 */
	public int getNumBins() {
		return numBins;
	}

	/**
	 * @param numBins
	 *            the numBins to set
	 */
	public void setNumBins(int numBins) {
		this.numBins = numBins;
	}

	/**
	 * @return the numClasses
	 */
	public int getNumClasses() {
		return numClasses;
	}

	/**
	 * @param numClasses
	 *            the numClasses to set
	 */
	public void setNumClasses(int numClasses) {
		this.numClasses = numClasses;
	}

	/**
	 * @return the subsequences
	 */
	public int[][][] getSubsequences() {
		return subsequences;
	}

	/**
	 * @param subsequences
	 *            the subsequences to set
	 */
	public void setSubsequences(int[][][] subsequences) {
		this.subsequences = subsequences;
	}

}
