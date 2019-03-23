package jaicore.ml.tsc.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import jaicore.ml.core.dataset.TimeSeriesDataset;
import jaicore.ml.core.dataset.attribute.categorical.CategoricalAttributeType;
import jaicore.ml.tsc.exceptions.TimeSeriesLoadingException;

/**
 * Unit tests for loading time series datasets from files using the class
 * <code>TimeSeriesLoader</code>.
 * 
 * @author Julian Lienen
 *
 */
public class TimeSeriesLoaderTest {
	/**
	 * Path prefix for the time series classification datasets.
	 */
	private static final String TSC_DATASET_PATH_PREFIX = "data" + File.separator;

	@Test
	public void testUnivariateArffFileLoading() throws TimeSeriesLoadingException {
		final File datasetFile = new File(
				TSC_DATASET_PATH_PREFIX + "univariate" + File.separator + "Car" + File.separator + "Car_TRAIN.arff");

		final TimeSeriesDataset result = TimeSeriesLoader.loadArff(datasetFile);

		final int expectedNumInstances = 60;
		final int expectedNumSteps = 577;
		final int expectedNumVariables = 1; // Univariate
		final int expectedNumTargets = 60;

		assertEquals(expectedNumInstances, result.getNumberOfInstances());
		assertEquals(expectedNumSteps, (int) result.getValues(0).shape()[1]);
		assertEquals(expectedNumVariables, result.getNumberOfVariables());
		assertEquals(expectedNumTargets, result.getTargets().length());
	}

	@Test
	public void testMultivariateArffFileLoading() throws TimeSeriesLoadingException {
		File datasetFile0 = new File(
				TSC_DATASET_PATH_PREFIX + "multivariate" + File.separator + "Libras" + File.separator
						+ "LibrasDimension1_TRAIN.arff");
		File datasetFile1 = new File(
				TSC_DATASET_PATH_PREFIX + "multivariate" + File.separator + "Libras" + File.separator
						+ "LibrasDimension2_TRAIN.arff");

		final TimeSeriesDataset result = TimeSeriesLoader.loadArffs(datasetFile0, datasetFile1);

		final int expectedNumInstances = 180;
		final int expectedNumSteps = 45;
		final int expectedNumVariables = 2; // Multivariate
		final int expectedNumTargets = 180;

		assertEquals(expectedNumInstances, result.getNumberOfInstances());
		assertEquals(expectedNumSteps, (int) result.getValues(0).shape()[1]);
		assertEquals(expectedNumVariables, result.getNumberOfVariables());
		assertEquals(expectedNumTargets, result.getTargets().length());
	}

	@Test
	public void testMultivariateArffFileLoadingStringClass() throws TimeSeriesLoadingException {

		File datasetFile0 = new File(TSC_DATASET_PATH_PREFIX
				+ "multivariate" + File.separator + "FingerMovements" + File.separator
				+ "FingerMovementsDimension1_TRAIN.arff");
		File datasetFile1 = new File(TSC_DATASET_PATH_PREFIX
				+ "multivariate" + File.separator + "FingerMovements" + File.separator
				+ "FingerMovementsDimension2_TRAIN.arff");

		final TimeSeriesDataset result = TimeSeriesLoader.loadArffs(datasetFile0, datasetFile1);

		final int expectedNumInstances = 316;
		final int expectedNumSteps = 50;
		final int expectedNumVariables = 2; // Multivariate
		final int expectedNumTargets = 316;
		final List<String> expectedClassDomain = Arrays.asList("left", "right");

		assertEquals(expectedNumInstances, result.getNumberOfInstances());
		assertEquals(expectedNumSteps, (int) result.getValues(0).shape()[1]);
		assertEquals(expectedNumVariables, result.getNumberOfVariables());
		assertEquals(expectedNumTargets, result.getTargets().length());
		assertEquals(expectedClassDomain, ((CategoricalAttributeType) result.getTargetType()).getDomain());
	}
}
