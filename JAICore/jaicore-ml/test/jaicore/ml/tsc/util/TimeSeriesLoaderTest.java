package jaicore.ml.tsc.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import org.junit.Test;

import jaicore.ml.core.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.exceptions.TimeSeriesLoadingException;

/**
 * Unit tests for loading time series datasets from files using the class
 * <code>TimeSeriesLoader</code>.
 * 
 * @author Julian Lienen
 *
 */
public class TimeSeriesLoaderTest {
	@Test
	public void testUnivariateArffFileLoading() throws TimeSeriesLoadingException {
		final File datasetFile = new File("C:\\Users\\Julian\\Downloads\\TSC\\Downloads\\Car\\Car_TRAIN.arff");

		final TimeSeriesDataset result = TimeSeriesLoader.loadArff(datasetFile);

		final int expectedNumInstances = 60;
		final int expectedNumSteps = 578;
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
				"C:\\Users\\Julian\\Downloads\\MultivariateTSCProblems\\Libras\\LibrasDimension1_TRAIN.arff");
		File datasetFile1 = new File(
				"C:\\Users\\Julian\\Downloads\\MultivariateTSCProblems\\Libras\\LibrasDimension2_TRAIN.arff");

		final TimeSeriesDataset result = TimeSeriesLoader.loadArffs(datasetFile0, datasetFile1);

		final int expectedNumInstances = 180;
		final int expectedNumSteps = 46;
		final int expectedNumVariables = 2; // Multivariate
		final int expectedNumTargets = 180;

		assertEquals(expectedNumInstances, result.getNumberOfInstances());
		assertEquals(expectedNumSteps, (int) result.getValues(0).shape()[1]);
		assertEquals(expectedNumVariables, result.getNumberOfVariables());
		assertEquals(expectedNumTargets, result.getTargets().length());
	}

	@Test
	public void testMultivariateArffFileLoadingStringClass() throws TimeSeriesLoadingException {

		File datasetFile0 = new File(
				"C:\\Users\\Julian\\Downloads\\MultivariateTSCProblems\\FingerMovements\\FingerMovementsDimension1_TRAIN.arff");
		File datasetFile1 = new File(
				"C:\\Users\\Julian\\Downloads\\MultivariateTSCProblems\\FingerMovements\\FingerMovementsDimension2_TRAIN.arff");

		final TimeSeriesDataset result = TimeSeriesLoader.loadArffs(datasetFile0, datasetFile1);

		final int expectedNumInstances = 316;
		final int expectedNumSteps = 51;
		final int expectedNumVariables = 2; // Multivariate
		final int expectedNumTargets = 316;

		assertEquals(expectedNumInstances, result.getNumberOfInstances());
		assertEquals(expectedNumSteps, (int) result.getValues(0).shape()[1]);
		assertEquals(expectedNumVariables, result.getNumberOfVariables());
		assertEquals(expectedNumTargets, result.getTargets().length());
	}
}
