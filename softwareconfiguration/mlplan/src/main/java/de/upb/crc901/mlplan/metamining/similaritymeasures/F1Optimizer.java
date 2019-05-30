package de.upb.crc901.mlplan.metamining.similaritymeasures;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.math.minimize.CostFunction;
import de.jungblut.math.minimize.CostGradientTuple;
import de.jungblut.math.minimize.GradientDescent;

public class F1Optimizer implements IHeterogenousSimilarityMeasureComputer {
	
	private Logger logger = LoggerFactory.getLogger(F1Optimizer.class);
	
	private static final  double ALPHA_START = 0.000000001; // learning rate
	private static final double ALPHA_MAX = 1e-5;
	private static final int ITERATIONS_PER_PROBE = 100;
	private static final int LIMIT = 1; // as long as the solution improves by at least this value, continue
	private static final double MAX_DESIRED_ERROR = 0;
	
	private INDArray rrt;
	private INDArray x;
	private INDArray u; // the learned matrix
	
	/**
	 * Learns a matrix U that minimizes F1 (W is ignored here)
	 * 
	 * @return
	 */
	public void build(INDArray x, INDArray w, INDArray r) {
		this.rrt = r.mmul(r.transpose());
		this.x = x;
		
		final int m = x.columns();
		
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
		
		logger.debug("X = {}",x);
		logger.debug("randomly initialized U = {}",currentSolutionAsMatrix);
		logger.debug("loss of randomly initialized U: {}",currentCost);
				
		CostFunction cf = input -> {
			INDArray uIntermediate = vector2matrix(input, x.columns(), numberOfImplicitFeatures);
			double cost = getCost(uIntermediate);
			INDArray gradientMatrix = getGradientAsMatrix(uIntermediate);
			return new CostGradientTuple(cost, matrix2vector(gradientMatrix));
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

			logger.debug("Current Cost {} (alpha = {})",currentCost,alpha);
		}
		
		u = currentSolutionAsMatrix;
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
	 * @param rrt
	 * @param u
	 * @param x
	 * @return
	 */
	public double getCost(INDArray u) {
		INDArray z1 = x.mmul(u);
		INDArray z2 = z1.transpose();
		INDArray z = z1.mmul(z2);
		INDArray q = rrt.sub(z);
		double cost = 0;
		int n = q.columns();
		for (int i = 0; i < n; i++)
			for (int j = 0; j < n; j++)
				cost += Math.pow(q.getDouble(i,j),2);
		return cost;
	}
	
	/**
	 * This computes the gradient of F1 in matrix form
	 * 
	 * @param R
	 * @param u
	 * @param x
	 * @return
	 */
	public INDArray getGradientAsMatrix(INDArray u) {
		int m = x.columns();
		int n = u.columns();
		float[][] derivatives = new float[m][n];
		for (int k = 0; k < m; k++) {
			for (int l = 0; l < n; l++) {
				derivatives[k][l] = getFirstDerivative(u, k, l);
			}
		}
		return Nd4j.create(derivatives);
	}
	
	/**
	 * This compute the derivative of F1 for the (k,l)-th element of the U matrix
	 * 
	 * @param rrt
	 * @param u
	 * @param x
	 * @param k
	 * @param l
	 * @return
	 */
	public float getFirstDerivative(INDArray u, int k, int l) {
		
		/* compute inner product Z := XU(XU)^-1 */
		INDArray z1 = x.mmul(u);
		INDArray z2 = z1.transpose();
		INDArray z = z1.mmul(z2);
		
		/* define the difference of RR^-1 and Z in Q */
		INDArray q = rrt.sub(z);
		
		/* now compute the inner product of the i-th row of X and the i-th column of U */
		int n = x.rows();
		float[] sums = new float[n];
		for (int i = 0; i < n; i++)
			sums[i] = x.getRow(i).mmul(u.getColumn(l)).getFloat(0,0);
		
		/* now compute the actual derivative */
		float derivative = 0;
		for (int i = 0; i < n; i++) {
			float xik = x.getFloat(i,k);
			for (int j = 0; j < n; j++) {
				float sumA = xik * sums[j];
				float sumB = x.getFloat(j,k) * sums[i];
				derivative += -2 * q.getFloat(i,j) * (sumA + sumB);
			}
		}
		return derivative;
	}

	@Override
	public double computeSimilarity(INDArray x, INDArray w) {
		return 0;
	}

	public INDArray getX() {
		return x;
	}

	public INDArray getU() {
		return u;
	}
}
