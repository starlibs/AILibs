package ai.libs.jaicore.ml.core.filter.sampling.infile;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.api4.java.algorithm.IAlgorithm;

import ai.libs.jaicore.basic.algorithm.AlgorithmCreationException;
import ai.libs.jaicore.ml.core.filter.sampling.infiles.AFileSamplingAlgorithm;
import ai.libs.jaicore.ml.core.filter.sampling.infiles.ArffUtilities;
import ai.libs.jaicore.ml.core.filter.sampling.infiles.stratified.sampling.ClassStratiFileAssigner;
import ai.libs.jaicore.ml.core.filter.sampling.infiles.stratified.sampling.StratifiedFileSampling;

public class ClassStratifiedFileSamplingTester extends GeneralFileSamplingTester {

	private static final long RANDOM_SEED = 1;

	@Override
	public IAlgorithm<?, ?> getAlgorithm(final Object problem) throws AlgorithmCreationException {
		File input = (File) problem;
		Random r = new Random(RANDOM_SEED);
		AFileSamplingAlgorithm algorithm = new StratifiedFileSampling(r, new ClassStratiFileAssigner(), input);
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
