package jaicore.ml.tsc.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.sets.SetUtil.Pair;
import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.exceptions.TimeSeriesLoadingException;

/**
 * Time series loader class which provides functionality to read datasets from
 * files storing into simplified, more efficient time series datasets.
 * 
 * @author Julian Lienen
 *
 */
public class SimplifiedTimeSeriesLoader {

	/**
	 * Log4j logger.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(SimplifiedTimeSeriesLoader.class);

	/**
	 * Default charset used when extracting from files.
	 */
	public static final String DEFAULT_CHARSET = "UTF-8";

	/**
	 * Prefix indicating an attribute declaration row in arff files.
	 */
	private static final String ARFF_ATTRIBUTE_PREFIX = "@attribute";
	/**
	 * Delimiter in value enumerations in arff files.
	 */
	private static final String ARFF_VALUE_DELIMITER = ",";
	/**
	 * Flag indicating the start of the data block in arff files.
	 */
	private static final String ARFF_DATA_FLAG = "@data";

	/**
	 * Epsilon used for target array comparisons (e. g. used when reading
	 * multivariate time series which must share their targets among all the
	 * series).
	 */
	private static final double TARGET_EQUALS_EPS = 0.01;

	// TODO: Add meta data support
	/**
	 * Loads a univariate time series dataset from the given arff file. Assumes the
	 * class attribute to be the last among the declared attributes in the file.
	 * 
	 * @param arffFile
	 *            The arff file which is read
	 * @return Returns a pair consisting of an univariate TimeSeriesDataset object
	 *         and a list of String objects containing the class values.
	 * @throws TimeSeriesLoadingException
	 *             Throws an exception when the TimeSeriesDataset could not be
	 *             created from the given file.
	 */
	@SuppressWarnings("unchecked")
	public static Pair<TimeSeriesDataset, ClassMapper> loadArff(final File arffFile) throws TimeSeriesLoadingException {
		if (arffFile == null)
			throw new IllegalArgumentException("Parameter 'arffFile' must not be null!");

		Object[] tsTargetClassNames = loadTimeSeriesWithTargetFromArffFile(arffFile);

		ArrayList<double[][]> matrices = new ArrayList<>();
		matrices.add((double[][]) tsTargetClassNames[0]);

		ClassMapper cm = null;
		if (tsTargetClassNames[2] != null) {
			cm = new ClassMapper((List<String>) tsTargetClassNames[2]);
		}

		return new Pair<TimeSeriesDataset, ClassMapper>(
				new TimeSeriesDataset(matrices, new ArrayList<double[][]>(), (int[]) tsTargetClassNames[1]), cm);
	}

	/**
	 * Loads a multivariate time series dataset from multiple arff files (each for
	 * one series). The arff files must share the same targets among all series.
	 * Assumes the class attribute to be the last among the declared attributes in
	 * the file.
	 * 
	 * @param arffFiles
	 *            A sequence of arff files each containing one time series per
	 *            instance
	 * @return Returns a multivariate TimeSeriesDataset object
	 * @throws TimeSeriesLoadingException
	 *             Throws an exception when the TimeSeriesDataset could not be
	 *             created from the given files.
	 */
	@SuppressWarnings("unchecked")
	public static Pair<TimeSeriesDataset, ClassMapper> loadArffs(final File... arffFiles)
			throws TimeSeriesLoadingException {
		if (arffFiles == null)
			throw new IllegalArgumentException("Parameter 'arffFiles' must not be null!");

		final List<double[][]> matrices = new ArrayList<>();
		int[] target = null;
		List<String> classNames = null;

		for (final File arffFile : arffFiles) {
			// Pair<INDArray, INDArray> tsWithTarget =
			// loadTimeSeriesWithTargetFromArffFile(arffFile);
			Object[] tsTargetClassNames = loadTimeSeriesWithTargetFromArffFile(arffFile);

			if (classNames == null && tsTargetClassNames[2] != null)
				classNames = (List<String>) tsTargetClassNames[2];
			else {
				// Check whether the same class names are used among all of the time series
				List<String> furtherClassNames = (List<String>) tsTargetClassNames[2];
				if ((classNames != null && furtherClassNames == null)
						|| (furtherClassNames != null && !furtherClassNames.equals(classNames)))
					throw new TimeSeriesLoadingException(
							"Could not load multivariate time series with different targets. Target values have to be stored in each "
									+ "time series arff file and must be equal!");
			}

			if (target == null)
				target = (int[]) tsTargetClassNames[1];
			else {
				// Check whether the same targets are used among all of the time series
				int[] furtherTarget = (int[]) tsTargetClassNames[1];
				if (furtherTarget == null || target.length != furtherTarget.length
						|| !Arrays.equals(target, furtherTarget)) {
					throw new TimeSeriesLoadingException(
							"Could not load multivariate time series with different targets. Target values have to be stored in each "
									+ "time series arff file and must be equal!");
				}
			}

			// Check for same instance length
			if (matrices.size() != 0 && ((double[][]) tsTargetClassNames[0]).length != matrices.get(0).length)
				throw new TimeSeriesLoadingException(
						"All time series must have the same first dimensionality (number of instances).");

			matrices.add((double[][]) tsTargetClassNames[0]);
		}
		ClassMapper cm = null;
		if (classNames != null)
			cm = new ClassMapper(classNames);

		return new Pair<TimeSeriesDataset, ClassMapper>(
				new TimeSeriesDataset(matrices, new ArrayList<double[][]>(), target), cm);
	}

