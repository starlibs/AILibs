package jaicore.ml.tsc.classifier.shapelets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;

import jaicore.basic.TimeOut;
import jaicore.basic.algorithm.IAlgorithm;
import jaicore.basic.algorithm.IRandomAlgorithmConfig;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.ml.core.exception.TrainingException;
import jaicore.ml.tsc.classifier.ASimplifiedTSCLearningAlgorithm;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.util.MathUtil;
import jaicore.ml.tsc.util.TimeSeriesUtil;
import jaicore.ml.tsc.util.WekaUtil;
import weka.clusterers.SimpleKMeans;
import weka.core.Instances;

/**
 * Generalized Shapelets Learning implementation for
 * <code>LearnShapeletsClassifier</code> published in "J. Grabocka, N.
 * Schilling, M. Wistuba, L. Schmidt-Thieme: Learning Time-Series Shapelets"
 * (https://www.ismll.uni-hildesheim.de/pub/pdfs/grabocka2014e-kdd.pdf)
 *
 * @author Julian Lienen
 *
 */
public class LearnShapeletsLearningAlgorithm extends ASimplifiedTSCLearningAlgorithm<Integer, LearnShapeletsClassifier> {

	public interface ILearnShapeletsLearningAlgorithmConfig extends IRandomAlgorithmConfig {

		public static final String K_NUMSHAPELETS = "numshapelets";
		public static final String K_LEARNINGRATE = "learningrate";
		public static final String K_REGULARIZATION = "regularization";
		public static final String K_SHAPELETLENGTH_MIN = "minshapeletlength";
		public static final String K_SHAPELETLENGTH_RELMIN = "relativeminshapeletlength";
		public static final String K_SCALER = "scaler";
		public static final String K_MAXITER = "maxiter";
		public static final String K_GAMMA = "gamma";
		public static final String K_ESTIMATEK = "estimatek";

		/**
		 * Parameter which determines how many of the most-informative shapelets should be used.
		 * Corresponds to K in the paper
		 */
		@Key(K_NUMSHAPELETS)
		public int numShapelets();

		/**
		 * The learning rate used within the SGD.
		 */
		@Key(K_LEARNINGRATE)
		public double learningRate();

		/**
		 * The regularization used wihtin the SGD.
		 */
		@Key(K_REGULARIZATION)
		public double regularization();

		/**
		 * The minimum shapelet of the shapelets to be learned. Internally derived by
		 * the time series lengths and the <code>minShapeLengthPercentage</code>.
		 */
		@Key(K_SHAPELETLENGTH_MIN)
		public int minShapeletLength();

		/**
		 * The minimum shape length percentage used to calculate the minimum shape length.
		 */
		@Key(K_SHAPELETLENGTH_RELMIN)
		public double minShapeLengthPercentage();

		/**
		 * The number of scales used for the shapelet lengths.
		 */
		@Key(K_SCALER)
		public int scaleR();

		/**
		 * The maximum iterations used for the SGD.
		 */
		@Key(K_MAXITER)
		public int maxIterations();


		/**
		 * Gamma value used for momentum during gradient descent. Defaults to 0.5.
		 */
		@Key(K_GAMMA)
		@DefaultValue("0.5")
		public double gamma();


		/**
		 * Parameter indicator whether estimation of K (number of learned shapelets)
		 * should be derived from the number of total segments. False by default.
		 */
		@Key(K_ESTIMATEK)
		@DefaultValue("false")
		public boolean estimateK();
	}

	/**
	 * The log4j logger.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(LearnShapeletsLearningAlgorithm.class);

	/**
	 * The number of instances.
	 */
	private int I;
	/**
	 * The number of attributes (i. e. the time series lengths without the class
	 * attribute).
	 */
	private int Q;
	/**
	 * The number of classes.
	 */
	private int C;

	/**
	 * Indicator whether Bessel's correction should be used when normalizing arrays.
	 */
	public static final boolean USE_BIAS_CORRECTION = false;

	/**
	 * Predefined alpha parameter used within the calculations.
	 */
	public static double ALPHA = -30d; // Used in implementation. Paper says -100d

	/**
	 * Epsilon value used to prevent dividing by zero occurrences.
	 */
	private static double EPS = 0.000000000000000000001d;

	/**
	 * See {@link IAlgorithm#getTimeout()}.
	 */
	private TimeOut timeout = new TimeOut(Integer.MAX_VALUE, TimeUnit.SECONDS);


