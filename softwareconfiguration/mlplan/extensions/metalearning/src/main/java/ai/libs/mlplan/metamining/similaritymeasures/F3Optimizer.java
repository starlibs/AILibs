package ai.libs.mlplan.metamining.similaritymeasures;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.sets.Pair;
import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.minimize.CostFunction;
import de.jungblut.math.minimize.CostGradientTuple;
import de.jungblut.math.minimize.GradientDescent;

public class F3Optimizer implements IHeterogenousSimilarityMeasureComputer {

	private static final Logger logger = LoggerFactory.getLogger(F3Optimizer.class);

	private static final double ALPHA_START = 0.000000001; // learning rate
	private static final double ALPHA_MAX = 1e-6;
	private static final int ITERATIONS_PER_PROBE = 100;
	private static final int LIMIT = 1; // as long as the solution improves by at least this value, continue
	private static final double MAX_DESIRED_ERROR = 0;

	private final double mu; // regularization constant

	private INDArray r;
	private INDArray x;
	private INDArray w;
	private INDArray u; // the learned matrices
	private INDArray v; // the learned matrices

	private final Random rand = new Random();

	public F3Optimizer(final double mu) {
		super();
		this.mu = mu;
	}

	/**
	 * Learns a matrix U that minimizes F1 (W is ignored here)
	 *
	 * @return
	 */
	@Override
	public void build(final INDArray x, final INDArray w, final INDArray r) {
		this.r = r;
		this.w = w;
		this.x = x;


		final int n = x.rows();
		final int d = x.columns();
		final int m = w.rows();
		final int l = w.columns();
		final int numberOfImplicitFeatures = 1;
		logger.debug("X = ( {} x {} )",n,x.columns());
		logger.debug("W = ( {} x {} )",m,w.columns());

		/* generate initial U and V vectors */
		boolean succesfullyBooted = false;
		DoubleVector currentSolutionAsVector = this.getRandomInitSolution(d, l, numberOfImplicitFeatures);
		Pair<INDArray, INDArray> currentUAndVAsMatrix = this.vector2matrices(currentSolutionAsVector, d,
				numberOfImplicitFeatures, l, numberOfImplicitFeatures);

		logger.debug("randomly initialized U = {} ({} x {})",currentUAndVAsMatrix.getX(),d,numberOfImplicitFeatures);
		logger.debug("randomly initialized V = {} ({} x {})",currentUAndVAsMatrix.getY(),l,numberOfImplicitFeatures);

		/* determine cost */
		double currentCost = this.getCost(currentUAndVAsMatrix.getX(), currentUAndVAsMatrix.getY());

		logger.debug("loss of randomly initialized U and V: {}",currentCost);

		CostFunction cf = input -> {
			Pair<INDArray, INDArray> uAndV = this.vector2matrices(input, d, numberOfImplicitFeatures, l,
					numberOfImplicitFeatures);
			INDArray uIntermediate = uAndV.getX();
			INDArray vIntermediate = uAndV.getY();
			assert uIntermediate.rows() == d && uIntermediate.columns() == numberOfImplicitFeatures : "Incorrect shape of U: (" + uIntermediate.rows()
			+ " x " + uIntermediate.columns() + ") instead of (" + d + " x " + numberOfImplicitFeatures + ")";
			assert vIntermediate.rows() == l && vIntermediate.columns() == numberOfImplicitFeatures : "Incorrect shape of V: (" + vIntermediate.rows()
			+ " x " + vIntermediate.columns() + ") instead of (" + l + " x " + numberOfImplicitFeatures + ")";
			double cost = this.getCost(uIntermediate, vIntermediate);
			INDArray gradientMatrixForU = this.getGradientAsMatrix(uIntermediate, vIntermediate, true);
			INDArray gradientMatrixForV = this.getGradientAsMatrix(uIntermediate, vIntermediate, false);
			return new CostGradientTuple(cost,
					this.matrices2vector(gradientMatrixForU, gradientMatrixForV));
		};

		/* probe algorithm with different alphas */
		double alpha = ALPHA_START;
		int turnsWithoutImprovement = 0;
		while (currentCost > MAX_DESIRED_ERROR) {
			double lastCost = currentCost;
			DoubleVector lastSolution = currentSolutionAsVector;
			GradientDescent gd = new GradientDescent(alpha, LIMIT);
			currentSolutionAsVector = gd.minimize(cf, currentSolutionAsVector, ITERATIONS_PER_PROBE, false);
			logger.debug("Produced gd solution vector {}", currentSolutionAsVector);

			/* if the current solution contains non-numbers, shrink alpha and use last solution again */
			boolean hasNanEntry = false;
			for (int i = 0; i < currentSolutionAsVector.getLength(); i++) {
				if (Double.valueOf(currentSolutionAsVector.get(i)).equals(Double.NaN)) {
					hasNanEntry = true;
					break;
				}
			}

			if (hasNanEntry) {
				currentSolutionAsVector = lastSolution;
				currentCost = lastCost;
				if (alpha > 1e-20) {
					alpha /= 2;
				}
			}
			else {
				currentUAndVAsMatrix = this.vector2matrices(currentSolutionAsVector, d, numberOfImplicitFeatures, l,
						numberOfImplicitFeatures);
				currentCost = this.getCost(currentUAndVAsMatrix.getX(), currentUAndVAsMatrix.getY());
				if (lastCost <= currentCost) {
					currentSolutionAsVector = lastSolution;
					currentCost = lastCost;
					if (lastCost == currentCost) {
						turnsWithoutImprovement ++;
						alpha *= 2;
					}
					else if (alpha > 1e-20) {
						alpha /= 2;
					}
					if (turnsWithoutImprovement > 10) {
						logger.debug("No further improvement, canceling");
						break;
					}
				} else {
					if (!succesfullyBooted) {
						succesfullyBooted = true;
					}
					turnsWithoutImprovement = 0;
					alpha *= 2;
				}
				alpha = Math.min(alpha, ALPHA_MAX);

				logger.debug("Current cost: {} (alpha= {})",currentCost,alpha);
			}

			/* if we have not successfully booted yet, draw a new random initialization */
			if (!succesfullyBooted) {
				currentSolutionAsVector = this.getRandomInitSolution(d, l, numberOfImplicitFeatures);
				currentUAndVAsMatrix = this.vector2matrices(currentSolutionAsVector, d, numberOfImplicitFeatures, l,
						numberOfImplicitFeatures);
				currentCost = this.getCost(currentUAndVAsMatrix.getX(), currentUAndVAsMatrix.getY());
				alpha = ALPHA_START;
				logger.info("Rebooting approach with solution vector {} that has cost {}", currentSolutionAsVector, currentCost);
			}
		}
		this.u = currentUAndVAsMatrix.getX();
		this.v = currentUAndVAsMatrix.getY();

		logger.info("Finished learning");
		logger.debug("U = {}",this.u);
		logger.debug("V = {}",this.v);
	}

