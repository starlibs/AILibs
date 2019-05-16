package jaicore.ml.tsc.classifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.TimeOut;
import jaicore.basic.sets.SetUtil.Pair;
import jaicore.ml.core.exception.EvaluationException;
import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.tsc.classifier.shapelets.LearnShapeletsClassifier;
import jaicore.ml.tsc.classifier.shapelets.ShapeletTransformTSClassifier;
import jaicore.ml.tsc.classifier.trees.LearnPatternSimilarityClassifier;
import jaicore.ml.tsc.classifier.trees.TimeSeriesBagOfFeaturesClassifier;
import jaicore.ml.tsc.classifier.trees.TimeSeriesForestClassifier;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.exceptions.TimeSeriesLoadingException;
import jaicore.ml.tsc.quality_measures.FStat;
import jaicore.ml.tsc.util.ClassMapper;
import jaicore.ml.tsc.util.SimplifiedTimeSeriesLoader;
import timeseriesweka.classifiers.LPS;
import timeseriesweka.classifiers.LearnShapelets;
import timeseriesweka.classifiers.ShapeletTransformClassifier;
import timeseriesweka.classifiers.TSBF;
import timeseriesweka.classifiers.TSF;

/**
 * Base class for simplified time series classifier comparisons between own
 * classes and reference implementations.
 * 
 * @author Julian Lienen
 *
 */
public class SimplifiedTSClassifierTest extends TSClassifierTest {
	/**
	 * Log4j logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(SimplifiedTSClassifierTest.class);

	/**
	 * Function comparing a time series classifier deriving from Weka's
	 * <code>Classifier</code> interface (used in Bagnall's reference
	 * implementations) or from <code>sfa.classification.Classifier</code>.
	 * 
	 * @param tsRefClassifier
	 *            Time series classifier reference to be compared to own
	 *            implementation
	 * @param tsClassifier
	 *            Own implementation of the time series classifier
	 * @param seed
	 *            Seed used in the experiments
	 * @param tsRefClassifierParams
	 *            Textual description of the used parameters of the reference
	 *            classifier. Will be stored in the database entry
	 * @param tsClassifierParams
	 *            Textual description of the used parameters of the own classifier.
	 *            Will be stored in the database entry
	 * @param trainingArffFile
	 *            Arff file storing the training dataset
	 * @param testArffFile
	 *            Arff file storing the test dataset
	 * @return Returns a map consisting of a fields and values to be stored in the
	 *         database
	 * @throws IOException
	 *             Will be thrown if the data could not be read
	 * @throws FileNotFoundException
	 *             Will be thrown if the given data set file could not be found
	 * @throws EvaluationException
	 *             Will be thrown if the given classifier could not be evaluated
	 * @throws TrainingException
	 *             Will be thrown if the given classifier could not be trained
	 * @throws PredictionException
	 *             Will be thrown if a prediction error occurred
	 * @throws TimeSeriesLoadingException
	 *             Will be thrown if the time series dataset could not be loaded
	 */
	public static Map<String, Object> compareClassifiers(final Object tsRefClassifier,
			final ASimplifiedTSClassifier<?> tsClassifier, final int seed, final String tsRefClassifierParams,
			final String tsClassifierParams, final File trainingArffFile, final File testArffFile)
			throws FileNotFoundException, EvaluationException, IOException, 
			TimeSeriesLoadingException {

		if (trainingArffFile == null || testArffFile == null)
			throw new IllegalArgumentException("Training and test file must not be null!");

		final Map<String, Object> result = new HashMap<>();
		// result.put("seed", seed);
		// result.put("dataset", reduceFileNames(trainingArffFile, testArffFile));

		// Load dataset
		// TODO: Deal with strings?
		Pair<TimeSeriesDataset, ClassMapper> trainPair = SimplifiedTimeSeriesLoader.loadArff(trainingArffFile);
		TimeSeriesDataset train = trainPair.getX();
		tsClassifier.setClassMapper(trainPair.getY());
		Pair<TimeSeriesDataset, ClassMapper> testPair = SimplifiedTimeSeriesLoader.loadArff(testArffFile);
		TimeSeriesDataset test = testPair.getX();

		// TODO: Deal with it and move it to the right place
		if (trainPair.getY() != null && testPair.getY() != null
				&& !testPair.getY().getClassValues().equals(trainPair.getY().getClassValues())) {
			LOGGER.warn("The class mapper of the training data differs from the test class mapper.");
		}

		try {
			trainAndEvaluateClassifier(tsClassifier, seed, tsClassifierParams, result, train, test);
		} catch (TrainingException e) {
			LOGGER.debug("Could not finish training of the own classifier implementation due to '{}'.",
					e.getMessage());
			e.printStackTrace();
			result.put("train_time", -1);
			result.put("eval_time", -1);
			result.put("accuracy", 0);

		} catch (PredictionException e) {
			LOGGER.debug("Could not finish evaluation of the own classifier implementation due to '{}'.",
					e.getMessage());
			e.printStackTrace();
			result.put("eval_time", -1);
			result.put("accuracy", 0);
		}

		// Test reference classifier
		try {
			compareRefClassifiers(tsRefClassifier, seed, tsRefClassifierParams, result, trainingArffFile, testArffFile);
		} catch (Exception e) {
			if (!result.containsKey("ref_train_time"))
				result.put("ref_train_time", -1);
			result.put("ref_eval_time", -1);
			result.put("ref_accuracy", 0);
		}

		return result;
	}