	/**
	 * Indicator whether instances used for training should be reordered s.t. the
	 * classes are used in an alternating manner.
	 */
	private boolean useInstanceReordering = true;


	/**
	 * Constructor of the algorithm to train a {@link LearnShapeletsClassifier}.
	 *
	 */
	public LearnShapeletsLearningAlgorithm(final ILearnShapeletsLearningAlgorithmConfig config, final LearnShapeletsClassifier classifier, final TimeSeriesDataset dataset) {
		super(config, classifier, dataset);
	}

	/**
	 * Initializes the tensor <code>S</code> storing the shapelets for each scale.
	 * The initialization is done by deriving inital shapelets from all normalized
	 * segments.
	 *
	 * @param trainingMatrix
	 *            The training matrix used for the initialization of <code>S</code>.
	 * @return Return the initialized tensor storing an initial guess for the
	 *         shapelets based on the clustering
	 * @throws TrainingException
	 */
	public double[][][] initializeS(final double[][] trainingMatrix) throws TrainingException {
		LOGGER.debug("Initializing S...");

		/* read config locally */
		final int scaleR = this.getConfig().scaleR();
		final int seed = this.getConfig().seed();
		final int minShapeLength = this.getConfig().minShapeletLength();

		final double[][][] result = new double[scaleR][][];

		for (int r = 0; r < scaleR; r++) {
			final int numberOfSegments = getNumberOfSegments(this.Q, minShapeLength, r);
			if (numberOfSegments < 1) {
				throw new TrainingException("The number of segments is lower than 1. Can not train the LearnShapelets model.");
			}

			final int L = (r + 1) * minShapeLength;

			final double[][] tmpSegments = new double[trainingMatrix.length * numberOfSegments][L];

			// Prepare training data for finding the centroids
			for (int i = 0; i < trainingMatrix.length; i++) {
				for (int j = 0; j < numberOfSegments; j++) {
					for (int l = 0; l < L; l++) {
						tmpSegments[i * numberOfSegments + j][l] = trainingMatrix[i][j + l];
					}
					tmpSegments[i * numberOfSegments + j] = TimeSeriesUtil.zNormalize(tmpSegments[i * numberOfSegments + j], USE_BIAS_CORRECTION);
				}
			}

			// Transform instances
			Instances wekaInstances = WekaUtil.matrixToWekaInstances(tmpSegments);

			// Cluster using k-Means
			SimpleKMeans kMeans = new SimpleKMeans();
			try {
				kMeans.setNumClusters(this.getConfig().numShapelets());
				kMeans.setSeed(seed);
				kMeans.setMaxIterations(100);
				kMeans.buildClusterer(wekaInstances);
			} catch (Exception e) {
				LOGGER.warn("Could not initialize matrix S using kMeans clustering for r={} due to the following problem: {}. " + "Using zero matrix instead (possibly leading to a poor training performance).", r, e.getMessage());
				result[r] = new double[this.getConfig().numShapelets()][r * minShapeLength];
				continue;
			}
			Instances clusterCentroids = kMeans.getClusterCentroids();

			double[][] tmpResult = new double[clusterCentroids.numInstances()][clusterCentroids.numAttributes()];
			for (int i = 0; i < tmpResult.length; i++) {
				double[] instValues = clusterCentroids.get(i).toDoubleArray();
				for (int j = 0; j < tmpResult[i].length; j++) {
					tmpResult[i][j] = instValues[j];
				}
			}
			result[r] = tmpResult;
		}

		LOGGER.debug("Initialized S.");

		return result;
	}

