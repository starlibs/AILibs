package jaicore.ml.tsc.classifier.trees;

import java.util.ArrayList;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.algorithm.IRandomAlgorithmConfig;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;
import jaicore.ml.core.exception.PredictionException;
import jaicore.ml.tsc.classifier.ASimplifiedTSCLearningAlgorithm;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Algorithm training a {@link LearnPatternSimilarityClassifier} as described in
 * Baydogan, Mustafa & Runger, George. (2015). Time series representation and
 * similarity based on local autopatterns. Data Mining and Knowledge Discovery.
 * 30. 1-34. 10.1007/s10618-015-0425-y.
 *
 * @author Julian Lienen
 *
 */
public class LearnPatternSimilarityLearningAlgorithm extends ASimplifiedTSCLearningAlgorithm<Integer, LearnPatternSimilarityClassifier> {

	public interface IPatternSimilarityConfig extends IRandomAlgorithmConfig {
		public static final String K_NUMTREES = "numtrees";
		public static final String K_MAXDEPTH = "maxdepth";
		public static final String K_NUMSEGMENTS = "numsegments";

		/**
		 * Number of trees to be trained.
		 */
		@Key(K_NUMTREES)
		@DefaultValue("-1")
		public int numTrees();

		/**
		 * Maximum depth of the trained trees.
		 */
		@Key(K_MAXDEPTH)
		@DefaultValue("-1")
		public int maxDepth();

		/**
		 * Number of segments used for feature generation for each tree.
		 */
		@Key(K_NUMSEGMENTS)
		@DefaultValue("1")
		public int numSegments();
	}

	/**
	 * Log4j logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(LearnPatternSimilarityLearningAlgorithm.class);

	/**
	 * Standard constructor.
	 *
	 * @param seed
	 *            See {@link LearnPatternSimilarityLearningAlgorithm#seed}.
	 * @param numTrees
	 *            See {@link LearnPatternSimilarityLearningAlgorithm#numTrees}.
	 * @param maxTreeDepth
	 *            See {@link LearnPatternSimilarityLearningAlgorithm#maxTreeDepth}.
	 * @param numSegments
	 *            See {@link LearnPatternSimilarityLearningAlgorithm#numSegments}.
	 */
	public LearnPatternSimilarityLearningAlgorithm(final IPatternSimilarityConfig config, final LearnPatternSimilarityClassifier model, final TimeSeriesDataset dataset) {
		super(config, model, dataset);
	}

