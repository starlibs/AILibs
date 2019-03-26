package de.upb.isys.linearalgebra;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

/**
 * This class tests every method which can be called on the {@link DenseDoubleVector}.
 * 
 * @author Alexander Hetzer
 *
 */
public class DenseDoubleVectorTest {

	private static final double DOUBLE_COMPARISON_DELTA = 0.0001;

	/**
	 * Tests whether an array can be directly added to a vector.
	 */
	@Test
	public void testAddVectorFromArrayWithoutCopy() {
		double[] data = { 2, 2 };
		Vector vector = new DenseDoubleVector(data);
		vector.addVector(data);

		assertResultVectorIs4(vector);
	}

	/**
	 * Tests whether a vector can be directly added to another vector.
	 */
	@Test
	public void testAddVectorWithoutCopy() {
		double[] data = { 2, 2 };
		Vector toAdd = new DenseDoubleVector(data);
		Vector vector = new DenseDoubleVector(data);
		vector.addVector(toAdd);

		assertResultVectorIs4(vector);
	}

	/**
	 * Tests whether an array can be added to a vector without changing the callee.
	 */
	@Test
	public void testAddVectorFromArrayWithCopy() {
		double[] data = { 2, 2 };
		Vector vector = new DenseDoubleVector(data);
		Vector resultVector = vector.addVectorToCopy(data);

		assertResultVectorIs4AndCalleeIsUnchanged(data, vector, resultVector);
	}

	/**
	 * Tests whether a vector can be added to another vector without changing the callee.
	 */
	@Test
	public void testAddVectorWithCopy() {
		double[] data = { 2, 2 };
		Vector toAdd = new DenseDoubleVector(data);
		Vector vector = new DenseDoubleVector(data);
		Vector resultVector = vector.addVectorToCopy(toAdd);

		assertResultVectorIs4AndCalleeIsUnchanged(data, vector, resultVector);
	}

	/**
	 * Tests whether a constant can be added to a vector without changing the callee.
	 */
	@Test
	public void testAddConstantWithCopy() {
		double[] data = { 2, 2 };
		double toAdd = 2;
		Vector vector = new DenseDoubleVector(data);
		Vector resultVector = vector.addConstantToCopy(toAdd);

		assertResultVectorIs4AndCalleeIsUnchanged(data, vector, resultVector);
	}

	/**
	 * Tests whether a constant can be directly added to a vector.
	 */
	@Test
	public void testAddConstantWithoutCopy() {
		double[] data = { 2, 2 };
		double toAdd = 2;
		Vector vector = new DenseDoubleVector(data);
		vector.addConstant(toAdd);

		assertResultVectorIs4(vector);
	}

	/**
	 * Tests whether a vector can be subtracted directly from another vector.
	 */
	@Test
	public void testSubtractVectorWithoutCopy() {
		double[] data = { 8, 8 };
		double[] toSubtractData = { 4, 4 };
		Vector toSubtract = new DenseDoubleVector(toSubtractData);
		Vector vector = new DenseDoubleVector(data);
		vector.subtractVector(toSubtract);

		assertResultVectorIs4(vector);
	}

	/**
	 * Tests whether a vector can be subtracted from another vector without changing the callee.
	 */
	@Test
	public void testSubtractVectorWithCopy() {
		double[] data = { 8, 8 };
		double[] toSubtractData = { 4, 4 };
		Vector toSubtract = new DenseDoubleVector(toSubtractData);
		Vector vector = new DenseDoubleVector(data);
		Vector resultVector = vector.subtractVectorFromCopy(toSubtract);

		assertResultVectorIs4AndCalleeIsUnchanged(data, vector, resultVector);
	}

	/**
	 * Tests whether an array can be directly subtracted from another vector.
	 */
	@Test
	public void testSubtractArrayFromVectorWithoutCopy() {
		double[] data = { 8, 8 };
		double[] toSubtractData = { 4, 4 };
		Vector vector = new DenseDoubleVector(data);
		vector.subtractVector(toSubtractData);

		assertResultVectorIs4(vector);
	}

