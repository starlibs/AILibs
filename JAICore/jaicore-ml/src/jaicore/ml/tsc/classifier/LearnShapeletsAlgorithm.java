package jaicore.ml.tsc.classifier;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.rng.distribution.Distribution;
import org.nd4j.linalg.api.rng.distribution.impl.NormalDistribution;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.TimeOut;
import jaicore.basic.algorithm.AlgorithmEvent;
import jaicore.ml.core.dataset.TimeSeriesDataset;
import jaicore.ml.core.dataset.attribute.categorical.CategoricalAttributeType;
import jaicore.ml.core.dataset.attribute.categorical.CategoricalAttributeValue;
import jaicore.ml.tsc.util.TimeSeriesUtil;
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

	public static double ALPHA = -100d;

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

	public List<INDArray> initializeS(final INDArray trainingMatrix) {
		final List<INDArray> result = new ArrayList<>();

		for (int r = 0; r < this.scaleR; r++) {
			final int numberOfSegments = getNumberOfSegments(this.Q, this.K, r);

			final int L = (r + 1) * this.minShapeLength;

			final INDArray tmpSegments = Nd4j.create(trainingMatrix.shape()[0] * numberOfSegments, L);

			// Prepare training data for finding the centroids
			for (int i = 0; i < trainingMatrix.shape()[0]; i++) {
				for (int j = 0; j < numberOfSegments; j++) {
					for (int l = 0; l < L; l++) {
						tmpSegments.putScalar(new int[] { i * numberOfSegments + j, l },
								trainingMatrix.getDouble(i, j + l));
					}
					TimeSeriesUtil.normalizeINDArray(tmpSegments.getRow(i * numberOfSegments + j), true);
				}
			}

			Instances wekaInstances = TimeSeriesUtil.indArrayToWekaInstances(tmpSegments);

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
				result.add(Nd4j.zeros(this.K, r * this.minShapeLength));
				continue;
			}
			Instances clusterCentroids = kMeans.getClusterCentroids();

			result.add(TimeSeriesUtil.wekaInstancesToINDArray(clusterCentroids, false));
		}

		return result;
	}

	@Override
	public LearnShapeletsClassifier call() throws Exception {
		// Training

		TimeSeriesDataset data = this.getInput();
		INDArray dataMatrix = null; // TODO

		this.I = data.getNumberOfAttributes(); // I
		this.Q = data.size(); // Q
		this.C = 0; // TODO: C

		// TODO: Prepare binary classes
		INDArray Y = Nd4j.create(this.I, this.C);

		// TODO: Initialization
		List<INDArray> S = initializeS(dataMatrix);
		List<INDArray> D = new ArrayList<>();
		List<INDArray> Xi = new ArrayList<>();
		List<INDArray> Phi = new ArrayList<>();
		for (int r = 0; r < this.scaleR; r++) {
			final int numberOfSegments = getNumberOfSegments(this.Q, this.minShapeLength, r);
			// S.add(Nd4j.create(K, r * this.minShapeLength));
			D.add(Nd4j.create(this.I, this.K, numberOfSegments));
			Xi.add(Nd4j.create(this.I, this.K, numberOfSegments));
			Phi.add(Nd4j.create(this.I, this.K, numberOfSegments));
		}

		// TODO: Check correct order of shape parameters of W => Current version is the
		// paper's version but doesn't match with the allocated matrix's shape
		Distribution wInitDistribution = new NormalDistribution(0, 0.01);
		INDArray W = Nd4j.rand(new long[] { this.C, this.scaleR, this.K }, wInitDistribution);
		INDArray W_0 = Nd4j.rand(new long[] { this.C }, wInitDistribution);

		INDArray Psi = Nd4j.create(this.scaleR, this.I, this.K);
		INDArray M_hat = Nd4j.create(this.scaleR, this.I, this.K);
		INDArray Theta = Nd4j.create(this.I, this.C);

		for (int it = 0; it < this.maxIter; it++) {
			for (int i = 0; i < this.Q; i++) {
				// Pre-compute terms
				for (int r = 0; r < this.scaleR; r++) {
					for (int k = 0; k < this.K; k++) {
						for (int j = 0; j < getNumberOfSegments(this.Q, this.minShapeLength, r); j++) {
							double newDValue = calculateD(S, minShapeLength, r, dataMatrix.getRow(i), k, j);

							D.get(r).putScalar(new int[] { i, k, j }, newDValue);
							Xi.get(r).putScalar(new int[] { i, k, j }, Math.exp(this.ALPHA * newDValue));
						}
						double newPsiValue = 0;
						double newMHatValue = 0;
						for (int j = 0; j < getNumberOfSegments(this.Q, this.minShapeLength, r); j++) {
							newPsiValue += Xi.get(r).getDouble(i, k, j);
							newMHatValue += D.get(r).getDouble(i, k, j) * Xi.get(r).getDouble(i, k, j);
						}
						Psi.putScalar(new int[] { r, i, k }, newPsiValue);

						newMHatValue /= Psi.getDouble(r, i, k);
						M_hat.putScalar(new int[] { r, i, k }, newMHatValue);
					}
				}

				for (int c = 0; c < this.C; c++) {
					double newThetaValue = 0;
					for (int r = 0; r < this.scaleR; r++) {
						for (int k = 0; k < this.K; k++) {

							newThetaValue += M_hat.getDouble(r, i, k) * W.getDouble(c, r, k);
						}
					}
					newThetaValue = Y.getDouble(i, c) - sigmoid(newThetaValue);
					Theta.putScalar(new int[] { i, c }, newThetaValue);
				}

				// Learn shapelets and classification weights
				for (int c = 0; c < this.C; c++) {
					for (int r = 0; r < this.scaleR; r++) {
						for (int k = 0; k < this.K; k++) {
							double wStep = Theta.getDouble(i, c) * M_hat.getDouble(r, i, k)
									- 2 * this.regularization / (this.I * this.C) * W.getDouble(c, r, k);

							W.putScalar(new int[] { c, r, k }, W.getDouble(c, r, k) + this.learningRate * wStep);

							for (int j = 0; j < getNumberOfSegments(this.Q, this.minShapeLength, r); j++) {
								double newPhiValue = 2 * Xi.get(r).getDouble(i, k, j)
										* (1 + this.ALPHA * (D.get(r).getDouble(i, k, j) - M_hat.getDouble(r, i, k)));
								newPhiValue /= r * this.minShapeLength * Psi.getDouble(r, i, k);
								Phi.get(r).putScalar(new int[] { i, k, j }, newPhiValue);

								for (int l = 0; l < r * this.scaleR; l++) {
									double sStep = Theta.getDouble(i, c) * Phi.get(r).getDouble(i, k, j)
											* (S.get(r).getDouble(k, l) - dataMatrix.getDouble(i, j + l - 1))
											* W.getDouble(c, r, k);
									S.get(r).putScalar(new int[] { k, l },
											S.get(r).getDouble(k, l) + this.learningRate * sStep);
								}
							}
						}
					}
					W_0.putScalar(c, W_0.getDouble(c) + this.learningRate * Theta.getDouble(i, c));
				}
			}
		}

		this.model.setS(S);
		this.model.setW(W);
		this.model.setW_0(W_0);
		// this.model.setM_hat(M_hat);
		return this.model;
	}

	public static double calculateM_hat(final List<INDArray> S, final int minShapeLength, final int r,
			final INDArray instance, final int k, final int Q, final double alpha) {
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

	public static double calculateD(final List<INDArray> S, final int minShapeLength, final int r,
			final INDArray instance, final int k, final int j) {
		double result = 0;
		for (int l = 0; l < r * minShapeLength; l++) {
			result += Math.pow(instance.getDouble(0, j + l - 1) - S.get(r).getDouble(k, l), 2);
		}
		return result / (r * minShapeLength);
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub

	}

	@Override
	public TimeSeriesDataset getInput() {
		// TODO Auto-generated method stub
		return null;
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
	public void setTimeout(int timeout, TimeUnit timeUnit) {
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
	public AlgorithmEvent nextWithException() throws Exception {
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
}