	/**
	 * Function comparing a time series classifier deriving from Weka's
	 * <code>Classifier</code> interface (used in Bagnall's reference
	 * implementations) or from <code>sfa.classification.Classifier</code>.
	 * 
	 * @param tsRefClassifier
	 *            Time series classifier reference to be compared to own
	 *            implementation
	 * @param tsClassifier
	 *            Own implementation of the time series classifier
	 * @param seed
	 *            Seed used for the stratified split of the given data in
	 *            <code>arffFile</code>
	 * @param trainingPortion
	 *            Training portion to be used for training of both classifiers
	 * @param tsRefClassifierParams
	 *            Textual description of the used parameters of the reference
	 *            classifier. Will be stored in the database entry
	 * @param tsClassifierParams
	 *            Textual description of the used parameters of the own classifier.
	 *            Will be stored in the database entry
	 * @param arffFiles
	 *            Arff files containing the data used for training and evaluation
	 *            (assumes univariate dataset, if only one file is given)
	 * @return Returns a map consisting of a fields and values to be stored in the
	 *         database
	 * @throws IOException
	 *             Will be thrown if the data could not be read
	 * @throws FileNotFoundException
	 *             Will be thrown if the given data set file could not be found
	 * @throws EvaluationException
	 *             Will be thrown if the given classifier could not be evaluated
	 * @throws TrainingException
	 *             Will be thrown if the given classifier could not be trained
	 * @throws PredictionException
	 *             Will be thrown if a prediction error occurred
	 * @throws TimeSeriesLoadingException
	 *             Will be thrown if the time series dataset could not be loaded
	 */
	public static Map<String, Object> compareClassifiers(final Object tsRefClassifier,
			final ASimplifiedTSClassifier<?> tsClassifier, final int seed, final double trainingPortion,
			final String tsRefClassifierParams, final String tsClassifierParams, final File... arffFiles)
			throws FileNotFoundException, EvaluationException, TrainingException, IOException, PredictionException,
			TimeSeriesLoadingException {

		final Map<String, Object> result = new HashMap<>();
		result.put("seed", seed);
		result.put("dataset", reduceFileNames(arffFiles));

		// Test reference classifier
		compareRefClassifiers(tsRefClassifier, seed, trainingPortion, tsRefClassifierParams, result, arffFiles);

		// Load dataset
		Pair<TimeSeriesDataset, ClassMapper> dataset = loadDatasetFromArffFiles(arffFiles);

		// TODO
		Pair<TimeSeriesDataset, TimeSeriesDataset> trainTest = null; // jaicore.ml.tsc.util.WekaUtil.getStratifiedSplit(dataset,
		// trainingPortion);
		TimeSeriesDataset train = trainTest.getX();
		TimeSeriesDataset test = trainTest.getY();

		trainAndEvaluateClassifier(tsClassifier, seed, tsClassifierParams, result, train, test);

		return result;
	}

