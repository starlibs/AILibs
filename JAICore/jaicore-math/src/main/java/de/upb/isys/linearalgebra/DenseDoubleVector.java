package de.upb.isys.linearalgebra;

import java.util.Arrays;

import de.upb.isys.random.RandomGenerator;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector.Norm;

/**
 * Dense vector implementation wrapping the MTJ implementation of a dense vector.
 * 
 * @author Alexander Hetzer
 */
public class DenseDoubleVector extends AbstractVector {

	private no.uib.cipr.matrix.Vector internalVector;

	/**
	 * Creates a dense vector with the given amount of dimensions, initialized with zeros.
	 * 
	 * @param numberOfDimensions
	 *            The number of dimensions of this vector.
	 */
	public DenseDoubleVector(int numberOfDimensions) {
		internalVector = new DenseVector(numberOfDimensions);
	}

	/**
	 * Creates a dense vector from the given data.
	 * 
	 * @param data
	 *            A double array, which can be interpreted as a vector.
	 */
	public DenseDoubleVector(double[] data) {
		internalVector = new DenseVector(Arrays.copyOf(data, data.length));
	}

	/**
	 * Creates a dense vector from an MTJ vector.
	 * 
	 * @param vector
	 *            The MTJ vector.
	 */
	public DenseDoubleVector(no.uib.cipr.matrix.Vector vector) {
		internalVector = vector;
	}

	/**
	 * Creates a new dense vector with the given size and paste for each entry the given value.
	 *
	 * @param size
	 *            The size of the dense vector.
	 * @param value
	 *            The value for each entry.
	 */
	public DenseDoubleVector(int size, double value) {
		internalVector = new DenseVector(size);
		for (int index = 0; index < internalVector.size(); index++) {
			internalVector.set(index, value);
		}
	}

	@Override
	public int length() {
		return internalVector.size();
	}

	@Override
	public double getValue(int index) {
		return internalVector.get(index);
	}

	@Override
	public void setValue(int index, double value) {
		internalVector.set(index, value);
	}

	@Override
	public void addConstant(double constant) {
		double[] contantAsVector = new double[internalVector.size()];
		for (int i = 0; i < contantAsVector.length; i++) {
			contantAsVector[i] = constant;
		}
		addVector(contantAsVector);
	}

	@Override
	public void addVector(Vector vector) {
		internalVector = internalVector.add(vector.toDenseVector().internalVector);
	}

	@Override
	public void subtractConstant(double constant) {
		addConstant(-1 * constant);
	}

	@Override
	public void subtractVector(Vector vector) {
		internalVector = internalVector.add(-1, vector.toDenseVector().internalVector);
	}

	@Override
	public void multiplyByVectorPairwise(Vector secondVector) {
		for (int i = 0; i < internalVector.size(); i++) {
			internalVector.set(i, internalVector.get(i) * secondVector.getValue(i));
		}
	}

	@Override
	public void multiplyByConstant(double constant) {
		internalVector = internalVector.scale(constant);
	}

	@Override
	public void divideByVectorPairwise(Vector secondVector) {
		for (int i = 0; i < internalVector.size(); i++) {
			internalVector.set(i, internalVector.get(i) / secondVector.getValue(i));
		}
	}

	@Override
	public void divideByConstant(double constant) {
		internalVector = internalVector.scale(1 / constant);
	}

	@Override
	public double dotProduct(Vector vector) {
		return internalVector.dot(vector.toDenseVector().internalVector);
	}

	@Override
	public boolean isSparse() {
		return false;
	}

	@Override
	public double[] asArray() {
		double[] result = new double[internalVector.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = internalVector.get(i);
		}
		return result;
	}

	@Override
	public void addVector(double[] vectorAsArray) {
		addVector(new DenseDoubleVector(vectorAsArray));
	}

	@Override
	public void subtractVector(double[] vectorAsArray) {
		subtractVector(new DenseDoubleVector(vectorAsArray));
	}

	@Override
	public void multiplyByVectorPairwise(double[] vectorAsArray) {
		multiplyByVectorPairwise(new DenseDoubleVector(vectorAsArray));
	}

	@Override
	public void divideByVectorPairwise(double[] vectorAsArray) {
		divideByVectorPairwise(new DenseDoubleVector(vectorAsArray));
	}

	@Override
	public double dotProduct(double[] vectorAsArray) {
		return dotProduct(new DenseDoubleVector(vectorAsArray));
	}

	@Override
	public Vector duplicate() {
		return new DenseDoubleVector(asArray());
	}

	@Override
	public void normalize() {
		internalVector = internalVector.scale(1 / internalVector.norm(Norm.Two));
	}

	@Override
	public void fillRandomly() {
		for (int numberOfAddedValues = 0; numberOfAddedValues < internalVector.size(); numberOfAddedValues++) {
			double fillValue = RandomGenerator.getRNG().nextDouble();
			internalVector.set(numberOfAddedValues, fillValue);
		}
	}

	@Override
	public DenseDoubleVector toDenseVector() {
		return this;
	}

	@Override
	public SparseDoubleVector toSparseVector() {
		return new SparseDoubleVector(asArray());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((internalVector == null) ? 0 : internalVector.hashCode());
		return result;
	}

}