	private DoubleVector getRandomInitSolution(final int d, final int l, final int numberOfImplicitFeatures) {
		double[] denseVector = new double[(d + l) * numberOfImplicitFeatures];
		int c = 0;
		for (int i = 0; i < d; i++) {
			for (int j = 0; j < numberOfImplicitFeatures; j++) {
				denseVector[c++] = (this.rand.nextDouble() - 0.5) * 100;
			}
		}
		for (int i = 0; i < l; i++) {
			for (int j = 0; j < numberOfImplicitFeatures; j++) {
				denseVector[c++] = (this.rand.nextDouble() - 0.5) * 100;
			}
		}
		return new DenseDoubleVector(denseVector);
	}

	/**
	 * creates a matrix of the Nd4j framework from a vector of Thomas Jungblut's
	 * math framework
	 *
	 * @param vector
	 * @param m
	 * @param n
	 * @return
	 */
	public INDArray vector2matrix(final DoubleVector vector, final int m, final int n) {
		double[] inputs = new double[vector.getLength()];
		for (int i = 0; i < vector.getLength(); i++) {
			inputs[i] = vector.get(i);
		}
		return Nd4j.create(inputs, new int[] { m, n });
	}

	public Pair<INDArray, INDArray> vector2matrices(final DoubleVector vector, final int n, final int d, final int m, final int l) {
		DoubleVector inputForU = vector.sliceByLength(0, n * d);
		DoubleVector inputForV = vector.sliceByLength(n * d, vector.getLength() - inputForU.getLength());
		INDArray uIntermediate = this.vector2matrix(inputForU, n, d);
		INDArray vIntermediate = this.vector2matrix(inputForV, m, l);
		return new Pair<>(uIntermediate, vIntermediate);
	}

