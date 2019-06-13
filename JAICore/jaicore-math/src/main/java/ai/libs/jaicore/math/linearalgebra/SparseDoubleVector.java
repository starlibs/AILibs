package ai.libs.jaicore.math.linearalgebra;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import ai.libs.jaicore.math.random.RandomGenerator;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector.Norm;
import no.uib.cipr.matrix.sparse.SparseVector;

/**
 * Sparse vector implementation wrapping the MTJ implementation of a sparse vector.
 *
 * @author Alexander Hetzer
 */
public class SparseDoubleVector extends AbstractVector {

	protected SparseVector internalVector;

	private boolean isChanged = true;

	/**
	 * Creates a new SparseDoubleVector which contains the given values.
	 *
	 * @param indices
	 *            An array which includes all indices for which there exists a value.
	 * @param values
	 *            An array which contains all values.
	 * @param dimension
	 *            The total dimension of the vector.
	 */
	public SparseDoubleVector(final int[] indices, final double[] values, final int dimension) {
		this.internalVector = new SparseVector(dimension, indices, values);
		this.setIsChanged();
	}

	/**
	 * Creates a new SparseDoubleVector which contains only zero values.
	 *
	 * @param dimension
	 *            The dimension of the vector.
	 */
	public SparseDoubleVector(final int dimension) {
		this.internalVector = new SparseVector(dimension);
	}

	/**
	 * Creates a new SparseDoubleVector which contains the given values.
	 *
	 * @param data
	 *            A double array, which can be interpreted as a vector.
	 */
	public SparseDoubleVector(final double[] data) {
		List<Integer> indicesWithNonZeroEntry = new ArrayList<>();
		List<Double> nonZeroEntries = new ArrayList<>();
		for (int i = 0; i < data.length; i++) {
			if (Double.compare(data[i], 0.0) != 0) {
				indicesWithNonZeroEntry.add(i);
				nonZeroEntries.add(data[i]);
			}
		}

		this.internalVector = new SparseVector(data.length, indicesWithNonZeroEntry.stream().mapToInt(i -> i).toArray(), nonZeroEntries.stream().mapToDouble(d -> d).toArray());
		this.setIsChanged();
	}

	/**
	 * Creates a new SparseDoubleVector from an MTJ {@link SparseVector}.
	 *
	 * @param mtjVector
	 *            The MTJ vector.
	 */
	public SparseDoubleVector(final SparseVector mtjVector) {
		this.internalVector = mtjVector;
		this.setIsChanged();
	}

	@Override
	public void addVector(final double[] vectorAsArray) {
		this.setIsChanged();
		this.addVector(new SparseDoubleVector(vectorAsArray));
	}

	@Override
	public void subtractVector(final double[] vectorAsArray) {
		this.setIsChanged();
		this.internalVector = (SparseVector) this.internalVector.add(-1, new SparseDoubleVector(vectorAsArray).internalVector);
	}

	@Override
	public void multiplyByVectorPairwise(final double[] vectorAsArray) {
		this.setIsChanged();
		SparseVector vector = this.internalVector;
		int[] indexes = vector.getIndex();
		for (int i = 0; i < indexes.length; i++) {
			vector.set(indexes[i], vector.get(indexes[i]) * vectorAsArray[indexes[i]]);
		}
		this.internalVector = vector;
	}

	@Override
	public void divideByVectorPairwise(final double[] vectorAsArray) {
		this.setIsChanged();
		SparseVector vector = this.internalVector;
		int[] indexes = vector.getIndex();
		for (int i = 0; i < indexes.length; i++) {
			vector.set(indexes[i], vector.get(indexes[i]) / vectorAsArray[indexes[i]]);
		}
		this.internalVector = vector;
	}

	@Override
	public double dotProduct(final double[] vectorAsArray) {
		return this.internalVector.dot(new DenseVector(vectorAsArray));
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
		this.setIsChanged();
		this.internalVector.set(index, value);
	}

	@Override
	public void addVector(final Vector vector) {
		this.setIsChanged();
		this.internalVector = (SparseVector) this.internalVector.add(vector.toSparseVector().internalVector);
	}

	@Override
	public void subtractVector(final Vector vector) {
		this.setIsChanged();
		this.internalVector = (SparseVector) this.internalVector.add(-1, vector.toSparseVector().internalVector);
	}

	@Override
	public void multiplyByVectorPairwise(final Vector secondVector) {
		this.setIsChanged();
		SparseVector sparseVector = this.internalVector;
		int[] indexes = sparseVector.getIndex();
		for (int i = 0; i < indexes.length; i++) {
			sparseVector.set(indexes[i], sparseVector.get(indexes[i]) * secondVector.getValue(indexes[i]));
		}
		this.internalVector = sparseVector;
	}

