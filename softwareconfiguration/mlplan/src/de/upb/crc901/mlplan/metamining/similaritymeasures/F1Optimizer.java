package de.upb.crc901.mlplan.metamining.similaritymeasures;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.minimize.CostFunction;
import de.jungblut.math.minimize.CostGradientTuple;
import de.jungblut.math.minimize.GradientDescent;

public class F1Optimizer implements IHeterogenousSimilarityMeasureComputer {
	private final static double ALPHA_START = 0.000000001; // learning rate
	private final static double ALPHA_MAX = 1e-5;
	private final static int ITERATIONS_PER_PROBE = 100;
	private final static int LIMIT = 1; // as long as the solution improves by at least this value, continue
	private final static boolean VERBOSE = false;
	private final static double MAX_DESIRED_ERROR = 0;
	
	private INDArray RRT;
	private INDArray X;
	private INDArray U; // the learned matrix
	
	/**
	 * Learns a matrix U that minimizes F1 (W is ignored here)
	 * 
	 * @return
	 */
	public void build(INDArray X, INDArray W, INDArray R) {
		this.RRT = R.mmul(R.transpose());
		this.X = X;
		
		final int m = X.columns();
		
		/* generate initial U vector */
		final int numberOfImplicitFeatures = 1;
		double[] denseVector = new double[m * numberOfImplicitFeatures];
		int c = 0;
		for (int i = 0; i < m; i++)
			for (int j = 0; j < numberOfImplicitFeatures; j++)
				denseVector[c++] = (Math.random() - 0.5) * 100;
		DoubleVector currentSolutionAsVector = new DenseDoubleVector(denseVector);
		INDArray currentSolutionAsMatrix = vector2matrix(currentSolutionAsVector, m, numberOfImplicitFeatures);
		double currentCost = getCost(currentSolutionAsMatrix);
		
		System.out.println("X = " + X);
		System.out.println("randomly initialized U = " + currentSolutionAsMatrix);
		System.out.println("loss of randomly initialized U: " + currentCost);
		CostFunction cf = new CostFunction() {
			
			@Override
			public CostGradientTuple evaluateCost(DoubleVector input) {
				INDArray U = vector2matrix(input, X.columns(), numberOfImplicitFeatures);
				double cost = getCost(U);
				INDArray gradientMatrix = getGradientAsMatrix(U);
				CostGradientTuple cgt = new CostGradientTuple(cost, matrix2vector(gradientMatrix));
				return cgt;
			}
		};
		
		/* probe algorithm with different alphas */
		double alpha = ALPHA_START;
		while (currentCost > MAX_DESIRED_ERROR) {
			double lastCost = currentCost;
			DoubleVector lastSolution = currentSolutionAsVector;
			GradientDescent gd = new GradientDescent(alpha, LIMIT);
			currentSolutionAsVector = gd.minimize(cf, currentSolutionAsVector, ITERATIONS_PER_PROBE, false);
			currentSolutionAsMatrix = vector2matrix(currentSolutionAsVector, m, numberOfImplicitFeatures);
			currentCost = getCost(currentSolutionAsMatrix);
			if (lastCost < currentCost) {
				currentSolutionAsVector = lastSolution;
				currentCost = lastCost;
				alpha /= 2;
			}
			else if (lastCost > currentCost)
				alpha *= 2;
			else
				break;
			alpha = Math.min(alpha, ALPHA_MAX);
			System.out.println(currentCost + " (alpha = " + alpha +")");
		}
		U = currentSolutionAsMatrix;
	}
	
	/**
	 * creates a matrix of the Nd4j framework from a vector of Thomas Jungblut's math framework
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
		return Nd4j.create(inputs, new int[] {m, n});
	}
	
	/**
	 * collapses a matrix of the Nd4j framework into a double vector of Thomas Jungblut's framework
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
				denseVector[c++] = matrix.getDouble(i,j);
		return new DenseDoubleVector(denseVector);
	}
	
	/**
	 * This evaluates F1
	 * 
	 * @param RRT
	 * @param U
	 * @param X
	 * @return
	 */
	public double getCost(INDArray U) {
		INDArray Z1 = X.mmul(U);
		INDArray Z2 = Z1.transpose();
		INDArray Z = Z1.mmul(Z2);
		INDArray Q = RRT.sub(Z);
		double cost = 0;
		int n = Q.columns();
		for (int i = 0; i < n; i++)
			for (int j = 0; j < n; j++)
				cost += Math.pow(Q.getDouble(i,j),2);
		return cost;
	}
	
	/**
	 * This computes the gradient of F1 in matrix form
	 * 
	 * @param R
	 * @param U
	 * @param X
	 * @return
	 */
	public INDArray getGradientAsMatrix(INDArray U) {
		int m = X.columns();
		int n = U.columns();
		float[][] derivatives = new float[m][n];
		for (int k = 0; k < m; k++) {
			for (int l = 0; l < n; l++) {
				derivatives[k][l] = getFirstDerivative(U, k, l);
			}
		}
		return Nd4j.create(derivatives);
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
	public float getFirstDerivative(INDArray U, int k, int l) {
		
		/* compute inner product Z := XU(XU)^-1 */
		INDArray Z1 = X.mmul(U);
		INDArray Z2 = Z1.transpose();
		INDArray Z = Z1.mmul(Z2);
		
		/* define the difference of RR^-1 and Z in Q */
		INDArray Q = RRT.sub(Z);
		
		/* now compute the inner product of the i-th row of X and the i-th column of U */
		int n = X.rows();
		float[] sums = new float[n];
		for (int i = 0; i < n; i++)
			sums[i] = X.getRow(i).mmul(U.getColumn(l)).getFloat(0,0);
		
		/* now compute the actual derivative */
		float derivative = 0;
		for (int i = 0; i < n; i++) {
			float Xik = X.getFloat(i,k);
			for (int j = 0; j < n; j++) {
				float sumA = Xik * sums[j];
				float sumB = X.getFloat(j,k) * sums[i];
				derivative += -2 * Q.getFloat(i,j) * (sumA + sumB);
			}
		}
		return derivative;
	}

	@Override
	public double computeSimilarity(INDArray x, INDArray w) {
		return 0;
	}

	public INDArray getX() {
		return X;
	}

	public INDArray getU() {
		return U;
	}
}