	/**
	 * Trains and evaluates a given <code>TSClassifier</code> object with regard to
	 * the accuracy score using the given data from file <code>arffFile</code>
	 * partitioned into a fraction of <code>trainingPortion</code> as training data.
	 * 
	 * @param tsClassifier
	 *            Time series classifier to be trained and evaluated
	 * @param seed
	 *            Seed used for randomized splitting the given data into train and
	 *            test set
	 * @param tsClassifierParams
	 *            Textual description of the time series classifier parameters to be
	 *            stored in the database
	 * @param result
	 *            Map used to store the database entry's information
	 * @param train
	 *            Training instances
	 * @param test
	 *            Test instances
	 * @throws TrainingException
	 *             Will be thrown if the training of <code>tsClassifier</code> fails
	 * @throws PredictionException
	 *             Will be thrown if the prediction of <code>tsClassifier</code>
	 *             fails
	 */
	private static void trainAndEvaluateClassifier(final ASimplifiedTSClassifier<?> tsClassifier, final int seed,
			final String tsClassifierParams, final Map<String, Object> result, final TimeSeriesDataset train,
			final TimeSeriesDataset test) throws TrainingException, PredictionException {

		result.put("classifier", tsClassifier.getClass().getSimpleName());
		result.put("classifier_params", tsClassifierParams);

		// Training
		LOGGER.debug("Starting training of classifier...");
		long timeStart = System.currentTimeMillis();
		tsClassifier.train(train);
		final long trainingEnd = System.currentTimeMillis();
		LOGGER.debug("Finished training of classifier. Took {} ms.", (trainingEnd - timeStart));
		result.put("train_time", (trainingEnd - timeStart));

		// Evaluation
		LOGGER.debug("Starting evaluation of classifier...");
		timeStart = System.currentTimeMillis();

		List<?> predictions = tsClassifier.predict(test);
		int totalPreds = predictions.size();
		int correct = 0;

		if (totalPreds <= 0)
			throw new PredictionException("Nothing has been predicted.");

		if (!(predictions.get(0) instanceof Integer || predictions.get(0) instanceof String))
			throw new PredictionException("Can not evaluate classifier due to an unsupported target type.");

		if (predictions.get(0) instanceof Integer) {
			for (int i = 0; i < totalPreds; i++) {
				int prediction = (int) predictions.get(i);
				if (prediction == test.getTargets()[i])
					correct++;
			}
		} else if (predictions.get(0) instanceof String) {
			// for (int i = 0; i < totalPreds; i++) {
			// String prediction = (String) predictions.get(i);
			// TODO: Add mapper
			throw new UnsupportedOperationException("Not implemented yet.");
			// if (prediction.equals(test.getTargets()[i]))
			// correct++;
			// }
		}

		double accuracy = (double) correct / totalPreds;

		final long evaluationEnd = System.currentTimeMillis();
		LOGGER.debug("Finished evaluation of classifier. Took {} ms. Accuracy: {}", (evaluationEnd - timeStart),
				accuracy);
		result.put("eval_time", (evaluationEnd - timeStart));
		result.put("accuracy", accuracy);
	}

	/**
	 * Loads a (simplified) time series dataset from an arbitrary number of arff
	 * files (multiple will be handled as multivariate dataset).
	 * 
	 * @param arffFiles
	 *            Arff files to be loaded
	 * @return Returns a TimeSeriesDataset object storing the arff data
	 * @throws TimeSeriesLoadingException
	 *             Thrown if the dataset could not be parsed
	 */
	private static Pair<TimeSeriesDataset, ClassMapper> loadDatasetFromArffFiles(final File... arffFiles)
			throws TimeSeriesLoadingException {
		Pair<TimeSeriesDataset, ClassMapper> result;

		try {
			if (arffFiles.length < 1)
				throw new IllegalArgumentException("At least one arff file must be given!");
			else if (arffFiles.length == 1) {
				result = SimplifiedTimeSeriesLoader.loadArff(arffFiles[0]);
			} else {
				result = SimplifiedTimeSeriesLoader.loadArffs(arffFiles);
			}
		} catch (TimeSeriesLoadingException e) {
			throw new TimeSeriesLoadingException("Could not load training dataset.", e);
		}
		return result;
	}

