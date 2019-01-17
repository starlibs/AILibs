package jaicore.ml.tsc.classifier;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.nd4j.linalg.api.rng.distribution.Distribution;
import org.nd4j.linalg.api.rng.distribution.impl.NormalDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.TimeOut;
import jaicore.basic.algorithm.IAlgorithmConfig;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.ml.core.dataset.TimeSeriesDataset;
import jaicore.ml.core.dataset.attribute.categorical.CategoricalAttributeType;
import jaicore.ml.core.dataset.attribute.categorical.CategoricalAttributeValue;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.Debug.Random;
import weka.core.DenseInstance;
import weka.core.Instance;
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
public class LearnShapeletsAlgorithm extends
		ATSCAlgorithm<CategoricalAttributeType, CategoricalAttributeValue, TimeSeriesDataset, LearnShapeletsClassifier> {

	private static final Logger LOGGER = LoggerFactory.getLogger(LearnShapeletsAlgorithm.class);

	private int K;
	private double learningRate;
	private double regularization;
	private int scaleR;
	private int minShapeLength;
	private int maxIter;
	private int seed;

	private int I;
	private int Q;
	private int C;

	public static double ALPHA = -30d; // Used in implementation. Paper says -100d

	public LearnShapeletsAlgorithm(final int K, final double learningRate, final double regularization,
			final int scaleR, final int minShapeLength, final int maxIter, final int seed) {
		this.K = K;
		this.learningRate = learningRate;
		this.regularization = regularization;
		this.scaleR = scaleR;
		this.minShapeLength = minShapeLength;
		this.maxIter = maxIter;
		this.seed = seed;
	}

	@Override
	public Iterator<AlgorithmEvent> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasNext() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AlgorithmEvent next() {
		// TODO Auto-generated method stub
		return null;
	}

	public double[][][] initializeS(final double[][] trainingMatrix) {
		LOGGER.debug("Initializing S...");

		final double[][][] result = new double[this.scaleR][][];

		for (int r = 0; r < this.scaleR; r++) {
			final int numberOfSegments = getNumberOfSegments(this.Q, this.minShapeLength, r);

			final int L = (r + 1) * this.minShapeLength;

			// final INDArray tmpSegments = Nd4j.create(trainingMatrix.shape()[0] *
			// numberOfSegments, L);
			final double[][] tmpSegments = new double[trainingMatrix.length * numberOfSegments][L];

			// Prepare training data for finding the centroids
			for (int i = 0; i < trainingMatrix.length; i++) {
				for (int j = 0; j < numberOfSegments; j++) {
					for (int l = 0; l < L; l++) {
						// tmpSegments.putScalar(new int[] { i * numberOfSegments + j, l },
						// trainingMatrix.getDouble(i, j + l));
						tmpSegments[i * numberOfSegments + j][l] = trainingMatrix[i][j + l];
					}
					// TimeSeriesUtil.normalizeINDArray(tmpSegments.getRow(i * numberOfSegments +
					// j), true);
					// TODO: Normalize
					tmpSegments[i * numberOfSegments + j] = null; // normalize(tmpSegments[i*numberOfSegments+j];
				}
			}

			// TODO:
			final ArrayList<Attribute> attributes = new ArrayList<>();
			for (int i = 0; i < tmpSegments[0].length; i++) {
				final Attribute newAtt = new Attribute("val" + i);
				attributes.add(newAtt);
			}
			Instances wekaInstances = new Instances("Instances", attributes, tmpSegments.length);
			for (int i = 0; i < tmpSegments[0].length; i++) {
				final Instance inst = new DenseInstance(1, tmpSegments[i]);
				inst.setDataset(wekaInstances);
				wekaInstances.add(inst);
			}

			// Instances wekaInstances =
			// TimeSeriesUtil.indArrayToWekaInstances(tmpSegments);

			// Cluster using k-Means
			SimpleKMeans kMeans = new SimpleKMeans();
			try {
				kMeans.setNumClusters(K);
				kMeans.setSeed(this.seed);
				kMeans.setMaxIterations(100);
				kMeans.buildClusterer(wekaInstances);
			} catch (Exception e) {
				LOGGER.warn(
						"Could not initialize matrix S using kMeans clustering for r=%d due to the following problem: %s. "
								+ "Using zero matrix instead (possibly leading to a poor training performance).",
						r, e.getMessage());
				// result.add(Nd4j.zeros(this.K, r * this.minShapeLength));
				result[r] = new double[this.K][r * this.minShapeLength];
				continue;
			}
			Instances clusterCentroids = kMeans.getClusterCentroids();

			// result[r] = TimeSeriesUtil.wekaInstancesToINDArray(clusterCentroids, false);
			double[][] tmpResult = new double[wekaInstances.numInstances()][wekaInstances.numAttributes()];
			for (int i = 0; i < tmpResult.length; i++) {
				double[] instValues = wekaInstances.get(i).toDoubleArray();
				for (int j = 0; j < tmpResult[i].length; j++) {
					tmpResult[i][j] = instValues[j];
				}
			}
			result[r] = tmpResult;
		}

		LOGGER.debug("Initialized S.");

		return result;
	}

	// @Override
	// public LearnShapeletsClassifier call() {
	// // Training
	//
	// TimeSeriesDataset data = this.getInput();
	//
	// if (data.isMultivariate())
	// throw new UnsupportedOperationException("Multivariate datasets are not
	// supported.");
	//
	// // TODO: Possibly unsafe cast
	// this.model.setTargetType((CategoricalAttributeType) data.getTargetType());
	//
	// final INDArray dataMatrix = data.getValuesOrNull(0);
	// if (dataMatrix == null || dataMatrix.shape().length != 2)
	// throw new IllegalArgumentException(
	// "Timestamp matrix must be a valid 2D matrix containing the time series values
	// for all instances!");
	//
	// final INDArray targetMatrix = data.getTargets();
	// final List<Integer> occuringClasses =
	// DoubleStream.of(targetMatrix.toDoubleVector()).mapToInt(d -> (int) d)
	// .boxed().collect(Collectors.toSet()).stream().collect(Collectors.toList());
	//
	// this.I = (int) data.getNumberOfInstances(); // I
	// this.Q = (int) dataMatrix.shape()[1]; // Q
	// this.C = occuringClasses.size(); // C
	//
	// // Prepare binary classes
	// INDArray Y = Nd4j.create(this.I, this.C);
	// for (int i = 0; i < this.I; i++) {
	// Integer instanceClass = targetMatrix.getInt(i);
	// Y.putScalar(new int[] { i, occuringClasses.indexOf(instanceClass) }, 1);
	// }
	//
	// // Normalize instances
	// // for (int i = 0; i < data.getNumberOfInstances(); i++)
	// // TimeSeriesUtil.normalizeINDArray(dataMatrix.getRow(i), true);
	//
	// // Initialization
	// List<INDArray> S = initializeS(dataMatrix);
	// List<INDArray> S_hist = new ArrayList<>();
	// for (int r = 0; r < this.scaleR; r++) {
	// S_hist.add(Nd4j.create(S.get(r).shape()));
	// }
	// List<INDArray> D = new ArrayList<>();
	// List<INDArray> Xi = new ArrayList<>();
	// List<INDArray> Phi = new ArrayList<>();
	//
	// int totalSegments = 0;
	// for (int r = 0; r < this.scaleR; r++) {
	// final int numberOfSegments = getNumberOfSegments(this.Q, this.minShapeLength,
	// r);
	// totalSegments += numberOfSegments * this.I;
	// }
	//
	// this.K = (int) (Math.log(totalSegments) * (this.C - 1));
	//
	// for (int r = 0; r < this.scaleR; r++) {
	// final int numberOfSegments = getNumberOfSegments(this.Q, this.minShapeLength,
	// r);
	// // S.add(Nd4j.create(K, r * this.minShapeLength));
	// D.add(Nd4j.create(this.I, this.K, numberOfSegments));
	// Xi.add(Nd4j.create(this.I, this.K, numberOfSegments));
	// Phi.add(Nd4j.create(this.I, this.K, numberOfSegments));
	// }
	//
	// // TODO: Check correct order of shape parameters of W => Current version is
	// the
	// // paper's version but doesn't match with the allocated matrix's shape
	// Distribution wInitDistribution = new NormalDistribution(0, 0.01);
	// INDArray W = Nd4j.rand(new long[] { this.C, this.scaleR, this.K },
	// wInitDistribution);
	// INDArray W_hist = Nd4j.create(W.shape());
	// INDArray W_0 = Nd4j.rand(new long[] { this.C }, wInitDistribution);
	// INDArray W_0_hist = Nd4j.create(W_0.shape());
	//
	// INDArray Psi = Nd4j.create(this.scaleR, this.I, this.K);
	// INDArray M_hat = Nd4j.create(this.scaleR, this.I, this.K);
	// INDArray Theta = Nd4j.create(this.I, this.C);
	//
	// LOGGER.debug("Starting training for {} iterations...", this.maxIter);
	// for (int it = 0; it < this.maxIter; it++) {
	// for (int i = 0; i < this.I; i++) {
	// // Pre-compute terms
	// for (int r = 0; r < this.scaleR; r++) {
	//
	// long kBound = S.get(r).shape()[0];
	// for (int k = 0; k < kBound; k++) { // this.K
	//
	// int J_r = getNumberOfSegments(this.Q, this.minShapeLength, r);
	//
	// for (int j = 0; j < J_r; j++) {
	// // if (i == 23 && k == 4 && r == 2)
	// // LOGGER.debug("Check this here.");
	//
	// double newDValue = calculateD(S, minShapeLength, r, dataMatrix.getRow(i), k,
	// j);
	// // if (Double.isNaN(newDValue))
	// // newDValue = 0;
	//
	// D.get(r).putScalar(new int[] { i, k, j }, newDValue);
	// newDValue = Math.exp(ALPHA * newDValue);
	// // if (Double.isNaN(newDValue))
	// // LOGGER.debug("Test");// newDValue = 0;
	// Xi.get(r).putScalar(new int[] { i, k, j }, newDValue);
	// }
	//
	// double newPsiValue = 0;
	// double newMHatValue = 0;
	// // FIXME: Xi stores zero only for row
	// for (int j = 0; j < J_r; j++) {
	// newPsiValue += Xi.get(r).getDouble(i, k, j);
	// newMHatValue += D.get(r).getDouble(i, k, j) * Xi.get(r).getDouble(i, k, j);
	// }
	// Psi.putScalar(new int[] { r, i, k }, newPsiValue);
	//
	// // FIXME Div by zero
	// newMHatValue /= Psi.getDouble(r, i, k);
	//
	// // if (Double.isNaN(newMHatValue) || Double.isInfinite(newMHatValue))
	// // LOGGER.debug("NaN value");
	//
	// M_hat.putScalar(new int[] { r, i, k }, newMHatValue);
	// }
	// }
	//
	// for (int c = 0; c < this.C; c++) {
	// double newThetaValue = 0;
	// for (int r = 0; r < this.scaleR; r++) {
	// for (int k = 0; k < this.K; k++) {
	//
	// newThetaValue += M_hat.getDouble(r, i, k) * W.getDouble(c, r, k);
	// }
	// }
	// // if (Double.isNaN(newThetaValue) || Double.isInfinite(newThetaValue))
	// // LOGGER.debug("NaN value");
	//
	// double newThetaValue2 = Y.getDouble(i, c) - sigmoid(newThetaValue);
	// // if (Double.isNaN(newThetaValue2) || Double.isInfinite(newThetaValue2))
	// // LOGGER.debug("NaN value");
	//
	// Theta.putScalar(new int[] { i, c }, newThetaValue2);
	// }
	//
	// // Learn shapelets and classification weights
	// for (int c = 0; c < this.C; c++) {
	// double gradW_0 = Theta.getDouble(i, c);
	//
	// for (int r = 0; r < this.scaleR; r++) {
	// for (int k = 0; k < S.get(r).shape()[0]; k++) { // this differs from paper:
	// this.K instead of
	// // shapelet length
	// double wStep = (-1d) * Theta.getDouble(i, c) * M_hat.getDouble(r, i, k)
	// + 2d * this.regularization / (this.I) * W.getDouble(c, r, k);
	//
	// double wStepSquare = W_hist.getScalar(c, r, k).addi(wStep *
	// wStep).getDouble(0);
	//
	// // double wStep = Theta.getDouble(i, c) * M_hat.getDouble(r, i, k)
	// // + 2d * this.regularization / (this.I * this.C) * W.getDouble(c, r, k);
	//
	// // W.putScalar(new int[] { c, r, k }, W.getDouble(c, r, k) -
	// this.learningRate *
	// // wStep);
	// W.getScalar(c, r, k).subi(this.learningRate * wStep /
	// Math.sqrt(wStepSquare));
	//
	// int J_r = getNumberOfSegments(this.Q, this.minShapeLength, r);
	// for (int j = 0; j < J_r; j++) {
	// double newPhiValue = 2 * Xi.get(r).getDouble(i, k, j)
	// * (1 + ALPHA * (D.get(r).getDouble(i, k, j) - M_hat.getDouble(r, i, k)));
	// newPhiValue /= (r + 1) * this.minShapeLength * Psi.getDouble(r, i, k);
	// Phi.get(r).putScalar(new int[] { i, k, j }, newPhiValue);
	//
	// for (int l = 0; l < (r + 1) * this.minShapeLength; l++) {
	// double sStep = (-1) * gradW_0 * Phi.get(r).getDouble(i, k, j)
	// * (S.get(r).getDouble(k, l) - dataMatrix.getDouble(i, j + l))
	// * W.getDouble(c, r, k);
	// double sStepSquare = S_hist.get(r).getScalar(k, l).addi(sStep *
	// sStep).getDouble(0);
	//
	// S.get(r).getScalar(k, l).subi(this.learningRate * sStep /
	// Math.sqrt(sStepSquare));
	// }
	// }
	// }
	// }
	//
	// double gradW_0Square = W_0_hist.getScalar(c).addi(gradW_0 *
	// gradW_0).getDouble(0);
	// W_0.getScalar(c).addi(this.learningRate * gradW_0 /
	// Math.sqrt(gradW_0Square));
	// }
	// }
	//
	// if (it % 10 == 0) {
	// LOGGER.debug("Iteration {}/{}", it, this.maxIter);
	// }
	// }
	// LOGGER.debug("Finished training.");
	//
	// this.model.setS(S);
	// this.model.setW(W);
	// this.model.setW_0(W_0);
	// // this.model.setM_hat(M_hat);
	// return this.model;
	// }

	@Override
	public LearnShapeletsClassifier call() {
		// Training

		TimeSeriesDataset data = this.getInput();

		if (data.isMultivariate())
			throw new UnsupportedOperationException("Multivariate datasets are not supported.");

		// TODO: Possibly unsafe cast
		this.model.setTargetType((CategoricalAttributeType) data.getTargetType());

		// TODO
		final double[][] dataMatrix = null; // = data.getValuesOrNull(0);
		// if (dataMatrix == null || dataMatrix.shape().length != 2)
		// throw new IllegalArgumentException(
		// "Timestamp matrix must be a valid 2D matrix containing the time series values
		// for all instances!");

		final int[] targetMatrix = null; // = data.getTargets();
		final List<Integer> occuringClasses = IntStream.of(targetMatrix).boxed().collect(Collectors.toSet()).stream()
				.collect(Collectors.toList());// DoubleStream.of(targetMatrix.toDoubleVector()).mapToInt(d -> (int) d)
		// .boxed().collect(Collectors.toSet()).stream().collect(Collectors.toList());

		this.I = (int) data.getNumberOfInstances(); // I
		this.Q = dataMatrix[0].length; // Q
		this.C = occuringClasses.size(); // C

		// Prepare binary classes
		int[][] Y = new int[this.I][this.C];
		for (int i = 0; i < this.I; i++) {
			Integer instanceClass = targetMatrix[i];
			Y[i][occuringClasses.indexOf(instanceClass)] = 1;
			// Y.putScalar(new int[] { i, occuringClasses.indexOf(instanceClass) }, 1);
		}

		// Normalize instances
		// for (int i = 0; i < data.getNumberOfInstances(); i++)
		// TimeSeriesUtil.normalizeINDArray(dataMatrix.getRow(i), true);

		// Initialization
		double[][][] S = initializeS(dataMatrix);
		double[][][] S_hist = new double[this.scaleR][][];
		for (int r = 0; r < this.scaleR; r++) {
			S_hist[r] = new double[S[r].length][S[r][0].length];
			// S_hist.add(Nd4j.create(S.get(r).shape()));
		}
		// List<INDArray> D = new ArrayList<>();
		// List<INDArray> Xi = new ArrayList<>();
		// List<INDArray> Phi = new ArrayList<>();
		double[][][][] D = new double[this.scaleR][][][];
		double[][][][] Xi = new double[this.scaleR][][][];
		double[][][][] Phi = new double[this.scaleR][][][];

		int totalSegments = 0;
		for (int r = 0; r < this.scaleR; r++) {
			final int numberOfSegments = getNumberOfSegments(this.Q, this.minShapeLength, r);
			totalSegments += numberOfSegments * this.I;
		}

		this.K = (int) (Math.log(totalSegments) * (this.C - 1));

		for (int r = 0; r < this.scaleR; r++) {
			final int numberOfSegments = getNumberOfSegments(this.Q, this.minShapeLength, r);
			// D.add(Nd4j.create(this.I, this.K, numberOfSegments));
			// Xi.add(Nd4j.create(this.I, this.K, numberOfSegments));
			// Phi.add(Nd4j.create(this.I, this.K, numberOfSegments));
			D[r] = new double[this.I][this.K][numberOfSegments];
			Xi[r] = new double[this.I][this.K][numberOfSegments];
			Phi[r] = new double[this.I][this.K][numberOfSegments];
		}

		// TODO: Check correct order of shape parameters of W => Current version is the
		// paper's version but doesn't match with the allocated matrix's shape
		Distribution wInitDistribution = new NormalDistribution(0, 0.01);
		Random rand = new Random(this.seed);

		double[][][] W = new double[this.C][this.scaleR][this.K];
		double[][][] W_hist = new double[this.C][this.scaleR][this.K];
		double[] W_0 = new double[this.C];
		double[] W_0_hist = new double[this.C];
		for (int i = 0; i < this.C; i++) {
			W_0[i] = rand.nextGaussian() * 0.01;
			for (int j = 0; j < this.scaleR; j++) {
				for (int k = 0; k < this.K; k++) {
					W[i][j][k] = rand.nextGaussian() * 0.01;
				}
			}
		}

		// INDArray W = Nd4j.rand(new long[] { this.C, this.scaleR, this.K },
		// wInitDistribution);
		// INDArray W_hist = Nd4j.create(W.shape());
		// INDArray W_0 = Nd4j.rand(new long[] { this.C }, wInitDistribution);
		// INDArray W_0_hist = Nd4j.create(W_0.shape());

		double[][][] Psi = new double[this.scaleR][this.I][this.K];
		double[][][] M_hat = new double[this.scaleR][this.I][this.K];
		double[][] Theta = new double[this.I][this.C];

		// INDArray Psi = Nd4j.create(this.scaleR, this.I, this.K);
		// INDArray M_hat = Nd4j.create(this.scaleR, this.I, this.K);
		// INDArray Theta = Nd4j.create(this.I, this.C);

		LOGGER.debug("Starting training for {} iterations...", this.maxIter);
		for (int it = 0; it < this.maxIter; it++) {
			for (int i = 0; i < this.I; i++) {
				// Pre-compute terms
				for (int r = 0; r < this.scaleR; r++) {

					long kBound = S[r].length;
					for (int k = 0; k < kBound; k++) { // this.K

						int J_r = getNumberOfSegments(this.Q, this.minShapeLength, r);

						for (int j = 0; j < J_r; j++) {
							// if (i == 23 && k == 4 && r == 2)
							// LOGGER.debug("Check this here.");

							double newDValue = calculateD(S, minShapeLength, r, dataMatrix[i], k, j);
							// if (Double.isNaN(newDValue))
							// newDValue = 0;

							// D.get(r).putScalar(new int[] { i, k, j }, newDValue);
							D[r][i][k][j] = newDValue;
							newDValue = Math.exp(ALPHA * newDValue);
							// if (Double.isNaN(newDValue))
							// LOGGER.debug("Test");// newDValue = 0;
							// Xi.get(r).putScalar(new int[] { i, k, j }, newDValue);
							Xi[r][i][k][j] = newDValue;

						}

						double newPsiValue = 0;
						double newMHatValue = 0;
						// FIXME: Xi stores zero only for row
						for (int j = 0; j < J_r; j++) {
							newPsiValue += Xi[r][i][k][j];
							newMHatValue += D[r][i][k][j] * Xi[r][i][k][j];
						}
						Psi[r][i][k] = newPsiValue;
						// Psi.putScalar(new int[] { r, i, k }, newPsiValue);

						// FIXME Div by zero
						// newMHatValue /= Psi.getDouble(r, i, k);
						newMHatValue /= Psi[r][i][k];

						// if (Double.isNaN(newMHatValue) || Double.isInfinite(newMHatValue))
						// LOGGER.debug("NaN value");

						// M_hat.putScalar(new int[] { r, i, k }, newMHatValue);
						M_hat[r][i][k] = newMHatValue;
					}
				}

				for (int c = 0; c < this.C; c++) {
					double newThetaValue = 0;
					for (int r = 0; r < this.scaleR; r++) {
						for (int k = 0; k < this.K; k++) {

							// newThetaValue += M_hat.getDouble(r, i, k) * W.getDouble(c, r, k);
							newThetaValue += M_hat[r][i][k] * W[c][r][k];
						}
					}
					// if (Double.isNaN(newThetaValue) || Double.isInfinite(newThetaValue))
					// LOGGER.debug("NaN value");

					// double newThetaValue2 = Y.getDouble(i, c) - sigmoid(newThetaValue);
					double newThetaValue2 = Y[i][c] - sigmoid(newThetaValue);
					// if (Double.isNaN(newThetaValue2) || Double.isInfinite(newThetaValue2))
					// LOGGER.debug("NaN value");

					// Theta.putScalar(new int[] { i, c }, newThetaValue2);
					Theta[i][c] = newThetaValue2;
				}

				// Learn shapelets and classification weights
				for (int c = 0; c < this.C; c++) {
					// double gradW_0 = Theta.getDouble(i, c);
					double gradW_0 = Theta[i][c];

					for (int r = 0; r < this.scaleR; r++) {
						for (int k = 0; k < S[r].length; k++) { // this differs from paper: this.K instead of
																// shapelet length
							double wStep = (-1d) * Theta[i][c] * M_hat[r][i][k]
									+ 2d * this.regularization / (this.I) * W[c][r][k];

							W_hist[c][r][k] += wStep * wStep;

							// double wStep = Theta.getDouble(i, c) * M_hat.getDouble(r, i, k)
							// + 2d * this.regularization / (this.I * this.C) * W.getDouble(c, r, k);

							// W.putScalar(new int[] { c, r, k }, W.getDouble(c, r, k) - this.learningRate *
							// wStep);
							W[c][r][k] -= (this.learningRate * wStep / Math.sqrt(W_hist[c][r][k]));

							int J_r = getNumberOfSegments(this.Q, this.minShapeLength, r);
							for (int j = 0; j < J_r; j++) {
								double newPhiValue = 2 * Xi[r][i][k][j]
										* (1 + ALPHA * (D[r][i][k][j] - M_hat[r][i][k]));
								newPhiValue /= (r + 1) * this.minShapeLength * Psi[r][i][k];
								Phi[r][i][k][j] = newPhiValue;

								for (int l = 0; l < (r + 1) * this.minShapeLength; l++) {
									double sStep = (-1) * gradW_0 * Phi[r][i][k][j]
											* (S[r][k][l] - dataMatrix[i][j + l]) * W[c][r][k];
									S_hist[r][k][l] += sStep * sStep;
									// double sStepSquare = S_hist[r][k][l].addi(sStep * sStep).getDouble(0);

									S[r][k][l] -= this.learningRate * sStep / Math.sqrt(S_hist[r][k][l]);
								}
							}
						}
					}

					W_0_hist[c] += gradW_0 * gradW_0;
					W_0[c] += this.learningRate * gradW_0 / Math.sqrt(W_0_hist[c]);
				}
			}

			if (it % 10 == 0) {
				LOGGER.debug("Iteration {}/{}", it, this.maxIter);
			}
		}
		LOGGER.debug("Finished training.");

		this.model.setS(S);
		this.model.setW(W);
		this.model.setW_0(W_0);
		// this.model.setM_hat(M_hat);
		return this.model;
	}

	public static double calculateM_hat(final double[][][] S, final int minShapeLength, final int r,
			final double[] instance, final int k, final int Q, final double alpha) {
		double nominator = 0;
		double denominator = 0;
		for (int j = 0; j < getNumberOfSegments(Q, minShapeLength, r); j++) {
			double D = calculateD(S, minShapeLength, r, instance, k, j);
			double expD = Math.exp(alpha * D);
			nominator += D * expD;
			denominator += expD;
		}
		return nominator / denominator;
	}

	public static double calculateD(final double[][][] S, final int minShapeLength, final int r,
			final double[] instance, final int k, final int j) {

		double result = 0;
		for (int l = 0; l < (r + 1) * minShapeLength; l++) {
			result += Math.pow(instance[j + l] - S[r][k][l], 2);
		}
		return result / (double) ((r + 1) * minShapeLength);
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub

	}

	@Override
	public TimeSeriesDataset getInput() {
		return this.input;
	}

	@Override
	public void registerListener(Object listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setNumCPUs(int numberOfCPUs) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getNumCPUs() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setTimeout(long timeout, TimeUnit timeUnit) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTimeout(TimeOut timeout) {
		// TODO Auto-generated method stub

	}

	@Override
	public TimeOut getTimeout() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AlgorithmEvent nextWithException() {
		// TODO Auto-generated method stub
		return null;
	}

	public static int getNumberOfSegments(final int Q, final int minShapeLength, final int r) {
		return Q - (r + 1) * minShapeLength;
	}

	// TODO: Maybe move to utility? Or use library?
	public static double sigmoid(final double z) {
		return 1 / (1 + Math.exp((-1) * z));
	}

	@Override
	public IAlgorithmConfig getConfig() {
		// TODO Auto-generated method stub
		return null;
	}
}