	/**
	 * Tests whether an array can be subtracted from another vector without changing the callee.
	 */
	@Test
	public void testSubtractArrayFromVectorWithCopy() {
		double[] data = { 8, 8 };
		double[] toSubtractData = { 4, 4 };
		Vector vector = new DenseDoubleVector(data);
		Vector resultVector = vector.subtractVectorFromCopy(toSubtractData);

		assertResultVectorIs4AndCalleeIsUnchanged(data, vector, resultVector);
	}

	/**
	 * Tests whether a constant can be directly subtracted from a vector.
	 */
	@Test
	public void testSubtractConstantWithoutCopy() {
		double[] data = { 8, 8 };
		double toSubtract = 4;
		Vector vector = new DenseDoubleVector(data);
		vector.subtractConstant(toSubtract);

		assertResultVectorIs4(vector);
	}

	/**
	 * Tests whether a constant can be subtracted from another vector without changing the callee.
	 */
	@Test
	public void testSubtractConstantWithCopy() {
		double[] data = { 8, 8 };
		double toSubtract = 4;
		Vector vector = new DenseDoubleVector(data);
		Vector resultVector = vector.subtractConstantFromCopy(toSubtract);

		assertResultVectorIs4AndCalleeIsUnchanged(data, vector, resultVector);
	}

	/**
	 * Tests whether a vector can be directly pairwise multiplied by another vector.
	 */
	@Test
	public void testMultiplyByVectorPairwiseWithoutCopy() {
		double[] data = { 2, 2 };
		Vector toMultiply = new DenseDoubleVector(data);
		Vector vector = new DenseDoubleVector(data);
		vector.multiplyByVectorPairwise(toMultiply);

		assertResultVectorIs4(vector);
	}

	/**
	 * Tests whether a vector can be pairwise multiplied by another vector without changing the callee.
	 */
	@Test
	public void testMultiplyByVectorPairwiseWithCopy() {
		double[] data = { 2, 2 };
		Vector toMultiply = new DenseDoubleVector(data);
		Vector vector = new DenseDoubleVector(data);
		Vector resultVector = vector.multiplyByVectorPairwiseToCopy(toMultiply);

		assertResultVectorIs4AndCalleeIsUnchanged(data, vector, resultVector);
	}

	/**
	 * Tests whether a vector can be directly pairwise multiplied by an array.
	 */
	@Test
	public void testMultiplyByVectorPairwiseFromArrayWithoutCopy() {
		double[] data = { 2, 2 };
		Vector vector = new DenseDoubleVector(data);
		vector.multiplyByVectorPairwise(data);

		assertResultVectorIs4(vector);
	}

	/**
	 * Tests whether a vector can be pairwise multiplied by an array without changing the callee.
	 */
	@Test
	public void testMultiplyByVectorPairwiseFromArrayWithCopy() {
		double[] data = { 2, 2 };
		Vector vector = new DenseDoubleVector(data);
		Vector resultVector = vector.multiplyByVectorPairwiseToCopy(data);

		assertResultVectorIs4AndCalleeIsUnchanged(data, vector, resultVector);
	}

	/**
	 * Tests whether a vector can be directly multiplied by a constant.
	 */
	@Test
	public void testMultiplyByConstantWithoutCopy() {
		double[] data = { 2, 2 };
		double toMultiply = 2;
		Vector vector = new DenseDoubleVector(data);
		vector.multiplyByConstant(toMultiply);

		assertResultVectorIs4(vector);
	}