	/**
	 * Function creating pairs of own and the corresponding reference classifiers
	 * using default parameters.
	 * 
	 * @param algorithm
	 *            Name of the algorithm / model
	 * @param seed
	 *            Seed used for randomized operations
	 * @param timeout
	 *            Timeout used within the training
	 * @return Returns a pair of created and parameterized classifiers
	 */
	public static Pair<ASimplifiedTSClassifier<Integer>, Object> createClassifierPairsWithDefaultParameter(
			final String algorithm, final int seed, final TimeOut timeout) {
		ASimplifiedTSClassifier<Integer> ownClassifier = null;
		Object refClassifier = null;

		switch (algorithm) {
		case "ShapeletTransform":
			int k = 205;
			final int minShapeletLength = 3;
			final int maxShapeletLength = 23;

			refClassifier = new ShapeletTransformClassifier();
			((ShapeletTransformClassifier) refClassifier).setSeed(seed);
			((ShapeletTransformClassifier) refClassifier).setNumberOfShapelets(k);

			ownClassifier = new ShapeletTransformTSClassifier(k, new FStat(), (int) seed, false, minShapeletLength,
					maxShapeletLength, true, timeout, 10);
			break;

		case "LearnShapelets":
			// Initialize classifiers with values selected by reference classifier by
			// default
			int K = 8;
			double learningRate = 0.1;
			double regularization = 0.01;
			int scaleR = 3;
			double minShapeLength = 0.2;
			int maxIter = 600;
			double gamma = 0.5;

			ownClassifier = new LearnShapeletsClassifier(K, learningRate, regularization, scaleR, minShapeLength,
					maxIter, gamma, (int) seed);

			refClassifier = new LearnShapelets();
			((LearnShapelets) refClassifier).setSeed(seed);
			((LearnShapelets) refClassifier).fixParameters();
			break;
		case "TimeSeriesForest":
			int numTrees = 500;
			// Ref classifier uses no depth limit
			int maxDepth = 1000;
			int numCPUs = 1;

			ownClassifier = new TimeSeriesForestClassifier(numTrees, maxDepth, (int) seed, false, numCPUs, timeout);

			refClassifier = new TSF((int) seed);
			((TSF) refClassifier).setNumTrees(numTrees);
			break;
		case "TimeSeriesBagOfFeatures":
			int numBins = 10;
			int numFolds = 10;
			double zProp = 0.1;
			int minIntervalLength = 5;

			ownClassifier = new TimeSeriesBagOfFeaturesClassifier((int) seed, numBins, numFolds, zProp,
					minIntervalLength);

			TSBF refClf = new TSBF();
			refClf.seedRandom(seed);
			try {
				FieldUtils.writeField(refClf, "stepWise", false, true);
				FieldUtils.writeField(refClf, "numReps", 1, true);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException("Cannot test TSBF reference classifier due to parameter set problems.");
			}
			refClf.setParamSearch(false);
			refClf.searchParameters(false);

			refClassifier = refClf;

			break;
		default:
			String errorString = String.format("Please specify a valid algorithm. An invalid value was given: %s",
					algorithm);
			LOGGER.error(errorString);
			throw new IllegalArgumentException(errorString);
		}

		return new Pair<ASimplifiedTSClassifier<Integer>, Object>(ownClassifier, refClassifier);
	}

