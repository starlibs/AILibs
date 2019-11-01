package ai.libs.jaicore.math.linearalgebra;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.api4.java.common.math.IVector;
import org.junit.Test;

/**
 * This class tests every method which can be called on the {@link SparseDoubleVector}.
 *
 * @author Alexander Hetzer
 *
 */
public class SparseDoubleVectorTest {

	private static final double DOUBLE_COMPARISON_DELTA = 0.0001;

	/**
	 * Tests whether an array can be directly added to a vector.
	 */
	@Test
	public void testAddVectorFromArrayWithoutCopy() {
		double[] value1 = { 1, 2 };
		double[] value2 = { 2, 3 };
		double[] sum = { 3, 5 };
		IVector vector = new SparseDoubleVector(value1);
		vector.addVector(value2);
		assertEquals(vector, new SparseDoubleVector(sum));

	}

	/**
	 * Tests whether a vector can be directly added to another vector.
	 */
	@Test
	public void testAddVectorWithoutCopy() {
		double[] value1 = { 1, 2 };
		double[] value2 = { 2, 3 };
		double[] sum = { 3, 5 };
		IVector toAdd = new SparseDoubleVector(value1);
		IVector vector = new SparseDoubleVector(value2);
		vector.addVector(toAdd);
		assertEquals(vector, new SparseDoubleVector(sum));
	}

	/**
	 * Tests whether an array can be added to a vector without changing the callee.
	 */
	@Test
	public void testAddVectorFromArrayWithCopy() {
		double[] value1 = { 1, 2 };
		double[] value2 = { 2, 3 };
		double[] sum = { 3, 5 };
		IVector vector = new SparseDoubleVector(value1);
		IVector resultVector = vector.addVectorToCopy(value2);
		assertEquals(resultVector, new SparseDoubleVector(sum));
	}

	/**
	 * Tests whether a vector can be added to another vector without changing the callee.
	 */
	@Test
	public void testAddVectorWithCopy() {
		double[] value1 = { 1, 2 };
		double[] value2 = { 2, 3 };
		double[] sum = { 3, 5 };
		IVector toAdd = new SparseDoubleVector(value1);
		IVector vector = new SparseDoubleVector(value2);
		IVector resultVector = vector.addVectorToCopy(toAdd);
		assertEquals(resultVector, new SparseDoubleVector(sum));
	}

	/**
	 * Tests whether a constant can be added to a vector without changing the callee.
	 */
	@Test
	public void testAddConstantWithCopy() {
		double[] value1 = { 1, 2 };
		double[] value2 = { 2, 3 };
		double constantToAdd = 1;
		IVector vector = new SparseDoubleVector(value1);
		IVector resultVector = vector.addConstantToCopy(constantToAdd);
		assertEquals(resultVector, new SparseDoubleVector(value2));
	}

	/**
	 * Tests whether a constant can be directly added to a vector.
	 */
	@Test
	public void testAddConstantWithoutCopy() {
		double[] value1 = { 1, 2 };
		double[] value2 = { 2, 3 };
		double constantToAdd = 1;
		IVector vector = new SparseDoubleVector(value1);
		vector.addConstant(constantToAdd);
		assertEquals(vector, new SparseDoubleVector(value2));
	}

	/**
	 * Tests whether a vector can be subtracted directly from another vector.
	 */
	@Test
	public void testSubtractVectorWithoutCopy() {
		double[] value1 = { 1, 2 };
		double[] value2 = { 2, 3 };
		double[] sum = { 3, 5 };
		IVector toSubtract = new SparseDoubleVector(value1);
		IVector vector = new SparseDoubleVector(sum);
		vector.subtractVector(toSubtract);
		assertEquals(vector, new SparseDoubleVector(value2));
	}