	/**
	 * Tests whether a vector can be multiplied with a constant without changing the callee.
	 */
	@Test
	public void testMultiplyByConstantWithCopy() {
		double[] data = { 2, 2 };
		double toMultiply = 2;
		Vector vector = new DenseDoubleVector(data);
		Vector resultVector = vector.multiplyByConstantToCopy(toMultiply);

		assertResultVectorIs4AndCalleeIsUnchanged(data, vector, resultVector);
	}

	/**
	 * Tests whether a vector can be directly divided by another vector.
	 */
	@Test
	public void testDivideByVectorPairwiseWithoutCopy() {
		double[] toDivideData = { 2, 2 };
		Vector toDivide = new DenseDoubleVector(toDivideData);

		double[] data = { 8, 8 };
		Vector vector = new DenseDoubleVector(data);
		vector.divideByVectorPairwise(toDivide);

		assertResultVectorIs4(vector);
	}

	/**
	 * Tests whether a vector can be pairwise divided by a vector without changing the callee.
	 */
	@Test
	public void testDivideByVectorPairwiseWithCopy() {
		double[] toDivideData = { 2, 2 };
		Vector toDivide = new DenseDoubleVector(toDivideData);

		double[] data = { 8, 8 };
		Vector vector = new DenseDoubleVector(data);
		Vector resultVector = vector.divideByVectorPairwiseToCopy(toDivide);

		assertResultVectorIs4AndCalleeIsUnchanged(data, vector, resultVector);
	}

	/**
	 * Tests whether a vector can be directly pairwise divided from an array.
	 */
	@Test
	public void testDivideByVectorPairwiseFromArrayWithoutCopy() {
		double[] toDivideData = { 2, 2 };

		double[] data = { 8, 8 };
		Vector vector = new DenseDoubleVector(data);
		vector.divideByVectorPairwise(toDivideData);

		assertResultVectorIs4(vector);
	}

	/**
	 * Tests whether a vector can be pairwise divided from an array without changing the callee.
	 */
	@Test
	public void testDivideByVectorPairwiseFromArrayWithCopy() {
		double[] toDivideData = { 2, 2 };

		double[] data = { 8, 8 };
		Vector vector = new DenseDoubleVector(data);
		Vector resultVector = vector.divideByVectorPairwiseToCopy(toDivideData);

		assertResultVectorIs4AndCalleeIsUnchanged(data, vector, resultVector);
	}

	/**
	 * Tests whether a vector can be directly divided by a constant.
	 */
	@Test
	public void testDivideByConstantWithoutCopy() {
		double[] data = { 8, 8 };
		double toDivide = 2;
		Vector vector = new DenseDoubleVector(data);
		vector.divideByConstant(toDivide);

		assertResultVectorIs4(vector);
	}

	/**
	 * Tests whether a vector can be divided by a constant without changing the callee.
	 */
	@Test
	public void testDivideByConstantWithCopy() {
		double[] data = { 8, 8 };
		double toDivide = 2;
		Vector vector = new DenseDoubleVector(data);
		Vector resultVector = vector.divideByConstantToCopy(toDivide);

		assertResultVectorIs4AndCalleeIsUnchanged(data, vector, resultVector);
	}

	/**
	 * Tests whether getting a specific dimension of the vector works.
	 */
	@Test
	public void testGetValue() {
		double expectedValue = 2;
		double[] data = { 2, 2 };
		Vector vector = new DenseDoubleVector(data);
		assertEquals(expectedValue, vector.getValue(0), DOUBLE_COMPARISON_DELTA);
	}

	/**
	 * Tests whether setting the value of a specific dimension of a vector works.
	 */
	@Test
	public void testSetValue() {
		double[] expectedVectorData = { 4, 2 };
		Vector expectedVector = new DenseDoubleVector(expectedVectorData);
		double[] data = { 2, 2 };
		Vector vector = new DenseDoubleVector(data);
		vector.setValue(0, 4);
		assertEquals(expectedVector, vector);
	}