	/**
	 * Training procedure for a {@link LearnPatternSimilarityClassifier}. At first,
	 * it generates randomly subsequences (segments) and subsequence difference
	 * locations used for feature generation. The generated features are used to
	 * train a forest of {@link RandomRegressionTree} models. The predicted leaf
	 * nodes are stored in the model for a 1NN search for an equally generated
	 * prediction leaf node matrix.
	 * @throws AlgorithmException
	 * @throws AlgorithmTimeoutedException
	 */
	@Override
	public LearnPatternSimilarityClassifier call() throws AlgorithmException, AlgorithmTimeoutedException {
		// Training procedure
		long beginTimeMs = System.currentTimeMillis();

		TimeSeriesDataset data = this.getInput();
		if (data == null || data.isEmpty()) {
			throw new IllegalStateException("The time series input data must not be null or empty!");
		}

		final double[][] dataMatrix = data.getValuesOrNull(0);
		if (dataMatrix == null) {
			throw new IllegalArgumentException("Value matrix must be a valid 2D matrix containing the time series values for all instances!");
		}
		final int[] targetMatrix = data.getTargets();

		final int timeSeriesLength = dataMatrix[0].length;

		int minLength = (int) (0.1d * timeSeriesLength);
		int maxLength = (int) (0.9d * timeSeriesLength);

		Random random = new Random(this.getConfig().seed());
		int numTrees = this.getConfig().numTrees();
		int numSegments = this.getConfig().numSegments();
		final int[][] segments = new int[numTrees][numSegments]; // Refers to matrix A in tsc algorithm
		// description
		final int[][] segmentsDifference = new int[numTrees][numSegments]; // Refers to matrix B in tsc
		// algorithm description

		final int[] lengthPerTree = new int[numTrees];
		final int[] classAttIndex = new int[numTrees];

		final RandomRegressionTree[] trees = new RandomRegressionTree[numTrees];
		final int[] numLeavesPerTree = new int[numTrees];
		final int[][][] leafNodeCounts = new int[data.getNumberOfInstances()][numTrees][];

		ArrayList<Attribute> attributes = new ArrayList<>();
		for (int j = 0; j < 2 * numSegments; j++) {
			attributes.add(new Attribute("val" + j));
		}

		for (int i = 0; i < numTrees; i++) {
			if ((System.currentTimeMillis() - beginTimeMs) > this.getTimeout().milliseconds()) {
				throw new AlgorithmTimeoutedException((System.currentTimeMillis() - beginTimeMs) - this.getTimeout().milliseconds());
			}

			// Generate subseries length
			lengthPerTree[i] = random.nextInt(maxLength - minLength) + minLength;

			// Generate random subseries locations as described in chapter 3.1 and random
			// subseries difference locations as described in chapter 3.4
			this.generateSegmentsAndDifferencesForTree(segments[i], segmentsDifference[i], lengthPerTree[i], timeSeriesLength, random);

			// Generate subseries features
			Instances seqInstances = generateSubseriesFeaturesInstances(attributes, lengthPerTree[i], segments[i], segmentsDifference[i], dataMatrix);

			classAttIndex[i] = random.nextInt(attributes.size());
			seqInstances.setClassIndex(classAttIndex[i]);

			trees[i] = this.initializeRegressionTree(seqInstances.numInstances());

			try {
				trees[i].buildClassifier(seqInstances);
			} catch (Exception e) {
				e.printStackTrace();
				throw new AlgorithmException("Could not build tree in iteration " + i + " due to the following exception: " + e.getMessage());
			}

			numLeavesPerTree[i] = trees[i].nosLeafNodes;
			for (int inst = 0; inst < data.getNumberOfInstances(); inst++) {
				leafNodeCounts[inst][i] = new int[numLeavesPerTree[i]];

				for (int len = 0; len < lengthPerTree[i]; len++) {
					int instanceIdx = inst * lengthPerTree[i] + len;

					try {
						collectLeafCounts(leafNodeCounts[inst][i], seqInstances.get(instanceIdx), trees[i]);
					} catch (PredictionException e1) {
						e1.printStackTrace();
						throw new AlgorithmException("Could not prediction using the tree in iteration " + i + " due to the following exception: " + e1.getMessage());
					}
				}
			}
		}

		// Update model
		LearnPatternSimilarityClassifier model = this.getClassifier();
		model.setSegments(segments);
		model.setSegmentsDifference(segmentsDifference);
		model.setLengthPerTree(lengthPerTree);
		model.setClassAttIndexPerTree(classAttIndex);
		model.setTrees(trees);
		model.setTrainLeafNodes(leafNodeCounts);
		model.setTrainTargets(targetMatrix);
		model.setAttributes(attributes);

		return model;
	}

	/**
	 * Method generating the segment start indices and the segment difference
	 * locations randomly using <code>random</code>. This method used the
	 * <code>length</code> to specify the interval being generated.
	 * {@link LearnPatternSimilarityLearningAlgorithm#numSegments} * 2 many indices are
	 * generated. The start indices can only be between
	 * <code>[0, timeSeriesLength - length]</code> and the segment differences can
	 * only between <code>[0, timeSeriesLength - length -1]</code> (-1 due to the
	 * fact that the next index is used for the difference calculation).
	 *
	 * @param segments
	 *            Segment start indices used for feature generation
	 * @param segmentsDifference
	 *            Segment difference start indices used for feature generation
	 * @param length
	 *            The length of the segments
	 * @param timeSeriesLength
	 *            The total length of the complete time series
	 * @param random
	 *            Generator for the random numbers
	 */
	public void generateSegmentsAndDifferencesForTree(final int[] segments, final int[] segmentsDifference, final int length, final int timeSeriesLength, final Random random) {
		for (int i = 0; i < this.getConfig().numSegments(); i++) {
			segments[i] = random.nextInt(timeSeriesLength - length); // Length is always l
			segmentsDifference[i] = random.nextInt(timeSeriesLength - length - 1);
		}
	}

	/**
	 * Initializes a new instance of {@link RandomRegressionTree}.
	 *
	 * @param numInstances
	 *            The number of instance used for latter training (used for setting
	 *            the minimum number of instances per leaf)
	 * @return Returns the initialized tree
	 */
	public RandomRegressionTree initializeRegressionTree(final int numInstances) {
		RandomRegressionTree regTree = new RandomRegressionTree();
		regTree.setSeed(this.getConfig().seed());
		regTree.setMaxDepth(this.getConfig().maxDepth());
		regTree.setKValue(1);
		regTree.setMinNum((int) (numInstances * 0.01));
		return regTree;
	}