	/**
	 * Main function to train a <code>LearnShapeletsClassifier</code>.
	 *
	 * @throws AlgorithmException
	 */
	@Override
	public LearnShapeletsClassifier call() throws AlgorithmException {
		// Training
		long beginTime = System.currentTimeMillis();

		TimeSeriesDataset data = this.getInput();

		if (data.isMultivariate()) {
			throw new UnsupportedOperationException("Multivariate datasets are not supported.");
		}
		if (data.isEmpty()) {
			throw new IllegalArgumentException("The training dataset must not be null!");
		}

		final double[][] dataMatrix = data.getValuesOrNull(0);
		if (dataMatrix == null) {
			throw new IllegalArgumentException("Timestamp matrix must be a valid 2D matrix containing the time series values for all instances!");
		}

		// Get occurring classes which can be used for index extraction
		final int[] targetMatrix = data.getTargets();
		final List<Integer> occuringClasses = TimeSeriesUtil.getClassesInDataset(data);

		this.I = data.getNumberOfInstances(); // I
		this.Q = dataMatrix[0].length; // Q
		this.C = occuringClasses.size(); // C

		/* update knowledge about the absolute min length of the shapelets */
		this.getConfig().setProperty(ILearnShapeletsLearningAlgorithmConfig.K_SHAPELETLENGTH_MIN, "" + (this.getConfig().minShapeLengthPercentage() * this.Q));
		final int minShapeLength = this.getConfig().minShapeletLength();
		final int scaleR = this.getConfig().scaleR();

		// Prepare binary classes
		int[][] Y = new int[this.I][this.C];
		for (int i = 0; i < this.I; i++) {
			Integer instanceClass = targetMatrix[i];
			Y[i][occuringClasses.indexOf(instanceClass)] = 1;
		}

		// Estimate parameter K by the maximum number of segments
		if (this.getConfig().estimateK()) {
			int totalSegments = 0;
			for (int r = 0; r < scaleR; r++) {
				final int numberOfSegments = getNumberOfSegments(this.Q, minShapeLength, r);
				totalSegments += numberOfSegments * this.I;
			}

			int k = (int)(Math.log(totalSegments) * (this.C - 1));
			this.getConfig().setProperty(ILearnShapeletsLearningAlgorithmConfig.K_NUMSHAPELETS, "" +  (k >= 0 ? k : 1));
		}
		final int k = this.getConfig().numShapelets();
		LOGGER.info("Parameters: k={}, learningRate={}, reg={}, r={}, minShapeLength={}, maxIter={}, Q={}, C={}", k, this.getConfig().learningRate(), this.getConfig().regularization(), scaleR, this.getConfig().minShapeletLength(), this.getConfig().maxIterations(), this.Q, this.C);

		// Initialization
		double[][][] S;
		try {
			S = this.initializeS(dataMatrix);
		} catch (TrainingException e) {
			throw new AlgorithmException(e, "Can not train LearnShapelets model due to error during initialization of S.");
		}
		double[][][] S_hist = new double[scaleR][][];
		for (int r = 0; r < scaleR; r++) {
			S_hist[r] = new double[S[r].length][S[r][0].length];
		}

		// Initializes the given weights nearly around zeros (as opposed to the paper
		// due to vanish effects)
		double[][][] W = new double[this.C][scaleR][k];
		double[][][] W_hist = new double[this.C][scaleR][k];
		double[] W_0 = new double[this.C];
		double[] W_0_hist = new double[this.C];
		this.initializeWeights(W, W_0);

		// Perform stochastic gradient descent
		LOGGER.debug("Starting training for {} iterations...", this.getConfig().maxIterations());
		this.performSGD(W, W_hist, W_0, W_0_hist, S, S_hist, dataMatrix, Y, beginTime, targetMatrix);
		LOGGER.debug("Finished training.");

		// Update model
		LearnShapeletsClassifier model = this.getClassifier();
		model.setS(S);
		model.setW(W);
		model.setW_0(W_0);
		model.setC(this.C);
		return model;
	}

	/**
	 * Randomly initializes the weights around zero. As opposed to the paper, the
	 * approach has been changed to a different standard deviation as used in the
	 * reference implementation for performance reasons.
	 *
	 * @param W
	 *            The weight matrix
	 * @param W_0
	 *            The bias vector
	 */
	public void initializeWeights(final double[][][] W, final double[] W_0) {
		Random rand = new Random(this.getConfig().seed());
		final int scaleR = this.getConfig().scaleR();
		final int numShapelets = this.getConfig().numShapelets();
		for (int i = 0; i < this.C; i++) {
			W_0[i] = EPS * rand.nextDouble() * Math.pow(-1, rand.nextInt(2));
			for (int j = 0; j < scaleR; j++) {
				for (int k = 0; k < numShapelets; k++) {
					W[i][j][k] = EPS * rand.nextDouble() * Math.pow(-1, rand.nextInt(2));
				}
			}
		}
	}

