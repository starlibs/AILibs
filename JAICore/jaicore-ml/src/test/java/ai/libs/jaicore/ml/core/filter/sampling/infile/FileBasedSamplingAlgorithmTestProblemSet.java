package ai.libs.jaicore.ml.core.filter.sampling.infile;

import java.io.File;

import ai.libs.jaicore.basic.algorithm.AAlgorithmTestProblemSet;
import ai.libs.jaicore.basic.algorithm.AlgorithmTestProblemSetCreationException;
import ai.libs.jaicore.ml.core.dataset.serialization.OpenMLDatasetReader;

public class FileBasedSamplingAlgorithmTestProblemSet extends AAlgorithmTestProblemSet<File> {

	public FileBasedSamplingAlgorithmTestProblemSet() {
		super("Sampling");
	}

	@Override
	public File getSimpleProblemInputForGeneralTestPurposes() throws AlgorithmTestProblemSetCreationException, InterruptedException {
		// Load bodyfat data set
		try {
			return this.loadDatasetFromOpenML(560);
		} catch (Exception e) {
			throw new AlgorithmTestProblemSetCreationException(e);
		}
	}

	public File getMediumProblemInputForGeneralTestPurposes() throws AlgorithmTestProblemSetCreationException, InterruptedException {
		// Load whine quality data set
		try {
			return this.loadDatasetFromOpenML(287);
		} catch (Exception e) {
			throw new AlgorithmTestProblemSetCreationException(e);
		}
	}

	@Override
	public File getDifficultProblemInputForGeneralTestPurposes() throws AlgorithmTestProblemSetCreationException, InterruptedException {
		// Load vancouver employee data set (more than 1.5 million instances but only 13 features)
		try {
			return this.loadDatasetFromOpenML(1237);
		} catch (Exception e) {
			throw new AlgorithmTestProblemSetCreationException(e);
		}
	}

	private File loadDatasetFromOpenML(final int id) throws Exception {
		return OpenMLDatasetReader.getArffFileOfOpenMLID(id);
	}

}
