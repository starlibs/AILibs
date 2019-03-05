package jaicore.ml.core.dataset.sampling.infile;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.AfterClass;
import org.junit.Test;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.basic.algorithm.GeneralAlgorithmTester;
import jaicore.basic.algorithm.IAlgorithmFactory;
import jaicore.ml.core.dataset.ArffUtilities;
import jaicore.ml.core.dataset.sampling.infiles.AFileSamplingAlgorithm;

/**
 * This class provides tests for assuring some basic sampling algorithm
 * properties on file level for the datasets whine quality and higgs. For actual
 * sampling algorithm implementation it has to be extended.
 * 
 * @author Lukas Brandt
 */
public abstract class GeneralFileSamplingTester extends GeneralAlgorithmTester<Object, File, File> {

	protected static final double DEFAULT_SAMPLE_FRACTION = 0.1;
	protected static final String OUTPUT_FILE_NAME = System.getProperty("user.home") + File.separator
			+ UUID.randomUUID().toString() + ".arff";

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
		IAlgorithmFactory<File, File> factory = this.getFactory();
		factory.setProblemInput(input);
		AFileSamplingAlgorithm samplingAlgorithm = (AFileSamplingAlgorithm) factory.getAlgorithm();
		int inputSize = ArffUtilities.countDatasetEntries(input, true);
		int sampleSize = (int) (inputSize * DEFAULT_SAMPLE_FRACTION);
		samplingAlgorithm.setSampleSize(sampleSize);
		samplingAlgorithm.setOutputFileName(OUTPUT_FILE_NAME);
		samplingAlgorithm.call();
		int outputSize = ArffUtilities.countDatasetEntries(new File(OUTPUT_FILE_NAME), true);
		assertEquals(sampleSize, outputSize);
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
		IAlgorithmFactory<File, File> factory = this.getFactory();
		factory.setProblemInput(input);
		AFileSamplingAlgorithm samplingAlgorithm = (AFileSamplingAlgorithm) factory.getAlgorithm();
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
				set.add(line.trim());
			}
		}
		reader.close();
		assertEquals(sampleAmount, set.size());
	}

	/**
	 * Verifies that the sampling algorithm does not modify the original input dataset file.
	 * @throws Exception 
	 */
	@Test
	public void testOriginalDatasetFileNotModified() throws Exception {
		File originalDataset = this.getSimpleProblemInputForGeneralTestPurposes();
		long changed = originalDataset.lastModified();
		IAlgorithmFactory<File, File> factory = this.getFactory();
		factory.setProblemInput(originalDataset);
		AFileSamplingAlgorithm samplingAlgorithm = (AFileSamplingAlgorithm) factory.getAlgorithm();
		int inputSize = ArffUtilities.countDatasetEntries(originalDataset, true);
		int sampleSize = (int) (inputSize * DEFAULT_SAMPLE_FRACTION);
		samplingAlgorithm.setSampleSize(sampleSize);
		samplingAlgorithm.setOutputFileName(OUTPUT_FILE_NAME);
		samplingAlgorithm.call();
		assertEquals(originalDataset.lastModified(), changed);
	}
	
	@Override
	public AlgorithmProblemTransformer<Object, File> getProblemReducer() {
		throw new UnsupportedOperationException("Problem reducer not applicable for sampling algorithms!");
	}

	@Override
	public File getSimpleProblemInputForGeneralTestPurposes() throws Exception {
		return new File("testsrc/ml/orig/vowel.arff");
	}

	@Override
	public File getDifficultProblemInputForGeneralTestPurposes() throws Exception {
		return new File("testsrc/ml/orig/letter.arff");
	}

	@AfterClass
	public static void removeOutputFile() {
		new File(OUTPUT_FILE_NAME).deleteOnExit();
	}
	
}