	/**
	 * Tests whether a vector can be subtracted from another vector without changing the callee.
	 */
	@Test
	public void testSubtractVectorWithCopy() {
		double[] data = { 8, 8 };
		double[] toSubtractData = { 4, 4 };
		IVector toSubtract = new SparseDoubleVector(toSubtractData);
		IVector vector = new SparseDoubleVector(data);
		IVector resultVector = vector.subtractVectorFromCopy(toSubtract);

		this.assertResultVectorIs4AndCalleeIsUnchanged(data, vector, resultVector);
	}

	/**
	 * Tests whether an array can be directly subtracted from another vector.
	 */
	@Test
	public void testSubtractArrayFromVectorWithoutCopy() {
		double[] data = { 8, 8 };
		double[] toSubtractData = { 4, 4 };
		IVector vector = new SparseDoubleVector(data);
		vector.subtractVector(toSubtractData);

		this.assertResultVectorIs4(vector);
	}

	/**
	 * Tests whether an array can be subtracted from another vector without changing the callee.
	 */
	@Test
	public void testSubtractArrayFromVectorWithCopy() {
		double[] data = { 8, 8 };
		double[] toSubtractData = { 4, 4 };
		IVector vector = new SparseDoubleVector(data);
		IVector resultVector = vector.subtractVectorFromCopy(toSubtractData);

		this.assertResultVectorIs4AndCalleeIsUnchanged(data, vector, resultVector);
	}

	/**
	 * Tests whether a constant can be directly subtracted from a vector.
	 */
	@Test
	public void testSubtractConstantWithoutCopy() {
		double[] data = { 8, 8 };
		double toSubtract = 4;
		IVector vector = new SparseDoubleVector(data);
		vector.subtractConstant(toSubtract);

		this.assertResultVectorIs4(vector);
	}

	/**
	 * Tests whether a constant can be subtracted from another vector without changing the callee.
	 */
	@Test
	public void testSubtractConstantWithCopy() {
		double[] data = { 8, 8 };
		double toSubtract = 4;
		IVector vector = new SparseDoubleVector(data);
		IVector resultVector = vector.subtractConstantFromCopy(toSubtract);

		this.assertResultVectorIs4AndCalleeIsUnchanged(data, vector, resultVector);
	}

	/**
	 * Tests whether a vector can be directly pairwise multiplied by another vector.
	 */
	@Test
	public void testMultiplyByVectorPairwiseWithoutCopy() {
		double[] data = { 2, 2 };
		IVector toMultiply = new SparseDoubleVector(data);
		IVector vector = new SparseDoubleVector(data);
		vector.multiplyByVectorPairwise(toMultiply);

		this.assertResultVectorIs4(vector);
	}

	/**
	 * Tests whether a vector can be pairwise multiplied by another vector without changing the callee.
	 */
	@Test
	public void testMultiplyByVectorPairwiseWithCopy() {
		double[] data = { 2, 2 };
		IVector toMultiply = new SparseDoubleVector(data);
		IVector vector = new SparseDoubleVector(data);
		IVector resultVector = vector.multiplyByVectorPairwiseToCopy(toMultiply);

		this.assertResultVectorIs4AndCalleeIsUnchanged(data, vector, resultVector);
	}

	/**
	 * Tests whether a vector can be directly pairwise multiplied by an array.
	 */
	@Test
	public void testMultiplyByVectorPairwiseFromArrayWithoutCopy() {
		double[] data = { 2, 2 };
		IVector vector = new SparseDoubleVector(data);
		vector.multiplyByVectorPairwise(data);

		this.assertResultVectorIs4(vector);
	}

	/**
	 * Tests whether a vector can be pairwise multiplied by an array without changing the callee.
	 */
	@Test
	public void testMultiplyByVectorPairwiseFromArrayWithCopy() {
		double[] data = { 2, 2 };
		IVector vector = new SparseDoubleVector(data);
		IVector resultVector = vector.multiplyByVectorPairwiseToCopy(data);

		this.assertResultVectorIs4AndCalleeIsUnchanged(data, vector, resultVector);
	}

