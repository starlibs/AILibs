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

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.ml.core.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.exceptions.TimeSeriesLoadingException;

/**
 * Time series loader class which provides functionality to read datasets from
 * files.
 * 
 * @author Julian Lienen
 *
 */
// TODO: Remove me if TimeSeriesDataset using INDArray is not used anymore
public class TimeSeriesLoader {

	/**
	 * Log4j logger.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(TimeSeriesLoader.class);

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
	 * Epsilon used for INDArray target comparisons (e. g. used when reading
	 * multivariate time series which must share their targets among all the
	 * series).
	 */
	private static final double IND_TARGET_EQUALS_EPS = 0.01;

	// TODO: Add meta data support
	/**
	 * Loads a univariate time series dataset from the given arff file. Assumes the
	 * class attribute to be the last among the declared attributes in the file.
	 * 
	 * @param arffFile
	 *            The arff file which is read
	 * @return Returns an univariate TimeSeriesDataset object
	 * @throws TimeSeriesLoadingException
	 *             Throws an exception when the TimeSeriesDataset could not be
	 *             created from the given file.
	 */
	@SuppressWarnings("unchecked")
	public static TimeSeriesDataset loadArff(final File arffFile) throws TimeSeriesLoadingException {
		if (arffFile == null)
			throw new IllegalArgumentException("Parameter 'arffFile' must not be null!");

		Object[] tsTargetClassNames = loadTimeSeriesWithTargetFromArffFile(arffFile);

		return new TimeSeriesDataset(Arrays.asList((INDArray) tsTargetClassNames[0]), new ArrayList<INDArray>(),
				(INDArray) tsTargetClassNames[1], (List<String>) tsTargetClassNames[2]);
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
	public static TimeSeriesDataset loadArffs(final File... arffFiles) throws TimeSeriesLoadingException {
		if (arffFiles == null)
			throw new IllegalArgumentException("Parameter 'arffFiles' must not be null!");

		final List<INDArray> matrices = new ArrayList<>();
		INDArray target = null;
		List<String> classNames = null;

		for (final File arffFile : arffFiles) {
			// Pair<INDArray, INDArray> tsWithTarget =
			// loadTimeSeriesWithTargetFromArffFile(arffFile);
			Object[] tsTargetClassNames = loadTimeSeriesWithTargetFromArffFile(arffFile);

			if (classNames == null)
				classNames = (List<String>) tsTargetClassNames[2];
			else {
				// Check whether the same class names are used among all of the time series
				List<String> furtherClassNames = (List<String>) tsTargetClassNames[2];
				if (furtherClassNames == null || !furtherClassNames.equals(classNames))
					throw new TimeSeriesLoadingException(
							"Could not load multivariate time series with different targets. Target values have to be stored in each "
									+ "time series arff file and must be equal!");
			}

			if (target == null)
				target = (INDArray) tsTargetClassNames[1];
			else {
				// Check whether the same targets are used among all of the time series
				INDArray furtherTarget = (INDArray) tsTargetClassNames[1];
				if (furtherTarget == null || target.length() != furtherTarget.length()
						|| !target.equalsWithEps(furtherTarget, IND_TARGET_EQUALS_EPS)) {
					throw new TimeSeriesLoadingException(
							"Could not load multivariate time series with different targets. Target values have to be stored in each "
									+ "time series arff file and must be equal!");
				}
			}

			// Check for same instance length
			if (matrices.size() != 0 && ((INDArray) tsTargetClassNames[0]).shape()[0] != matrices.get(0).shape()[0])
				throw new TimeSeriesLoadingException(
						"All time series must have the same first dimensionality (number of instances).");

			matrices.add((INDArray) tsTargetClassNames[0]);
		}
		return new TimeSeriesDataset(matrices, new ArrayList<INDArray>(), target, classNames);
	}

	/**
	 * Extracting the time series and target matrices from a given arff file.
	 * Assumes the class attribute to be the last among the declared attributes in
	 * the file.
	 * 
	 * @param arffFile
	 *            The arff file to be parsed
	 * @return Returns an object consisting of three elements: 1. The time series
	 *         value matrix (INDArray), 2. the target value matrix (INDArray) and 3.
	 *         a list of the class value strings (List<String>)
	 * @throws TimeSeriesLoadingException
	 *             Throws an exception when the matrices could not be extracted from
	 *             the given arff file
	 */
	private static Object[] loadTimeSeriesWithTargetFromArffFile(final File arffFile)
			throws TimeSeriesLoadingException {
		INDArray matrix = null;
		INDArray targetMatrix = null;

		long numEmptyDataRows = 0;

		List<String> targetValues = null;

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

						targetSet = true;
					}

					// Count attributes
					if (line.startsWith(ARFF_ATTRIBUTE_PREFIX))
						attributeCount++;

					if (line.startsWith(ARFF_DATA_FLAG)) {
						readData = true;
						numInstances = fileLinesCount - lineCounter + 1;
						matrix = Nd4j.create(numInstances, targetSet ? attributeCount - 1 : attributeCount);
						targetMatrix = Nd4j.create(numInstances);
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
							String actValue = values[i];
							if (actValue.startsWith("'"))
								actValue = actValue.substring(1);
							if (actValue.endsWith("'"))
								actValue = actValue.substring(0, actValue.length() - 1);
							dValues[i] = Double.parseDouble(actValue);
						}
						matrix.putRow(lineCounter, Nd4j.create(dValues));

						if (targetSet)
							targetMatrix.putScalar(lineCounter, targetValues.indexOf(values[values.length - 1]));
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
			long endIndex = matrix.shape()[0] - numEmptyDataRows;
			matrix = matrix.get(NDArrayIndex.interval(0, endIndex));
			targetMatrix = targetMatrix.get(NDArrayIndex.interval(0, endIndex));
		}

		Object[] result = new Object[3];
		result[0] = matrix;
		result[1] = targetMatrix;
		result[2] = targetValues;
		// return new TimeSeriesDataset(Arrays.asList(matrix), null, targetMatrix,
		// targetValues);
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
