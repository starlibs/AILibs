package ai.libs.jaicore.math.linearalgebra;

import java.util.Arrays;

import ai.libs.jaicore.math.random.RandomGenerator;
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
	public DenseDoubleVector(final int numberOfDimensions) {
		this.internalVector = new DenseVector(numberOfDimensions);
	}

	/**
	 * Creates a dense vector from the given data.
	 *
	 * @param data
	 *            A double array, which can be interpreted as a vector.
	 */
	public DenseDoubleVector(final double[] data) {
		this.internalVector = new DenseVector(Arrays.copyOf(data, data.length));
	}

	/**
	 * Creates a dense vector from an MTJ vector.
	 *
	 * @param vector
	 *            The MTJ vector.
	 */
	public DenseDoubleVector(final no.uib.cipr.matrix.Vector vector) {
		this.internalVector = vector;
	}

	/**
	 * Creates a new dense vector with the given size and paste for each entry the given value.
	 *
	 * @param size
	 *            The size of the dense vector.
	 * @param value
	 *            The value for each entry.
	 */
	public DenseDoubleVector(final int size, final double value) {
		this.internalVector = new DenseVector(size);
		for (int index = 0; index < this.internalVector.size(); index++) {
			this.internalVector.set(index, value);
		}
	}

	@Override
	public int length() {
		return this.internalVector.size();
	}

	@Override
	public double getValue(final int index) {
		return this.internalVector.get(index);
	}

	@Override
	public void setValue(final int index, final double value) {
		this.internalVector.set(index, value);
	}

	@Override
	public void addConstant(final double constant) {
		double[] contantAsVector = new double[this.internalVector.size()];
		for (int i = 0; i < contantAsVector.length; i++) {
			contantAsVector[i] = constant;
		}
		this.addVector(contantAsVector);
	}

	@Override
	public void addVector(final Vector vector) {
		this.internalVector = this.internalVector.add(vector.toDenseVector().internalVector);
	}

	@Override
	public void subtractConstant(final double constant) {
		this.addConstant(-1 * constant);
	}

	@Override
	public void subtractVector(final Vector vector) {
		this.internalVector = this.internalVector.add(-1, vector.toDenseVector().internalVector);
	}

	@Override
	public void multiplyByVectorPairwise(final Vector secondVector) {
		for (int i = 0; i < this.internalVector.size(); i++) {
			this.internalVector.set(i, this.internalVector.get(i) * secondVector.getValue(i));
		}
	}

	@Override
	public void multiplyByConstant(final double constant) {
		this.internalVector = this.internalVector.scale(constant);
	}

	@Override
	public void divideByVectorPairwise(final Vector secondVector) {
		for (int i = 0; i < this.internalVector.size(); i++) {
			this.internalVector.set(i, this.internalVector.get(i) / secondVector.getValue(i));
		}
	}

	@Override
	public void divideByConstant(final double constant) {
		this.internalVector = this.internalVector.scale(1 / constant);
	}

	@Override
	public double dotProduct(final Vector vector) {
		return this.internalVector.dot(vector.toDenseVector().internalVector);
	}

	@Override
	public boolean isSparse() {
		return false;
	}

	@Override
	public double[] asArray() {
		double[] result = new double[this.internalVector.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = this.internalVector.get(i);
		}
		return result;
	}

	@Override
	public void addVector(final double[] vectorAsArray) {
		this.addVector(new DenseDoubleVector(vectorAsArray));
	}

	@Override
	public void subtractVector(final double[] vectorAsArray) {
		this.subtractVector(new DenseDoubleVector(vectorAsArray));
	}

	@Override
	public void multiplyByVectorPairwise(final double[] vectorAsArray) {
		this.multiplyByVectorPairwise(new DenseDoubleVector(vectorAsArray));
	}

	@Override
	public void divideByVectorPairwise(final double[] vectorAsArray) {
		this.divideByVectorPairwise(new DenseDoubleVector(vectorAsArray));
	}

	@Override
	public double dotProduct(final double[] vectorAsArray) {
		return this.dotProduct(new DenseDoubleVector(vectorAsArray));
	}

	@Override
	public Vector duplicate() {
		return new DenseDoubleVector(this.asArray());
	}

	@Override
	public void normalize() {
		this.internalVector = this.internalVector.scale(1 / this.internalVector.norm(Norm.Two));
	}

	@Override
	public void fillRandomly() {
		for (int numberOfAddedValues = 0; numberOfAddedValues < this.internalVector.size(); numberOfAddedValues++) {
			double fillValue = RandomGenerator.getRNG().nextDouble();
			this.internalVector.set(numberOfAddedValues, fillValue);
		}
	}

	@Override
	public DenseDoubleVector toDenseVector() {
		return this;
	}

	@Override
	public SparseDoubleVector toSparseVector() {
		return new SparseDoubleVector(this.asArray());
	}

	@Override
	public Vector kroneckerProduct(final double[] vectorAsArray) {
		return new DenseDoubleVector(this.kroneckerProductInternal(vectorAsArray));
	}
}
