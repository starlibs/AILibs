package jaicore.ml.core.dataset.sampling.infile;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.basic.algorithm.IAlgorithm;
import jaicore.basic.algorithm.IAlgorithmFactory;
import jaicore.ml.core.dataset.ArffUtilities;
import jaicore.ml.core.dataset.sampling.infiles.AFileSamplingAlgorithm;
import jaicore.ml.core.dataset.sampling.infiles.ReservoirSampling;

public class ReservoirSamplingTest extends GeneralFileSamplingTester {

	private static final long RANDOM_SEED = 1;

	@Override
	public IAlgorithmFactory<File, File> getFactory() {
		return new IAlgorithmFactory<File, File>() {

			private File input;

			@Override
			public <P> void setProblemInput(P problemInput, AlgorithmProblemTransformer<P, File> reducer) {
				throw new UnsupportedOperationException(
						"Problem input with reducer is not applicable for file-level subsampling algorithms!");
			}

			@Override
			public void setProblemInput(File problemInput) {
				this.input = problemInput;
			}

			@Override
			public IAlgorithm<File, File> getAlgorithm() {
				Random r = new Random(RANDOM_SEED);
				AFileSamplingAlgorithm algorithm = new ReservoirSampling(r);
				try {
					algorithm.setOutputFileName(OUTPUT_FILE_NAME);
					if (this.input != null) {
						algorithm.setInput(input);
						int inputSize = ArffUtilities.countDatasetEntries(input, true);
						int sampleSize = (int) (inputSize * DEFAULT_SAMPLE_FRACTION);
						algorithm.setSampleSize(sampleSize);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				return algorithm;
			}

		};
	}

}