	@Override
	public void multiplyByConstant(final double constant) {
		this.setIsChanged();
		this.internalVector = this.internalVector.scale(constant);
	}

	@Override
	public void divideByVectorPairwise(final Vector secondVector) {
		this.setIsChanged();
		SparseVector sparseVector = this.internalVector;
		int[] indexes = sparseVector.getIndex();
		for (int i = 0; i < indexes.length; i++) {
			sparseVector.set(indexes[i], sparseVector.get(indexes[i]) / secondVector.getValue(indexes[i]));
		}
		this.internalVector = sparseVector;
	}

	@Override
	public void divideByConstant(final double constant) {
		this.setIsChanged();
		this.internalVector = this.internalVector.scale(1 / constant);
	}

	@Override
	public double dotProduct(final Vector vector) {
		return this.internalVector.dot(vector.toSparseVector().internalVector);
	}

	@Override
	public boolean isSparse() {
		return true;
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
	public DenseDoubleVector toDenseVector() {
		return new DenseDoubleVector(this.asArray());
	}

	@Override
	public SparseDoubleVector toSparseVector() {
		return this;
	}

	@Override
	public Vector duplicate() {
		return new SparseDoubleVector(this.asArray());
	}

	@Override
	public void normalize() {
		this.setIsChanged();
		this.internalVector = this.internalVector.scale(this.internalVector.norm(Norm.Two));
	}

	@Override
	public void addConstant(final double constant) {
		this.setIsChanged();
		double[] contantAsVector = new double[this.internalVector.size()];
		for (int i = 0; i < contantAsVector.length; i++) {
			contantAsVector[i] = constant;
		}
		this.addVector(contantAsVector);
	}

	@Override
	public void subtractConstant(final double constant) {
		this.setIsChanged();
		this.addConstant(-1 * constant);
	}

	@Override
	public void fillRandomly() {
		this.setIsChanged();
		Random random = RandomGenerator.getRNG();
		int numberToAdd = random.nextInt(this.internalVector.size());
		List<Integer> unfilledIndexes = new ArrayList<>();
		for (int i = 0; i < this.internalVector.size(); i++) {
			unfilledIndexes.add(i);
		}
		for (int numberOfAddedValues = 0; numberOfAddedValues < numberToAdd; numberOfAddedValues++) {
			int randomIndex = random.nextInt(unfilledIndexes.size());
			int toBeFilledIndex = unfilledIndexes.get(randomIndex);
			double fillValue = random.nextDouble();
			this.internalVector.set(toBeFilledIndex, fillValue);
			unfilledIndexes.remove(0);
		}
	}

	/**
	 * Returns an array containing the non-zero indices of this sparse vector.
	 *
	 * @return an integer array containing the non-zero indices of this sparse vector
	 */
	public int[] getNonZeroIndices() {
		if (this.isChanged) {
			List<Integer> indicesWithNonZeroEntry = new ArrayList<>(this.length());
			List<Double> nonZeroEntries = new ArrayList<>(this.length());
			for (int i = 0; i < this.length(); i++) {
				double value = this.internalVector.get(i);
				if (Double.compare(value, 0.0) != 0) {
					indicesWithNonZeroEntry.add(i);
					nonZeroEntries.add(value);
				}
			}
			// do we need to recopy?
			if (indicesWithNonZeroEntry.size() != this.internalVector.getIndex().length) {
				this.internalVector = new SparseVector(indicesWithNonZeroEntry.size(), indicesWithNonZeroEntry.stream().mapToInt(i -> i).toArray(), nonZeroEntries.stream().mapToDouble(d -> d).toArray());
			}
			this.setUnchanged();
		}
		return this.internalVector.getIndex();
	}

	/**
	 * Changes the status of this vector to changed.
	 */
	private void setIsChanged() {
		this.isChanged = true;
	}

	/**
	 * Changes the status of this vector to unchanged.
	 */
	private void setUnchanged() {
		this.isChanged = false;
	}

	@Override
	public Vector kroneckerProduct(final double[] vectorAsArray) {
		return new SparseDoubleVector(this.kroneckerProductInternal(vectorAsArray));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((this.internalVector == null) ? 0 : this.internalVector.hashCode());
		result = prime * result + (this.isChanged ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		SparseDoubleVector other = (SparseDoubleVector) obj;
		// we cannot compare the internal vector
		if (this.isChanged != other.isChanged) {
			return false;
		}
		return Arrays.equals(this.asArray(), other.asArray());
	}



}