	/**
	 * Tests whether a vector can be directly multiplied by a constant.
	 */
	@Test
	public void testMultiplyByConstantWithoutCopy() {
		double[] data = { 2, 2 };
		double toMultiply = 2;
		IVector vector = new SparseDoubleVector(data);
		vector.multiplyByConstant(toMultiply);

		this.assertResultVectorIs4(vector);
	}

	/**
	 * Tests whether a vector can be multiplied with a constant without changing the callee.
	 */
	@Test
	public void testMultiplyByConstantWithCopy() {
		double[] data = { 2, 2 };
		double toMultiply = 2;
		IVector vector = new SparseDoubleVector(data);
		IVector resultVector = vector.multiplyByConstantToCopy(toMultiply);

		this.assertResultVectorIs4AndCalleeIsUnchanged(data, vector, resultVector);
	}

	/**
	 * Tests whether a vector can be directly divided by another vector.
	 */
	@Test
	public void testDivideByVectorPairwiseWithoutCopy() {
		double[] toDivideData = { 2, 2 };
		IVector toDivide = new SparseDoubleVector(toDivideData);

		double[] data = { 8, 8 };
		IVector vector = new SparseDoubleVector(data);
		vector.divideByVectorPairwise(toDivide);

		this.assertResultVectorIs4(vector);
	}

	/**
	 * Tests whether a vector can be pairwise divided by a vector without changing the callee.
	 */
	@Test
	public void testDivideByVectorPairwiseWithCopy() {
		double[] toDivideData = { 2, 2 };
		IVector toDivide = new SparseDoubleVector(toDivideData);

		double[] data = { 8, 8 };
		IVector vector = new SparseDoubleVector(data);
		IVector resultVector = vector.divideByVectorPairwiseToCopy(toDivide);

		this.assertResultVectorIs4AndCalleeIsUnchanged(data, vector, resultVector);
	}

	/**
	 * Tests whther a vector can be directly pairwise divided from an array.
	 */
	@Test
	public void testDivideByVectorPairwiseFromArrayWithoutCopy() {
		double[] toDivideData = { 2, 2 };

		double[] data = { 8, 8 };
		IVector vector = new SparseDoubleVector(data);
		vector.divideByVectorPairwise(toDivideData);

		this.assertResultVectorIs4(vector);
	}

	/**
	 * Tests whether a vector can be pairwise divided from an array without changing the callee.
	 */
	@Test
	public void testDivideByVectorPairwiseFromArrayWithCopy() {
		double[] toDivideData = { 2, 2 };

		double[] data = { 8, 8 };
		IVector vector = new SparseDoubleVector(data);
		IVector resultVector = vector.divideByVectorPairwiseToCopy(toDivideData);

		this.assertResultVectorIs4AndCalleeIsUnchanged(data, vector, resultVector);
	}

	/**
	 * Tests whether a vector can be directly divided by a constant.
	 */
	@Test
	public void testDivideByConstantWithoutCopy() {
		double[] data = { 8, 8 };
		double toDivide = 2;
		IVector vector = new SparseDoubleVector(data);
		vector.divideByConstant(toDivide);

		this.assertResultVectorIs4(vector);
	}

	/**
	 * Tests whether a vector can be divided by a constant without changing the callee.
	 */
	@Test
	public void testDivideByConstantWithCopy() {
		double[] data = { 8, 8 };
		double toDivide = 2;
		IVector vector = new SparseDoubleVector(data);
		IVector resultVector = vector.divideByConstantToCopy(toDivide);

		this.assertResultVectorIs4AndCalleeIsUnchanged(data, vector, resultVector);
	}

	/**
	 * Tests whether getting a specific dimension of the vector works.
	 */
	@Test
	public void testGetValue() {
		double expectedValue = 2;
		double[] data = { 2, 2 };
		IVector vector = new SparseDoubleVector(data);
		assertEquals(expectedValue, vector.getValue(0), DOUBLE_COMPARISON_DELTA);
	}