	public static Pair<ASimplifiedTSClassifier<Integer>, Object> createClassifierPairsWithSpecificParameter(
			final Map<String, String> parameters, final TimeOut timeOut) {
		ASimplifiedTSClassifier<Integer> ownClassifier = null;
		Object refClassifier = null;

		final int seed = Integer.parseInt(parameters.get("seed"));
		final String algorithm = parameters.get("algorithm");

		System.out.println(parameters);

		switch (algorithm) {
		case "ShapeletTransform":
			int k = Integer.parseInt(parameters.get("st_k"));
			final int minShapeletLength = Integer.parseInt(parameters.get("st_minShapeletLength"));
			final int maxShapeletLength = Integer.parseInt(parameters.get("st_maxShapeletLength"));

			refClassifier = new ShapeletTransformClassifier();
			((ShapeletTransformClassifier) refClassifier).setSeed(seed);
			((ShapeletTransformClassifier) refClassifier).setNumberOfShapelets(k);

			ownClassifier = new ShapeletTransformTSClassifier(k, new FStat(), (int) seed, false, minShapeletLength,
					maxShapeletLength, true, timeOut, 10);
			break;

		case "LearnShapelets":
			// Initialize classifiers with values selected by reference classifier by
			// default
			int K = Integer.parseInt(parameters.get("ls_k"));
			double learningRate = Double.parseDouble(parameters.get("ls_learningRate"));
			double regularization = Double.parseDouble(parameters.get("ls_regularization"));
			int scaleR = Integer.parseInt(parameters.get("ls_scaleR"));
			double minShapeLength = Double.parseDouble(parameters.get("ls_minShapeLength"));
			int maxIter = Integer.parseInt(parameters.get("ls_maxIteration"));
			double gamma = 0.5d;

			ownClassifier = new LearnShapeletsClassifier(K, learningRate, regularization, scaleR, minShapeLength,
					maxIter, gamma, (int) seed);
			((LearnShapeletsClassifier) ownClassifier).setEstimateK(true); // As used in reference implementation

			refClassifier = new LearnShapelets();
			((LearnShapelets) refClassifier).setSeed(seed);

			((LearnShapelets) refClassifier).K = K;
			((LearnShapelets) refClassifier).eta = learningRate;
			((LearnShapelets) refClassifier).lambdaW = regularization;
			((LearnShapelets) refClassifier).R = scaleR;
			((LearnShapelets) refClassifier).percentageOfSeriesLength = minShapeLength;
			((LearnShapelets) refClassifier).maxIter = maxIter / 2; // Doubles the number of iterations

			// Since parts of the parameters set above are overwritten, the param search
			// approach is used to reuse the reference implementation with different
			// parameters
			try {
				((LearnShapelets) refClassifier).setParamSearch(true);
				FieldUtils.writeField(refClassifier, "lambdaWRange", new double[] { regularization }, true);
				FieldUtils.writeField(refClassifier, "percentageOfSeriesLengthRange", new double[] { minShapeLength },
						true);
				FieldUtils.writeField(refClassifier, "shapeletLengthScaleRange", new int[] { scaleR }, true);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException(
						"Cannot test Learn Shapelets reference classifier due to parameter set problems.");
			}

			break;
		case "TimeSeriesForest":
			int numTrees = Integer.parseInt(parameters.get("tsf_numTree"));
			// Ref classifier uses no depth limit
			int maxDepth = Integer.parseInt(parameters.get("tsf_maxDepth"));
			int numCPUs = 1;

			ownClassifier = new TimeSeriesForestClassifier(numTrees, maxDepth, (int) seed, false, numCPUs, timeOut);

			refClassifier = new TSF((int) seed);
			((TSF) refClassifier).setNumTrees(numTrees);

			break;
		case "TimeSeriesBagOfFeatures":
			int numBins = Integer.parseInt(parameters.get("tsbf_numBin"));
			int numFolds = Integer.parseInt(parameters.get("tsbf_numFold"));
			double zProp = Double.parseDouble(parameters.get("tsbf_zProp"));
			int minIntervalLength = Integer.parseInt(parameters.get("tsbf_minIntervalLength"));

			ownClassifier = new TimeSeriesBagOfFeaturesClassifier((int) seed, numBins, numFolds, zProp,
					minIntervalLength);

			TSBF refClf = new TSBF();
			refClf.seedRandom(seed);
			try {
				FieldUtils.writeField(refClf, "stepWise", false, true);
				FieldUtils.writeField(refClf, "numReps", 1, true);
				FieldUtils.writeField(refClf, "numBins", numBins, true);
				FieldUtils.writeField(refClf, "folds", numFolds, true);
				FieldUtils.writeField(refClf, "z", zProp, true);
				FieldUtils.writeField(refClf, "minIntervalLength", minIntervalLength, true);

			} catch (IllegalAccessException e) {
				throw new IllegalStateException("Cannot test TSBF reference classifier due to parameter set problems.");
			}
			refClf.setParamSearch(false);
			refClf.searchParameters(false);

			refClassifier = refClf;

			break;
		case "LearnPatternSimilarity":

			int numLPSTrees = Integer.parseInt(parameters.get("lps_numTree"));
			int maxLPSDepth = Integer.parseInt(parameters.get("lps_maxTreeDepth"));
			int numSegments = Integer.parseInt(parameters.get("lps_numSegment"));

			ownClassifier = new LearnPatternSimilarityClassifier((int) seed, numLPSTrees, maxLPSDepth, numSegments);

			LPS refLPSClf = new LPS();
			refLPSClf.setParamSearch(false);
			try {
				FieldUtils.writeField(refLPSClf, "nosTrees", numLPSTrees, true);
				FieldUtils.writeField(refLPSClf, "treeDepth", maxLPSDepth, true);
				FieldUtils.writeField(refLPSClf, "nosSegments", numSegments, true);
				FieldUtils.writeField(refLPSClf, "ratioLevel", 0.01d, true);

			} catch (IllegalAccessException e) {
				throw new IllegalStateException("Cannot test TSBF reference classifier due to parameter set problems.");
			}

			refClassifier = refLPSClf;

			break;
		default:
			String errorString = String.format("Please specify a valid algorithm. An invalid value was given: %s",
					algorithm);
			LOGGER.error(errorString);
			throw new IllegalArgumentException(errorString);
		}

		return new Pair<ASimplifiedTSClassifier<Integer>, Object>(ownClassifier, refClassifier);
	}
}
