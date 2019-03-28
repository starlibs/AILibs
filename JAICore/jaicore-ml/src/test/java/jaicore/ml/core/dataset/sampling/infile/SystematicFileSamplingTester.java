package jaicore.ml.core.dataset.sampling.infile;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import jaicore.basic.algorithm.IAlgorithm;
import jaicore.ml.core.dataset.ArffUtilities;
import jaicore.ml.core.dataset.sampling.infiles.AFileSamplingAlgorithm;
import jaicore.ml.core.dataset.sampling.infiles.SystematicFileSampling;

public class SystematicFileSamplingTester extends GeneralFileSamplingTester {

	private static final long RANDOM_SEED = 1;

	@Override
	public IAlgorithm<?, ?> getAlgorithm(Object problem) {
		File input = (File) problem;
		Random r = new Random(RANDOM_SEED);
		AFileSamplingAlgorithm algorithm = new SystematicFileSampling(r, input);
		try {
			algorithm.setOutputFileName(OUTPUT_FILE_NAME);
			if (input != null) {
				int inputSize = ArffUtilities.countDatasetEntries(input, true);
				int sampleSize = (int) (inputSize * DEFAULT_SAMPLE_FRACTION);
				algorithm.setSampleSize(sampleSize);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return algorithm;
	}

}