	/**
	 * Method performing the stochastic gradient descent to learn the weights and
	 * shapelets.
	 *
	 * @param W
	 *            The weight matrix
	 * @param W_hist
	 *            The weight's history matrix used for smoothing learning
	 * @param W_0
	 *            The bias vector
	 * @param W_0_hist
	 *            The bias' history vector used for smoothing learning
	 * @param S
	 *            The shapelet matrix
	 * @param S_hist
	 *            The shapelet's history matrix used for smoothing learning
	 * @param dataMatrix
	 *            The data values matrix
	 * @param Y
	 *            The binarized target matrix
	 * @param beginTime
	 *            The begin time used to check for the timeout
	 */
	public void performSGD(final double[][][] W, final double[][][] W_hist, final double[] W_0, final double[] W_0_hist, final double[][][] S, final double[][][] S_hist, final double[][] dataMatrix, final int[][] Y, final long beginTime,
			final int[] targets) {
		// Define the "helper" matrices used for the gradient calculations
		final int scaleR = this.getConfig().scaleR();
		final int minShapeLength = this.getConfig().minShapeletLength();
		final int maxIter = this.getConfig().maxIterations();
		final int seed = this.getConfig().seed();
		final int numShapelets = this.getConfig().numShapelets();
		final double learningRate = this.getConfig().learningRate();
		final double regularization = this.getConfig().regularization();
		final double gamma = this.getConfig().gamma();

		double[][][][] D = new double[scaleR][][][];
		double[][][][] Xi = new double[scaleR][][][];
		double[][][][] Phi = new double[scaleR][][][];

		int[] numberOfSegments = new int[scaleR];

		for (int r = 0; r < scaleR; r++) {
			numberOfSegments[r] = getNumberOfSegments(this.Q, minShapeLength, r);
			D[r] = new double[this.I][numShapelets][numberOfSegments[r]];
			Xi[r] = new double[this.I][numShapelets][numberOfSegments[r]];
			Phi[r] = new double[this.I][numShapelets][numberOfSegments[r]];
		}

		double[][][] Psi = new double[scaleR][this.I][numShapelets];
		double[][][] M_hat = new double[scaleR][this.I][numShapelets];
		double[][] Theta = new double[this.I][this.C];

		List<Integer> indices = IntStream.range(0, this.I).boxed().collect(Collectors.toList());

		// Stochastic gradient descent
		LOGGER.debug("Starting training for {} iterations...", maxIter);

		// Initialize velocities used within training with zeros
		double[][][] velocitiesW = new double[W.length][W[0].length][W[0][0].length];
		double[] velocitiesW0 = new double[W_0.length];
		double[][][] velocitiesS = new double[S.length][][];
		for (int i = 0; i < S.length; i++) {
			velocitiesS[i] = new double[S[i].length][];
			for (int j = 0; j < S[i].length; j++) {
				velocitiesS[i][j] = new double[S[i][j].length];
			}
		}

		for (int it = 0; it < maxIter; it++) {

			// Shuffle instances
			if (this.useInstanceReordering) {
				indices = this.shuffleAccordingToAlternatingClassScheme(indices, targets, new Random(seed + it));
			} else {
				Collections.shuffle(indices, new Random(seed + it));
			}

			for (int idx = 0; idx < this.I; idx++) {
				int i = indices.get(idx);

				// Pre-compute terms
				for (int r = 0; r < scaleR; r++) {

					long kBound = S[r].length;
					for (int k = 0; k < kBound; k++) { // this.K

						int J_r = numberOfSegments[r];

						for (int j = 0; j < J_r; j++) {

							double newDValue = calculateD(S, minShapeLength, r, dataMatrix[i], k, j);
							D[r][i][k][j] = newDValue;
							newDValue = Math.exp(ALPHA * newDValue);
							Xi[r][i][k][j] = newDValue;

						}

						double newPsiValue = 0;
						double newMHatValue = 0;

						for (int j = 0; j < J_r; j++) {
							newPsiValue += Xi[r][i][k][j];
							newMHatValue += D[r][i][k][j] * Xi[r][i][k][j];
						}
						Psi[r][i][k] = newPsiValue;

						newMHatValue /= Psi[r][i][k];

						M_hat[r][i][k] = newMHatValue;
					}
				}

				for (int c = 0; c < this.C; c++) {
					double newThetaValue = 0;
					for (int r = 0; r < scaleR; r++) {
						for (int k = 0; k < numShapelets; k++) {

							newThetaValue += M_hat[r][i][k] * W[c][r][k];
						}
					}
					Theta[i][c] = Y[i][c] - MathUtil.sigmoid(newThetaValue);
				}

				// Learn shapelets and classification weights
				for (int c = 0; c < this.C; c++) {
					double gradW_0 = Theta[i][c];

					for (int r = 0; r < scaleR; r++) {
						for (int k = 0; k < S[r].length; k++) { // this differs from paper: this.K instead of
							// shapelet length
							double wStep = (-1d) * Theta[i][c] * M_hat[r][i][k] + 2d * regularization / (this.I) * W[c][r][k];
							velocitiesW[c][r][k] = gamma * velocitiesW[c][r][k] + learningRate * wStep;
							W_hist[c][r][k] += wStep * wStep;

							W[c][r][k] -= (velocitiesW[c][r][k] / Math.sqrt(W_hist[c][r][k] + EPS));

							int J_r = numberOfSegments[r];

							double phiDenominator = 1d / ((r + 1d) * minShapeLength * Psi[r][i][k]);

							double[] distDiff = new double[J_r];
							for (int j = 0; j < J_r; j++) {
								distDiff[j] = Xi[r][i][k][j] * (1d + ALPHA * (D[r][i][k][j] - M_hat[r][i][k]));
							}

							for (int l = 0; l < (r + 1) * minShapeLength; l++) {
								double shapeletDiff = 0;
								for (int j = 0; j < J_r; j++) {
									shapeletDiff += distDiff[j] * (S[r][k][l] - dataMatrix[i][j + l]);
								}

								double sStep = (-1d) * gradW_0 * shapeletDiff * W[c][r][k] * phiDenominator;

								velocitiesS[r][k][l] = gamma * velocitiesS[r][k][l] + learningRate * sStep;
								S_hist[r][k][l] += sStep * sStep;

								S[r][k][l] -= velocitiesS[r][k][l] / Math.sqrt(S_hist[r][k][l] + EPS);
							}
						}
					}

					velocitiesW0[c] = gamma * velocitiesW0[c] + learningRate * gradW_0;
					W_0_hist[c] += gradW_0 * gradW_0;
					W_0[c] += velocitiesW0[c] / Math.sqrt(W_0_hist[c] + EPS);

				}
			}

			if (it % 10 == 0) {
				LOGGER.debug("Iteration {}/{}", it, maxIter);

				long currTime = System.currentTimeMillis();
				if (currTime - beginTime > this.timeout.milliseconds()) {
					LOGGER.debug("Stopping training due to timeout.");
					break;
				}
			}
		}
	}