	/**
	 * Tests whether the dot product operation works.
	 */
	@Test
	public void testDotProduct() {
		double[] vectorData1 = { 4, 1 };
		Vector vector1 = new DenseDoubleVector(vectorData1);

		double[] vectorData2 = { 2, 2 };
		Vector vector2 = new DenseDoubleVector(vectorData2);

		assertEquals(10, vector1.dotProduct(vector2), DOUBLE_COMPARISON_DELTA);
	}

	/**
	 * Tests whether the sum operation works.
	 */
	@Test
	public void testSum() {
		double[] data = { 4, 2 };
		Vector vector = new DenseDoubleVector(data);

		assertEquals(6, vector.sum(), DOUBLE_COMPARISON_DELTA);
	}

	/**
	 * Tests whether computing the mean of the dimensions of a vector works.
	 */
	@Test
	public void testMean() {
		double[] data = { 4, 2 };
		Vector vector = new DenseDoubleVector(data);

		assertEquals(3, vector.mean(), DOUBLE_COMPARISON_DELTA);
	}

	/**
	 * Tests whether computing the mean of the dimensions of a vector works.
	 */
	@Test
	public void testStandardDeviation() {
		double[] data = { 4, 2, 1, 3 };
		Vector vector = new DenseDoubleVector(data);
		assertEquals(1.118, vector.standardDeviation(), DOUBLE_COMPARISON_DELTA);
	}

	/**
	 * Tests whether duplicating a vector works.
	 */
	@Test
	public void testDuplicate() {
		double[] data = { 4, 2 };
		Vector vector = new DenseDoubleVector(data);

		Vector vectorCopy = vector.duplicate();
		vectorCopy.addConstant(2);

		assertNotEquals(vector, vectorCopy);
	}

	/**
	 * Tests whether zeroing all dimensions of a vector works.
	 */
	@Test
	public void testZeroAllDimensions() {
		double[] data = { 4, 2 };
		Vector vector = new DenseDoubleVector(data);
		vector.zeroAllDimensions();

		Vector expectedVector = new DenseDoubleVector(2);

		assertEquals(expectedVector, vector);
	}

	/**
	 * Tests whether computing the euclidean norm of a vector works.
	 */
	@Test
	public void testEuclideanNorm() {
		double[] data = { 4, 2 };
		Vector vector = new DenseDoubleVector(data);

		double expectedEuclideanNorm = Math.sqrt(4 * 4 + 2 * 2);

		assertEquals(expectedEuclideanNorm, vector.euclideanNorm(), DOUBLE_COMPARISON_DELTA);
	}

	/**
	 * Tests whether obtaining the values of a vector as an array works.
	 */
	@Test
	public void testAsArray() {
		double[] data = { 4, 2 };
		Vector vector = new DenseDoubleVector(data);

		assertArrayEquals(data, vector.asArray(), DOUBLE_COMPARISON_DELTA);
	}

	/**
	 * Asserts that the given vector is a (4,4) vector.
	 * 
	 * @param resultVector
	 *            The vector to be tested.
	 */
	private void assertResultVectorIs4(Vector resultVector) {
		double[] expectedVectorValues = { 4, 4 };
		Vector expectedVector = new DenseDoubleVector(expectedVectorValues);
		// assert that the operation yields the correct result
		assertEquals(expectedVector, resultVector);
	}

	/**
	 * Asserts that the given vector is a (4,4) vector and that the given callee did not change.
	 * 
	 * @param expectedCalleeData
	 *            The expected callee data.
	 * @param callee
	 *            The callee to check for a change.
	 * @param resultVector
	 *            The vector to be checked to be (4,4).
	 */
	private void assertResultVectorIs4AndCalleeIsUnchanged(double[] expectedCalleeData, Vector callee, Vector resultVector) {
		assertResultVectorIs4(resultVector);
		// assert that the original vector did not change
		assertArrayEquals(expectedCalleeData, callee.asArray(), DOUBLE_COMPARISON_DELTA);
	}

}
