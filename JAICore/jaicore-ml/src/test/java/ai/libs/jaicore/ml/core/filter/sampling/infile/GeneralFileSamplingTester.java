package ai.libs.jaicore.ml.core.filter.sampling.infile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.AfterClass;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;
import org.openml.apiconnector.io.OpenmlConnector;

import ai.libs.jaicore.basic.algorithm.GeneralAlgorithmTester;
import ai.libs.jaicore.basic.algorithm.IAlgorithmTestProblemSet;
import ai.libs.jaicore.logging.LoggerUtil;
import ai.libs.jaicore.ml.core.filter.sampling.infiles.AFileSamplingAlgorithm;
import ai.libs.jaicore.ml.core.filter.sampling.infiles.ArffUtilities;

/**
 * This class provides tests for assuring some basic sampling algorithm
 * properties on file level for the datasets whine quality and higgs. For actual
 * sampling algorithm implementation it has to be extended.
 *
 * @author Lukas Brandt
 */
public abstract class GeneralFileSamplingTester extends GeneralAlgorithmTester {

	protected static final double DEFAULT_SAMPLE_FRACTION = 0.1;
	protected static final String OUTPUT_FILE_NAME = System.getProperty("user.home") + File.separator + UUID.randomUUID().toString() + ".arff";

	public static Stream<Arguments> getProblemSets() {
		IAlgorithmTestProblemSet<?> problemSet = new FileBasedSamplingAlgorithmTestProblemSet();
		return Stream.of(Arguments.of(problemSet));
	}

	/**
	 * This test verifies that the produced sample file has the specified amount of
	 * datapoints for a small dataset file.
	 *
	 * @throws Exception
	 */
	@Test
	public void testSampleSizeSmallProblem() throws Exception {
		this.logger.info("Test sample size for big problem.");
		File input = this.getSimpleProblemInputForGeneralTestPurposes();
		this.testSampleSize(input);
		this.logger.info("Finished.");
	}

	/**
	 * This test verifies that the produced sample file has the specified amount of
	 * datapoints for a big dataset file.
	 *
	 * @throws Exception
	 */
	@Test
	public void testSampleSizeBigProblem() throws Exception {
		this.logger.info("Test sample size for big problem.");
		File input = this.getDifficultProblemInputForGeneralTestPurposes();
		this.testSampleSize(input);
		this.logger.info("Finished.");
	}

	private void testSampleSize(final File input) throws Exception {
		AFileSamplingAlgorithm samplingAlgorithm = (AFileSamplingAlgorithm) this.getAlgorithm(input);
		samplingAlgorithm.setLoggerName(LoggerUtil.LOGGER_NAME_TESTEDALGORITHM);
		this.logger.info("Testing {}.", samplingAlgorithm.getClass().getName());
		int inputSize = ArffUtilities.countDatasetEntries(input, true);
		int sampleSize = (int) (inputSize * DEFAULT_SAMPLE_FRACTION);
		samplingAlgorithm.setSampleSize(sampleSize);
		samplingAlgorithm.setOutputFileName(OUTPUT_FILE_NAME);
		samplingAlgorithm.call();
		this.logger.info("Call to {} completed.", samplingAlgorithm.getClass().getName());
		int outputSize = ArffUtilities.countDatasetEntries(new File(OUTPUT_FILE_NAME), true);
		// Allow sample size to be one off, in case of rounding errors
		assertTrue("Required sample size is " + sampleSize + " but output is " + outputSize, Math.abs(sampleSize - outputSize) <= 1);
	}

	/**
	 * This test verifies that the produced sample file contains no duplicated
	 * samples for a small dataset file.
	 *
	 * @throws Exception
	 */
	@Test
	public void testNoDuplicatesSmallProblem() throws Exception {
		File input = this.getSimpleProblemInputForGeneralTestPurposes();
		this.testNoDuplicates(input);
	}

	/**
	 * This test verifies that the produced sample file contains no duplicated
	 * samples for a big dataset file.
	 *
	 * @throws Exception
	 */
	@Test
	public void testNoDuplicatesBigProblem() throws Exception {
		File input = this.getDifficultProblemInputForGeneralTestPurposes();
		this.testNoDuplicates(input);
	}

	private void testNoDuplicates(final File input) throws Exception {
		this.logger.info("Starting test for duplicates with file {} and sampling test {}", input, this.getClass().getName());
		AFileSamplingAlgorithm samplingAlgorithm = (AFileSamplingAlgorithm) this.getAlgorithm(input);
		int inputSize = ArffUtilities.countDatasetEntries(input, true);
		int sampleSize = (int) (inputSize * DEFAULT_SAMPLE_FRACTION);
		samplingAlgorithm.setSampleSize(sampleSize);
		samplingAlgorithm.setOutputFileName(OUTPUT_FILE_NAME);
		samplingAlgorithm.call();
		File outputFile = new File(OUTPUT_FILE_NAME);
		int sampleAmount = ArffUtilities.countDatasetEntries(outputFile, true);
		Set<String> set = new HashSet<>();
		BufferedReader reader = new BufferedReader(new FileReader(outputFile));
		ArffUtilities.skipWithReaderToDatapoints(reader);
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.trim().equals("") || line.trim().charAt(0) == '%') {
				continue;
			} else {
				if (set.contains(line.trim())) {
					System.err.println("Duplicate line: " + line.trim());
				}
				set.add(line.trim());
			}
		}
		reader.close();
		assertEquals(sampleAmount, set.size());
	}

	/**
	 * Verifies that the sampling algorithm does not modify the original input
	 * dataset file.
	 *
	 * @throws Exception
	 */
	@Test
	public void testOriginalDatasetFileNotModified() throws Exception {
		File originalDataset = this.getSimpleProblemInputForGeneralTestPurposes();
		long changed = originalDataset.lastModified();
		AFileSamplingAlgorithm samplingAlgorithm = (AFileSamplingAlgorithm) this.getAlgorithm(originalDataset);
		int inputSize = ArffUtilities.countDatasetEntries(originalDataset, true);
		int sampleSize = (int) (inputSize * DEFAULT_SAMPLE_FRACTION);
		samplingAlgorithm.setSampleSize(sampleSize);
		samplingAlgorithm.setOutputFileName(OUTPUT_FILE_NAME);
		samplingAlgorithm.call();
		assertEquals(originalDataset.lastModified(), changed);
	}

	public File getSimpleProblemInputForGeneralTestPurposes() throws Exception {
		OpenmlConnector client = new OpenmlConnector();
		return client.datasetGet(client.dataGet(560));
	}

	public File getDifficultProblemInputForGeneralTestPurposes() throws Exception {
		OpenmlConnector client = new OpenmlConnector();
		return client.datasetGet(client.dataGet(182));
	}

	@AfterClass
	public static void removeOutputFile() {
		new File(OUTPUT_FILE_NAME).deleteOnExit();
	}

}