	/**
	 * Tests whether setting the value of a specific dimension of a vector works.
	 */
	@Test
	public void testSetValue() {
		double[] expectedVectorData = { 4, 2 };
		IVector expectedVector = new SparseDoubleVector(expectedVectorData);
		double[] data = { 2, 2 };
		IVector vector = new SparseDoubleVector(data);
		vector.setValue(0, 4);
		assertEquals(expectedVector, vector);
	}

	/**
	 * Tests whether the dot product operation works.
	 */
	@Test
	public void testDotProduct() {
		double[] vectorData1 = { 4, 1 };
		IVector vector1 = new SparseDoubleVector(vectorData1);

		double[] vectorData2 = { 2, 2 };
		IVector vector2 = new SparseDoubleVector(vectorData2);

		assertEquals(10, vector1.dotProduct(vector2), DOUBLE_COMPARISON_DELTA);
	}

	/**
	 * Tests whether the sum operation works.
	 */
	@Test
	public void testSum() {
		double[] data = { 4, 2 };
		IVector vector = new SparseDoubleVector(data);

		assertEquals(6, vector.sum(), DOUBLE_COMPARISON_DELTA);
	}

	/**
	 * Tests whether computing the mean of the dimensions of a vector works.
	 */
	@Test
	public void testMean() {
		double[] data = { 4, 2 };
		IVector vector = new SparseDoubleVector(data);

		assertEquals(3, vector.mean(), DOUBLE_COMPARISON_DELTA);
	}

	/**
	 * Tests whether computing the mean of the dimensions of a vector works.
	 */
	@Test
	public void testStandardDeviation() {
		double[] data = { 4, 2, 1, 3 };
		IVector vector = new DenseDoubleVector(data);
		assertEquals(1.118, vector.standardDeviation(), DOUBLE_COMPARISON_DELTA);
	}

	/**
	 * Tests whether duplicating a vector works.
	 */
	@Test
	public void testDuplicate() {
		double[] data = { 4, 2 };
		IVector vector = new SparseDoubleVector(data);

		IVector vectorCopy = vector.duplicate();
		vectorCopy.addConstant(2);

		assertNotEquals(vector, vectorCopy);
	}

	/**
	 * Tests whether zeroing all dimensions of a vector works.
	 */
	@Test
	public void testZeroAllDimensions() {
		double[] data = { 4, 2 };
		IVector vector = new SparseDoubleVector(data);
		vector.zeroAllDimensions();

		IVector expectedVector = new SparseDoubleVector(2);

		assertEquals(expectedVector, vector);
	}

	/**
	 * Tests whether computing the euclidean norm of a vector works.
	 */
	@Test
	public void testEuclideanNorm() {
		double[] data = { 4, 2 };
		IVector vector = new SparseDoubleVector(data);

		double expectedEuclideanNorm = Math.sqrt(4 * 4 + 2 * 2);

		assertEquals(expectedEuclideanNorm, vector.euclideanNorm(), DOUBLE_COMPARISON_DELTA);
	}

	/**
	 * Tests whether obtaining the values of a vector as an array works.
	 */
	@Test
	public void testAsArray() {
		double[] data = { 4, 2 };
		IVector vector = new SparseDoubleVector(data);

		assertArrayEquals(data, vector.asArray(), DOUBLE_COMPARISON_DELTA);
	}

	/**
	 * Asserts that the given vector is a (4,4) vector.
	 *
	 * @param resultVector
	 *            The vector to be tested.
	 */
	private void assertResultVectorIs4(final IVector resultVector) {
		double[] expectedVectorValues = { 4, 4 };
		IVector expectedVector = new SparseDoubleVector(expectedVectorValues);
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
	private void assertResultVectorIs4AndCalleeIsUnchanged(final double[] expectedCalleeData, final IVector callee, final IVector resultVector) {
		this.assertResultVectorIs4(resultVector);
		// assert that the original vector did not change
		assertArrayEquals(expectedCalleeData, callee.asArray(), DOUBLE_COMPARISON_DELTA);
	}

}
