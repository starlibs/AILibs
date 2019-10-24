package ai.libs.jaicore.ml.core.dataset.sampling.infile;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.api4.java.algorithm.IAlgorithm;

import ai.libs.jaicore.ml.core.filter.sampling.infiles.AFileSamplingAlgorithm;
import ai.libs.jaicore.ml.core.filter.sampling.infiles.stratified.sampling.ClassStratiFileAssigner;
import ai.libs.jaicore.ml.core.filter.sampling.infiles.stratified.sampling.StratifiedFileSampling;
import ai.libs.jaicore.ml.core.olddataset.ArffUtilities;

public class ClassStratifiedFileSamplingTester extends GeneralFileSamplingTester {

	private static final long RANDOM_SEED = 1;

	@Override
	public IAlgorithm<?, ?> getAlgorithm(final Object problem) {
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
			e.printStackTrace();
		}
		return algorithm;
	}

}
