package ai.libs.jaicore.basic;

import java.util.Arrays;
import java.util.Collection;

public class ArrayUtil {

	private ArrayUtil() {
		// Prevent instantiation of this util class.
	}

	private static void columnSanityCheck(final int arrayLength, final Collection<Integer> columnIndices) {
		if (columnIndices.stream().filter(x -> x >= arrayLength || x < 0).findAny().isPresent()) {
			throw new IllegalArgumentException("Cannot exclude non existing columns (" + columnIndices + "), array length: " + arrayLength);
		}
	}

	/**
	 * Copies an array of type <T> without copying the columns in columnsToExclude.
	 *
	 * @param <T> The data type of objects contained in the array.
	 * @param array The array to copy excluding the given columns.
	 * @param columnsToExclude The columns to exclude when copying.
	 * @param clazz The class object for the type T.
	 * @return The copy of the original array without the excluded values.
	 */
	public static <T> T[] copyArrayExlcuding(final T[] array, final Collection<Integer> columnsToExclude) {
		columnSanityCheck(array.length, columnsToExclude);

		T[] arrayCopy = Arrays.copyOf(array, array.length - columnsToExclude.size());
		int pointer = 0;
		for (int i = 0; i < array.length; i++) {
			if (columnsToExclude.contains(i)) {
				continue;
			}
			arrayCopy[pointer++] = array[i];
		}
		return arrayCopy;
	}

	/**
	 * Copies an array of type <T> retaining the columns in columnsToRetain.
	 *
	 * @param <T> The data type of objects contained in the array.
	 * @param array The array to copy retaining the given columns.
	 * @param columnsToExclude The columns to retain when copying.
	 * @param clazz The class object for the type T.
	 * @return The copy of the original array retaining the given column values only.
	 */
	public static <T> T[] copyArrayRetaining(final T[] array, final Collection<Integer> columnsToRetain) {
		columnSanityCheck(array.length, columnsToRetain);
		T[] arrayCopy = Arrays.copyOf(array, columnsToRetain.size());
		int pointer = 0;
		for (int i = 0; i < array.length; i++) {
			if (!columnsToRetain.contains(i)) {
				continue;
			}
			arrayCopy[pointer++] = array[i];
		}
		return arrayCopy;
	}

	/**
	 * Transposes a matrix A and returns A^T.
	 * @param matrix The given matrix A.
	 * @return The transposed matrix A^T originating from A.
	 */
	public static double[][] transposeDoubleMatrix(final double[][] matrix) {
		double[][] transposed = new double[matrix[0].length][matrix.length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				transposed[j][i] = matrix[i][j];
			}
		}
		return transposed;
	}

	/**
	 * Transposes a matrix A and returns A^T.
	 * @param matrix The given matrix A.
	 * @return The transposed matrix A^T originating from A.
	 */
	public static int[][] transposeIntegerMatrix(final int[][] matrix) {
		int[][] transposed = new int[matrix[0].length][matrix.length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				transposed[j][i] = matrix[i][j];
			}
		}
		return transposed;
	}

	private static String cleanArrayString(final String arrayString) {
		String cleanArrayString = arrayString.trim();
		if (cleanArrayString.startsWith("[") && cleanArrayString.endsWith("]")) {
			cleanArrayString = cleanArrayString.substring(1, cleanArrayString.length() - 1);
		}
		return cleanArrayString;
	}

	public static double[] parseStringToDoubleArray(final String arrayString) {
		return Arrays.stream(cleanArrayString(arrayString).split(",")).mapToDouble(Double::parseDouble).toArray();
	}

	public static int[] parseStringToIntArray(final String arrayString) {
		return Arrays.stream(cleanArrayString(arrayString).split(",")).mapToInt(Integer::parseInt).toArray();
	}

	public static String[] parseStringToStringArray(final String arrayString) {
		return (String[]) Arrays.stream(cleanArrayString(arrayString).split(",")).toArray();
	}

}