	/**
	 * Extracting the time series and target matrices from a given arff file.
	 * Assumes the class attribute to be the last among the declared attributes in
	 * the file.
	 * 
	 * @param arffFile
	 *            The arff file to be parsed
	 * @return Returns an object consisting of three elements: 1. The time series
	 *         value matrix (double[][]), 2. the target value matrix (int[]) and 3.
	 *         a list of the class value strings (List<String>)
	 * @throws TimeSeriesLoadingException
	 *             Throws an exception when the matrices could not be extracted from
	 *             the given arff file
	 */
	private static Object[] loadTimeSeriesWithTargetFromArffFile(final File arffFile)
			throws TimeSeriesLoadingException {
		double[][] matrix = null;
		int[] targetMatrix = null;

		int numEmptyDataRows = 0;

		List<String> targetValues = null;
		boolean stringAttributes = false;

		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(new FileInputStream(arffFile), DEFAULT_CHARSET))) {

			int attributeCount = 0;

			int lineCounter = 0;
			int numInstances = 0;

			final int fileLinesCount = countFileLines(arffFile);

			boolean targetSet = false;
			boolean readData = false;
			String line;
			String lastLine = "";

			while ((line = br.readLine()) != null) {
				if (!readData) {
					lineCounter++;

					// Set target values
					if (!targetSet && line.equals("") && lastLine.startsWith(ARFF_ATTRIBUTE_PREFIX)) {
						String targetString = lastLine.substring(lastLine.indexOf("{") + 1, lastLine.length() - 1);
						targetValues = Arrays.asList(targetString.split(ARFF_VALUE_DELIMITER));
						for (String targetVal : targetValues) {
							try {
								Double.parseDouble(targetVal);
							} catch (NumberFormatException e) {
								LOGGER.info("Found String attributes in parsed dataset.");
								stringAttributes = true;
								break;
							}
						}

						targetSet = true;
					}

					// Count attributes
					if (line.startsWith(ARFF_ATTRIBUTE_PREFIX))
						attributeCount++;

					if (line.startsWith(ARFF_DATA_FLAG)) {
						readData = true;
						numInstances = fileLinesCount - lineCounter + 1;
						matrix = new double[numInstances][targetSet ? attributeCount - 1 : attributeCount];
						targetMatrix = new int[numInstances];
						lineCounter = 0;

						if (!targetSet)
							LOGGER.warn("No target has been set before reading data.");
					}
				} else {
					if (!line.equals("")) {
						// Read the data
						String[] values = line.split(ARFF_VALUE_DELIMITER);
						double[] dValues = new double[targetSet ? values.length - 1 : values.length];
						for (int i = 0; i < values.length - 1; i++) {
							dValues[i] = Double.parseDouble(values[i]);
						}
						matrix[lineCounter] = dValues;

						if (targetSet)
							targetMatrix[lineCounter] = targetValues.indexOf(values[values.length - 1]);
					}

					lineCounter++;
				}

				lastLine = line;
			}

			// Update empty data rows
			numEmptyDataRows = numInstances - lineCounter;

		} catch (

		UnsupportedEncodingException e) {
			throw new TimeSeriesLoadingException("Could not load time series dataset due to unsupported encoding.", e);
		} catch (FileNotFoundException e) {
			throw new TimeSeriesLoadingException(
					String.format("Could not locate time series dataset file '%s'.", arffFile.getPath()), e);
		} catch (IOException e) {
			throw new TimeSeriesLoadingException("Could not load time series dataset due to IOException.", e);
		}

		// Due to efficiency reasons, the matrices are narrowed afterwards to eliminate
		// empty data rows
		if (numEmptyDataRows > 0) {
			int endIndex = matrix.length - numEmptyDataRows;
			matrix = getInterval(matrix, 0, endIndex);
			targetMatrix = getInterval(targetMatrix, 0, endIndex);
		}

		Object[] result = new Object[3];
		result[0] = matrix;
		result[1] = targetMatrix;
		result[2] = stringAttributes ? targetValues : null;
		return result;
	}

	/**
	 * Function returning a submatrix of the given <code>matrix</code>. The
	 * submatrix is specified by the indices <code>begin</code> and
	 * </code>end</code> (exclusive). Only the rows within this interval are copied
	 * into the result matrix.
	 * 
	 * @param matrix
	 *            The matrix from which the submatrix is extracted
	 * @param begin
	 *            Begin index of the rows to be extracted
	 * @param end
	 *            Exclusive end index of the rows to be extracted
	 * @return Returns the specified submatrix
	 */
	private static double[][] getInterval(final double[][] matrix, final int begin, final int end) {
		if (begin < 0 || begin > matrix.length - 1)
			throw new IllegalArgumentException("The begin index must be valid!");
		if (end < 1 || end > matrix.length)
			throw new IllegalArgumentException("The end index must be valid!");

		final double[][] result = new double[end - begin][];
		for (int i = 0; i < end - begin; i++) {
			result[i] = matrix[i + begin];
		}
		return result;
	}

	/**
	 * Function returning an interval as subarray of the given <code>array</code>.
	 * The interval is specified by the indices <code>begin</code> and
	 * </code>end</code> (exclusive).
	 * 
	 * @param array
	 *            The array from which the interval is extracted
	 * @param begin
	 *            Begin index of the interval
	 * @param end
	 *            Exclusive end index of the interval
	 * @return Returns the specified interval as a subarray
	 */
	private static int[] getInterval(final int[] array, final int begin, final int end) {
		if (begin < 0 || begin > array.length - 1)
			throw new IllegalArgumentException("The begin index must be valid!");
		if (end < 1 || end > array.length)
			throw new IllegalArgumentException("The end index must be valid!");

		final int[] result = new int[end - begin];
		for (int i = 0; i < end - begin; i++) {
			result[i] = array[i + begin];
		}
		return result;
	}

	/**
	 * Counts the lines of the given File object in a very efficient way (thanks to
	 * https://stackoverflow.com/a/453067).
	 * 
	 * @param filename
	 *            File which lines of code are counted
	 * @return Returns the number of file lines
	 * @throws IOException
	 *             Throws exception when the given file could not be read
	 */
	public static int countFileLines(File file) throws IOException {
		InputStream is = new BufferedInputStream(new FileInputStream(file));
		try {
			byte[] c = new byte[1024];

			int readChars = is.read(c);
			if (readChars == -1) {
				// bail out if nothing to read
				return 0;
			}

			// make it easy for the optimizer to tune this loop
			int count = 0;
			while (readChars == 1024) {
				for (int i = 0; i < 1024;) {
					if (c[i++] == '\n') {
						++count;
					}
				}
				readChars = is.read(c);
			}

			// count remaining characters
			while (readChars != -1) {
				for (int i = 0; i < readChars; ++i) {
					if (c[i] == '\n') {
						++count;
					}
				}
				readChars = is.read(c);
			}

			return count == 0 ? 1 : count;
		} finally {
			is.close();
		}
	}
}