	/**
	 * Function collecting the leaf counts for the given <code>instance</code> as
	 * predicted by <code>regTree</code>. The result is stored at the induced leaf
	 * node index in <code>leafNodeCountsForInstance</code>.
	 *
	 * @param leafNodeCountsForInstance
	 *            The vector storing the frequencies for each leaf node of the tree
	 *            being the last node within the prediction
	 * @param instance
	 *            The given Weka instance which is fed to the <code>regTree</code>
	 * @param regTree
	 *            The regression tree used for prediction
	 * @throws PredictionException
	 *             Thrown if the random regression tree could not predict anything
	 *             for the given <code>instance</code>
	 */
	public static void collectLeafCounts(final int[] leafNodeCountsForInstance, final Instance instance, final RandomRegressionTree regTree) throws PredictionException {
		try {
			regTree.distributionForInstance(instance);
		} catch (Exception e) {
			throw new PredictionException("Could not predict the distribution for instance for the given instance '" + instance.toString() + "' due to an internal Weka exception.", e);
		}
		int leafNodeIdx = RandomRegressionTree.lastNode;
		leafNodeCountsForInstance[leafNodeIdx]++;
	}

	/**
	 * Function generating a dataset storing the features being generated as
	 * described in the original paper. The <code>segments</code> and
	 * <code>segmentsDifference</code> indices are used to extract subsequences of
	 * the given <code>dataMatrix</code> and generating value differences,
	 * respectively.
	 *
	 * @param attributes
	 *            The attributes used by Weka to create the dataset
	 * @param length
	 *            The length considered for the feature generation
	 * @param segments
	 *            Segment start indices used for feature generation
	 * @param segmentsDifference
	 *            Segment difference start indices used for feature generation
	 * @param dataMatrix
	 *            Matrix storing the instance values used for feature generation
	 * @return Returns Weka instances storing the generated features
	 */
	public static Instances generateSubseriesFeaturesInstances(final ArrayList<Attribute> attributes, final int length, final int[] segments, final int[] segmentsDifference, final double[][] dataMatrix) {
		Instances seqInstances = new Instances("SeqFeatures", attributes, dataMatrix.length * length);
		for (int inst = 0; inst < dataMatrix.length; inst++) {
			double[] instValues = dataMatrix[inst];
			for (int len = 0; len < length; len++) {
				seqInstances.add(generateSubseriesFeatureInstance(instValues, segments, segmentsDifference, len));
			}
		}
		return seqInstances;
	}

	/**
	 * Function generating subseries feature instances based on the given
	 * <code>segments</code> and <code>segmentsDifference</code> matrices. The
	 * <code>len</code> parameter indicates which subsequence instance is generated
	 * within this call. The values are extracted and used for calculation (for
	 * difference) from <code>instValues</code>.
	 *
	 * @param instValues
	 *            Instance values used for feature generation
	 * @param segments
	 *            Segment start indices used for feature generation
	 * @param segmentsDifference
	 *            Segment difference start indices used for feature generation
	 * @param len
	 *            Current length (is added to the segment and segment difference
	 *            locations)
	 * @return Returns a Weka instance storing the generated features
	 */
	public static Instance generateSubseriesFeatureInstance(final double[] instValues, final int[] segments, final int[] segmentsDifference, final int len) {
		if (segments.length != segmentsDifference.length) {
			throw new IllegalArgumentException("The number of segments and the number of segments differences must be the same!");
		}
		if (instValues.length < len) {
			throw new IllegalArgumentException("If the segments' length is set to '" + len + "', the number of time series variables must be greater or equals!");
		}

		DenseInstance instance = new DenseInstance(2 * segments.length);
		for (int seq = 0; seq < segments.length; seq++) {
			instance.setValue(seq * 2, instValues[segments[seq] + len]);

			double difference = instValues[segmentsDifference[seq] + len + 1] - instValues[segmentsDifference[seq] + len];
			instance.setValue(seq * 2 + 1, difference);
		}
		return instance;
	}

	@Override
	public AlgorithmEvent nextWithException() {
		throw new UnsupportedOperationException("Currently no step-wise execution possible.");
	}

	@Override
	public IPatternSimilarityConfig getConfig() {
		return (IPatternSimilarityConfig)super.getConfig();
	}
}
