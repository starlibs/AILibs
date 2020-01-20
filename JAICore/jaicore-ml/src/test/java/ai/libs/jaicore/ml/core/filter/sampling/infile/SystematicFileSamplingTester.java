package ai.libs.jaicore.ml.core.filter.sampling.infile;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.api4.java.algorithm.IAlgorithm;

import ai.libs.jaicore.basic.algorithm.AlgorithmCreationException;
import ai.libs.jaicore.ml.core.filter.sampling.infiles.AFileSamplingAlgorithm;
import ai.libs.jaicore.ml.core.filter.sampling.infiles.ArffUtilities;
import ai.libs.jaicore.ml.core.filter.sampling.infiles.SystematicFileSampling;

public class SystematicFileSamplingTester extends GeneralFileSamplingTester {

	private static final long RANDOM_SEED = 1;

	// FIXME (better not test with SSD: writes many small files which might kill an SSD)
	@Override
	public IAlgorithm<?, ?> getAlgorithm(final Object problem) throws AlgorithmCreationException {
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
			throw new AlgorithmCreationException(e);
		}
		return algorithm;
	}

}
