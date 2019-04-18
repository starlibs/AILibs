package jaicore.ml.core.dataset.sampling.infile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataSetDescription;

import jaicore.basic.algorithm.GeneralAlgorithmTester;
import jaicore.ml.core.dataset.ArffUtilities;
import jaicore.ml.core.dataset.sampling.infiles.AFileSamplingAlgorithm;

/**
 * This class provides tests for assuring some basic sampling algorithm
 * properties on file level for the datasets whine quality and higgs. For actual
 * sampling algorithm implementation it has to be extended.
 * 
 * @author Lukas Brandt
 */
@RunWith(JUnit4.class)
public abstract class GeneralFileSamplingTester extends GeneralAlgorithmTester {

	private static final String OPENML_API_KEY = "4350e421cdc16404033ef1812ea38c01";
	protected static final double DEFAULT_SAMPLE_FRACTION = 0.1;
	protected static final String OUTPUT_FILE_NAME = System.getProperty("user.home") + File.separator + UUID.randomUUID().toString() + ".arff";

	/**
	 * This test verifies that the produced sample file has the specified amount of
	 * datapoints for a small dataset file.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSampleSizeSmallProblem() throws Exception {
		File input = this.getSimpleProblemInputForGeneralTestPurposes();
		this.testSampleSize(input);
	}

	/**
	 * This test verifies that the produced sample file has the specified amount of
	 * datapoints for a big dataset file.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSampleSizeBigProblem() throws Exception {
		File input = this.getDifficultProblemInputForGeneralTestPurposes();
		this.testSampleSize(input);
	}

	private void testSampleSize(File input) throws Exception {
		AFileSamplingAlgorithm samplingAlgorithm = (AFileSamplingAlgorithm) this.getAlgorithm(input);
		int inputSize = ArffUtilities.countDatasetEntries(input, true);
		int sampleSize = (int) (inputSize * DEFAULT_SAMPLE_FRACTION);
		samplingAlgorithm.setSampleSize(sampleSize);
		samplingAlgorithm.setOutputFileName(OUTPUT_FILE_NAME);
		samplingAlgorithm.call();
		int outputSize = ArffUtilities.countDatasetEntries(new File(OUTPUT_FILE_NAME), true);
		// Allow sample size to be one off, in case of rounding errors
		assertTrue(sampleSize >= outputSize - 1 && sampleSize <= outputSize + 1);
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

	private void testNoDuplicates(File input) throws Exception {
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
					System.out.println("Duplicate line: " + line.trim());
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
		DataSetDescription description = client.dataGet(188);
		File file = description.getDataset(OPENML_API_KEY);
		return file;
	}

	public File getDifficultProblemInputForGeneralTestPurposes() throws Exception {
		OpenmlConnector client = new OpenmlConnector();
		DataSetDescription description = client.dataGet(182);
		File file = description.getDataset(OPENML_API_KEY);
		return file;
	}

	@AfterClass
	public static void removeOutputFile() {
		new File(OUTPUT_FILE_NAME).deleteOnExit();
	}

}