	/**
	 * Shuffles the data in a class alternating scheme. That means that at first,
	 * all indices per class are shuffled. Then, the randomized indices are selected
	 * in a round robin fashion among the classes.
	 *
	 * @param instanceIndices
	 *            The instance indices the original dataset
	 * @param targets
	 *            The targets of each instance
	 * @param random
	 *            Random object used for randomized shuffling
	 * @return Returns the list of the shuffled indices of the alternating class
	 *         scheme. Each index of <code>instanceIndices</code> is only used once
	 *         (without replacement)
	 */
	public List<Integer> shuffleAccordingToAlternatingClassScheme(final List<Integer> instanceIndices, final int[] targets, final Random random) {
		if (instanceIndices.size() != targets.length) {
			throw new IllegalArgumentException("The number of instances must be equal to the number of available target values!");
		}

		// Extract indices per class
		Map<Integer, List<Integer>> indicesPerClass = new HashMap<>();
		for (int i = 0; i < instanceIndices.size(); i++) {
			int classIdx = targets[i];

			if (!indicesPerClass.containsKey(classIdx)) {
				indicesPerClass.put(classIdx, new ArrayList<>());
			}

			indicesPerClass.get(classIdx).add(i);
		}

		// Shuffle all class indices
		List<Iterator<Integer>> iteratorList = new ArrayList<>();
		for (List<Integer> list : indicesPerClass.values()) {
			Collections.shuffle(list, random);
			iteratorList.add(list.iterator());
		}

		// Add indices to result list based on the alternating scheme
		List<Integer> resultList = new ArrayList<>();
		Iterator<Iterator<Integer>> roundRobinIt = Iterables.cycle(iteratorList).iterator();
		for (int i = 0; i < instanceIndices.size(); i++) {
			int tmpCounter = 0;
			while (roundRobinIt.hasNext() && tmpCounter < this.C) {
				Iterator<Integer> tmpIt = roundRobinIt.next();
				if (!tmpIt.hasNext()) {
					tmpCounter++;
					continue;
				} else {
					resultList.add(tmpIt.next());
					break;
				}
			}
		}
		return resultList;
	}