	/**
	 * collapses a matrix of the Nd4j framework into a double vector of Thomas
	 * Jungblut's framework
	 *
	 * @param matrix
	 * @return
	 */
	public DoubleVector matrix2vector(final INDArray matrix) {
		int m = matrix.rows();
		int n = matrix.columns();
		double[] denseVector = new double[m * n];
		int c = 0;
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				denseVector[c++] = matrix.getDouble(i, j);
			}
		}
		return new DenseDoubleVector(denseVector);
	}

	public DoubleVector matrices2vector(final INDArray... matrices) {
		List<DoubleVector> vectors = new ArrayList<>();
		int length = 0;
		for (INDArray matrix : matrices) {
			DoubleVector vector = this.matrix2vector(matrix);
			vectors.add(vector);
			length += vector.getLength();
		}
		double[] collapsed = new double[length];
		int c = 0;
		for (DoubleVector vector : vectors) {
			for (int i = 0; i < vector.getLength(); i++) {
				collapsed[c++] = vector.get(i);
			}
		}
		return new DenseDoubleVector(collapsed);
	}

	/**
	 * This evaluates F1
	 *
	 * @param RRT
	 * @param u
	 * @param x
	 * @return
	 */
	public double getCost(final INDArray u, final INDArray v) {
		INDArray z1 = this.x.mmul(u);
		INDArray z2 = this.w.mmul(v).transpose();
		INDArray z = z1.mmul(z2);
		INDArray q = this.r.sub(z);
		return this.getSquaredFrobeniusNorm(q) + this.mu * (this.getSquaredFrobeniusNorm(u) + this.getSquaredFrobeniusNorm(v));
	}

	public double getSquaredFrobeniusNorm(final INDArray matrix) {
		double norm = 0;
		int m = matrix.rows();
		int n = matrix.columns();
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				norm += Math.pow(matrix.getDouble(i, j), 2);
			}
		}
		return norm;
	}

	/**
	 * This computes the gradient of F1 in matrix form
	 *
	 * @param r
	 * @param u
	 * @param x
	 * @return
	 */
	public INDArray getGradientAsMatrix(final INDArray u, final INDArray v, final boolean computeDerivationsOfU) {
		if (computeDerivationsOfU) {
			int m = u.rows();
			int n = u.columns();
			float[][] derivatives = new float[m][n];
			for (int s = 0; s < m; s++) {
				for (int t = 0; t < n; t++) {
					derivatives[s][t] = this.getFirstDerivative(u, v, s, t, true);
				}
			}
			return Nd4j.create(derivatives);
		} else {
			int m = v.rows();
			int n = v.columns();
			float[][] derivatives = new float[m][n];
			for (int s = 0; s < m; s++) {
				for (int t = 0; t < n; t++) {
					derivatives[s][t] = this.getFirstDerivative(u, v, s, t, false);
				}
			}
			return Nd4j.create(derivatives);
		}
	}

	/**
	 * This compute the derivative of F1 for the (k,l)-th element of the U matrix
	 *
	 * @param RRT
	 * @param u
	 * @param x
	 * @param k
	 * @param l
	 * @return
	 */
	public float getFirstDerivative(final INDArray u, final INDArray v, final int s, final int t, final boolean deriveForU) {

		/* compute inner product Z := XUV^TW^T */
		INDArray z1 = this.x.mmul(u);
		INDArray z2 = this.w.mmul(v).transpose();
		INDArray z = z1.mmul(z2);

		/* define the difference of R and Z in Q */
		INDArray q = this.r.sub(z);
		float derivative = 0;
		int n = q.rows();
		int m = q.columns();
		assert m == this.w.rows() : "W has " + this.w.rows() + " but is expected to have m = " + m + " rows";
		if (t >= v.columns()) {
			throw new IllegalArgumentException("V has only " + v.columns() + " but would have to have " + (t + 1)
					+ " columns to proceed! I.e. deriving a derivative for t = " + t + " is not possible.");
		}

		/* compute derivative based on whether it is for a u-element or a v-element */
		if (deriveForU) {
			for (int i = 0; i < n; i++) {
				float xis = this.x.getFloat(i, s);
				for (int j = 0; j < m; j++) {
					double factor1 = q.getFloat(i, j);
					double factor2 = xis;
					double scalarProduct = this.w.getRow(j).mmul(v.getColumn(t)).getDouble(0, 0);
					derivative -= 2 * factor1 * factor2 * scalarProduct;
				}
			}
			derivative += 2 * this.mu * u.getDouble(s,t);
		} else {
			for (int i = 0; i < n; i++) {
				double scalarProduct = this.x.getRow(i).mmul(v.getColumn(t)).getDouble(0, 0);
				for (int j = 0; j < m; j++) {
					double factor1 = q.getFloat(i, j);
					double wjs = this.w.getFloat(j, s);
					derivative -= 2 * factor1 * wjs * scalarProduct;
				}
			}
			derivative += 2 * this.mu * v.getDouble(s,t);
		}
		return derivative;
	}

	@Override
	public double computeSimilarity(final INDArray x, final INDArray w) {
		return x.mmul(this.u).mmul(this.v.transpose()).mmul(w.transpose()).getDouble(0);
	}
}
