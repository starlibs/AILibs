package de.upb.crc901.mlplan.metamining.similaritymeasures;

import java.util.ArrayList;
import java.util.List;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.minimize.CostFunction;
import de.jungblut.math.minimize.CostGradientTuple;
import de.jungblut.math.minimize.GradientDescent;
import jaicore.basic.sets.SetUtil.Pair;

public class F3Optimizer implements IHeterogenousSimilarityMeasureComputer {
	private final static Logger logger = LoggerFactory.getLogger(F3Optimizer.class);
	private final static double ALPHA_START = 0.000000001; // learning rate
	private final static double ALPHA_MAX = 1e-6;
	private final static int ITERATIONS_PER_PROBE = 100;
	private final static int LIMIT = 1; // as long as the solution improves by at least this value, continue
	private final static double MAX_DESIRED_ERROR = 0;

	private final double mu; // regularization constant
	
	private INDArray R;
	private INDArray X;
	private INDArray W;
	private INDArray U, V; // the learned matrices

	public F3Optimizer(double mu) {
		super();
		this.mu = mu;
	}

	/**
	 * Learns a matrix U that minimizes F1 (W is ignored here)
	 * 
	 * @return
	 */
	public void build(INDArray X, INDArray W, INDArray R) {
		this.R = R;
		this.W = W;
		this.X = X;

		
		final int n = X.rows();
		final int d = X.columns();
		final int m = W.rows();
		final int l = W.columns();
		final int numberOfImplicitFeatures = 1;
		System.out.println("X = " + " (" + n + " x " + X.columns() + ")");
		System.out.println("W = " + " (" + m + " x " + W.columns() + ")");

		/* generate initial U and V vectors */
		boolean succesfullyBooted = false;
		DoubleVector currentSolutionAsVector = getRandomInitSolution(d, l, numberOfImplicitFeatures);
		Pair<INDArray, INDArray> currentUAndVAsMatrix = vector2matrices(currentSolutionAsVector, d,
				numberOfImplicitFeatures, l, numberOfImplicitFeatures);
		System.out.println("randomly initialized U = " + currentUAndVAsMatrix.getX() + " (" + d + " x "
				+ numberOfImplicitFeatures + ")");
		System.out.println("randomly initialized V = " + currentUAndVAsMatrix.getY() + " (" + l + " x "
				+ numberOfImplicitFeatures + ")");

		/* determine cost */
		double currentCost = getCost(currentUAndVAsMatrix.getX(), currentUAndVAsMatrix.getY());
		System.out.println("loss of randomly initialized U and V: " + currentCost);
		CostFunction cf = new CostFunction() {

			@Override
			public CostGradientTuple evaluateCost(DoubleVector input) {
				Pair<INDArray, INDArray> UAndV = vector2matrices(input, d, numberOfImplicitFeatures, l,
						numberOfImplicitFeatures);
				INDArray U = UAndV.getX();
				INDArray V = UAndV.getY();
				assert U.rows() == d && U.columns() == numberOfImplicitFeatures : "Incorrect shape of U: (" + U.rows()
						+ " x " + U.columns() + ") instead of (" + d + " x " + numberOfImplicitFeatures + ")";
				assert V.rows() == l && V.columns() == numberOfImplicitFeatures : "Incorrect shape of V: (" + V.rows()
						+ " x " + V.columns() + ") instead of (" + l + " x " + numberOfImplicitFeatures + ")";
				double cost = getCost(U, V);
				INDArray gradientMatrixForU = getGradientAsMatrix(U, V, true);
				INDArray gradientMatrixForV = getGradientAsMatrix(U, V, false);
				CostGradientTuple cgt = new CostGradientTuple(cost,
						matrices2vector(gradientMatrixForU, gradientMatrixForV));
				return cgt;
			}
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
				if (alpha > 1e-20)
					alpha /= 2;
			}
			else {
				currentUAndVAsMatrix = vector2matrices(currentSolutionAsVector, d, numberOfImplicitFeatures, l,
						numberOfImplicitFeatures);
				currentCost = getCost(currentUAndVAsMatrix.getX(), currentUAndVAsMatrix.getY());
				if (lastCost <= currentCost) {
					currentSolutionAsVector = lastSolution;
					currentCost = lastCost;
					if (lastCost == currentCost) {
						turnsWithoutImprovement ++;
						alpha *= 2;
					}
					else if (alpha > 1e-20)
						alpha /= 2;
					if (turnsWithoutImprovement > 10) {
						System.out.println("No further improvement, canceling");
						break;
					}
				} else {
					if (!succesfullyBooted)
						succesfullyBooted = true;
					turnsWithoutImprovement = 0;
					alpha *= 2;
				}
				alpha = Math.min(alpha, ALPHA_MAX);
				System.out.println(currentCost + " (alpha = " + alpha + ")");
			}
			
			/* if we have not successfully booted yet, draw a new random initialization */
			if (!succesfullyBooted) {
				currentSolutionAsVector = getRandomInitSolution(d, l, numberOfImplicitFeatures);
				currentUAndVAsMatrix = vector2matrices(currentSolutionAsVector, d, numberOfImplicitFeatures, l,
						numberOfImplicitFeatures);
				currentCost = getCost(currentUAndVAsMatrix.getX(), currentUAndVAsMatrix.getY());
				alpha = ALPHA_START;
				logger.info("Rebooting approach with solution vector {} that has cost {}", currentSolutionAsVector, currentCost);
			}
		}
		U = currentUAndVAsMatrix.getX();
		V = currentUAndVAsMatrix.getY();
		System.out.println("Finished learning");
		System.out.println("U = " + U);
		System.out.println("V = " + V);
	}
	
	private DoubleVector getRandomInitSolution(int d, int l, int numberOfImplicitFeatures) {
		double[] denseVector = new double[(d + l) * numberOfImplicitFeatures];
		int c = 0;
		for (int i = 0; i < d; i++)
			for (int j = 0; j < numberOfImplicitFeatures; j++)
				denseVector[c++] = (Math.random() - 0.5) * 100;
		for (int i = 0; i < l; i++)
			for (int j = 0; j < numberOfImplicitFeatures; j++)
				denseVector[c++] = (Math.random() - 0.5) * 100;
		DoubleVector currentSolutionAsVector = new DenseDoubleVector(denseVector);
		return currentSolutionAsVector;
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
	public INDArray vector2matrix(DoubleVector vector, int m, int n) {
		double[] inputs = new double[vector.getLength()];
		for (int i = 0; i < vector.getLength(); i++)
			inputs[i] = vector.get(i);
		return Nd4j.create(inputs, new int[] { m, n });
	}

	public Pair<INDArray, INDArray> vector2matrices(DoubleVector vector, int n, int d, int m, int l) {
		DoubleVector inputForU = vector.sliceByLength(0, n * d);
		DoubleVector inputForV = vector.sliceByLength(n * d, vector.getLength() - inputForU.getLength());
		INDArray U = vector2matrix(inputForU, n, d);
		INDArray V = vector2matrix(inputForV, m, l);
		return new Pair<>(U, V);
	}

	/**
	 * collapses a matrix of the Nd4j framework into a double vector of Thomas
	 * Jungblut's framework
	 * 
	 * @param matrix
	 * @return
	 */
	public DoubleVector matrix2vector(INDArray matrix) {
		int m = matrix.rows();
		int n = matrix.columns();
		double[] denseVector = new double[m * n];
		int c = 0;
		for (int i = 0; i < m; i++)
			for (int j = 0; j < n; j++)
				denseVector[c++] = matrix.getDouble(i, j);
		return new DenseDoubleVector(denseVector);
	}

	public DoubleVector matrices2vector(INDArray... matrices) {
		List<DoubleVector> vectors = new ArrayList<>();
		int length = 0;
		for (INDArray matrix : matrices) {
			DoubleVector vector = matrix2vector(matrix);
			vectors.add(vector);
			length += vector.getLength();
		}
		double[] collapsed = new double[length];
		int c = 0;
		for (DoubleVector vector : vectors) {
			for (int i = 0; i < vector.getLength(); i++)
				collapsed[c++] = vector.get(i);
		}
		return new DenseDoubleVector(collapsed);
	}

	/**
	 * This evaluates F1
	 * 
	 * @param RRT
	 * @param U
	 * @param X
	 * @return
	 */
	public double getCost(INDArray U, INDArray V) {
		INDArray Z1 = X.mmul(U);
		INDArray Z2 = W.mmul(V).transpose();
		INDArray Z = Z1.mmul(Z2);
		INDArray Q = R.sub(Z);
		return getSquaredFrobeniusNorm(Q) + mu * (getSquaredFrobeniusNorm(U) + getSquaredFrobeniusNorm(V));
	}
	
	public double getSquaredFrobeniusNorm(INDArray matrix) {
		double norm = 0;
		int m = matrix.rows();
		int n = matrix.columns();
		for (int i = 0; i < m; i++)
			for (int j = 0; j < n; j++)
				norm += Math.pow(matrix.getDouble(i, j), 2);
		return norm;
	}

	/**
	 * This computes the gradient of F1 in matrix form
	 * 
	 * @param R
	 * @param U
	 * @param X
	 * @return
	 */
	public INDArray getGradientAsMatrix(INDArray U, INDArray V, boolean computeDerivationsOfU) {
		if (computeDerivationsOfU) {
			int m = U.rows();
			int n = U.columns();
			float[][] derivatives = new float[m][n];
			for (int s = 0; s < m; s++) {
				for (int t = 0; t < n; t++) {
					derivatives[s][t] = getFirstDerivative(U, V, s, t, true);
				}
			}
			return Nd4j.create(derivatives);
		} else {
			int m = V.rows();
			int n = V.columns();
			float[][] derivatives = new float[m][n];
			for (int s = 0; s < m; s++) {
				for (int t = 0; t < n; t++) {
					derivatives[s][t] = getFirstDerivative(U, V, s, t, false);
				}
			}
			return Nd4j.create(derivatives);
		}
	}

	/**
	 * This compute the derivative of F1 for the (k,l)-th element of the U matrix
	 * 
	 * @param RRT
	 * @param U
	 * @param X
	 * @param k
	 * @param l
	 * @return
	 */
	public float getFirstDerivative(INDArray U, INDArray V, int s, int t, boolean deriveForU) {

		/* compute inner product Z := XUV^TW^T */
		INDArray Z1 = X.mmul(U);
		INDArray Z2 = W.mmul(V).transpose();
		INDArray Z = Z1.mmul(Z2);

		/* define the difference of R and Z in Q */
		INDArray Q = R.sub(Z);
		float derivative = 0;
		int n = Q.rows();
		int m = Q.columns();
		assert m == W.rows() : "W has " + W.rows() + " but is expected to have m = " + m + " rows";
		assert t < V.columns() : "V has only " + V.columns() + " but would have to have " + (t + 1)
				+ " columns to proceed! I.e. deriving a derivative for t = " + t + " is not possible.";
		
		/* compute derivative based on whether it is for a u-element or a v-element */
		if (deriveForU) {
			for (int i = 0; i < n; i++) {
				float Xis = X.getFloat(i, s);
				for (int j = 0; j < m; j++) {
					double factor1 = Q.getFloat(i, j);
					double factor2 = Xis;
					double scalarProduct = W.getRow(j).mmul(V.getColumn(t)).getDouble(0, 0);
					derivative -= 2 * factor1 * factor2 * scalarProduct;
				}
			}
			derivative += 2 * mu * U.getDouble(s,t);
		} else {
			for (int i = 0; i < n; i++) {
				double scalarProduct = X.getRow(i).mmul(V.getColumn(t)).getDouble(0, 0);
				for (int j = 0; j < m; j++) {
					double factor1 = Q.getFloat(i, j);
					double Wjs = W.getFloat(j, s);
					derivative -= 2 * factor1 * Wjs * scalarProduct;
				}
			}
			derivative += 2 * mu * V.getDouble(s,t);
		}
		return derivative;
	}

	@Override
	public double computeSimilarity(INDArray x, INDArray w) {
		return x.mmul(U).mmul(V.transpose()).mmul(w.transpose()).getDouble(0);
	}
}