	/**
	 * Function to calculate the soft-minimum function which is a differentiable
	 * approximation of the minimum distance matrix given in the paper in section
	 * 3.1.4.
	 *
	 * @param S
	 *            The tensor storing the shapelets for different scales
	 * @param minShapeLength
	 *            The minimum shape length
	 * @param r
	 *            The number of scale to look at
	 * @param instance
	 *            The instance time series vector
	 * @param k
	 *            The index of the shapelet to look at
	 * @param Q
	 *            The number of attributes (time series length)
	 * @param alpha
	 *            Parameter to control the desired precision of the M_hat
	 *            approximation
	 * @return Returns the approximation of the minimum distance of the instance and
	 *         the shapelet given by the parameters <code>r</code> and
	 *         <code>k</code>.
	 */
	public static double calculateM_hat(final double[][][] S, final int minShapeLength, final int r, final double[] instance, final int k, final int Q, final double alpha) {
		double nominator = 0;
		double denominator = 0;
		for (int j = 0; j < getNumberOfSegments(Q, minShapeLength, r); j++) {
			double D = calculateD(S, minShapeLength, r, instance, k, j);
			double expD = Math.exp(alpha * D);
			nominator += D * expD;
			denominator += expD;
		}
		denominator = denominator == 0d ? EPS : denominator;
		return nominator / denominator;
	}

	/**
	 * Function to calculate the distance between the <code>j</code>-th segment of
	 * the given time series <code>instance</code> and the <code>k</code>-th
	 * shapelet stored in the shapelet tensor <code>S</code>.
	 *
	 * @param S
	 *            The tensor storing the shapelets for different scales
	 * @param minShapeLength
	 *            The minimum shape length
	 * @param r
	 *            The number of scale to look at
	 * @param instance
	 *            The instance time series vector
	 * @param k
	 *            The index of the shapelet to look at
	 * @param j
	 *            The segment of the instance time series to look at
	 * @return Returns the minimum distance of the <code>j</code>-th segment of the
	 *         instance and the shapelet given by the parameters <code>r</code>,
	 *         <code>k</code> and <code>j</code>.
	 */
	public static double calculateD(final double[][][] S, final int minShapeLength, final int r, final double[] instance, final int k, final int j) {

		double result = 0;
		for (int l = 0; l < (r + 1) * minShapeLength; l++) {
			result += Math.pow(instance[j + l] - S[r][k][l], 2);
		}
		return result / ((r + 1) * minShapeLength);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AlgorithmEvent nextWithException() {
		throw new UnsupportedOperationException("The operation to be performed is not supported.");
	}

	/**
	 * Returns the number of segments which are available for a instance with
	 * <code>Q</code> attributes for a given scale <code>r</code> and a minimum
	 * shape length <code>minShapeLength</code>.
	 *
	 * @param Q
	 *            Number of attributes of an instance
	 * @param minShapeLength
	 *            Minimum shapelet length
	 * @param r
	 *            Scale to be looked at
	 * @return Returns the number of segments which can be looked at for an instance
	 *         with <code>Q</code> time series attributes
	 */
	public static int getNumberOfSegments(final int Q, final int minShapeLength, final int r) {
		return Q - (r + 1) * minShapeLength;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ILearnShapeletsLearningAlgorithmConfig getConfig() {
		return (ILearnShapeletsLearningAlgorithmConfig)super.getConfig();
	}

	/**
	 * @return the useInstanceReordering
	 */
	public boolean isUseInstanceReordering() {
		return this.useInstanceReordering;
	}

	/**
	 * @param useInstanceReordering
	 *            the useInstanceReordering to set
	 */
	public void setUseInstanceReordering(final boolean useInstanceReordering) {
		this.useInstanceReordering = useInstanceReordering;
	}

	/**
	 * @return the c
	 */
	public int getC() {
		return this.C;
	}

	/**
	 * @param c
	 *            the c to set
	 */
	public void setC(final int c) {
		this.C = c;
	}
}
