package ai.libs.automl;

import java.io.IOException;
import java.util.stream.Stream;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.learner.ISupervisedLearner;
import org.api4.java.algorithm.IAlgorithm;
import org.junit.jupiter.params.provider.Arguments;

import ai.libs.jaicore.basic.algorithm.AlgorithmCreationException;
import ai.libs.jaicore.basic.algorithm.GeneralAlgorithmTester;
import ai.libs.jaicore.ml.experiments.OpenMLProblemSet;

public abstract class AutoMLAlgorithmCoreFunctionalityTester extends GeneralAlgorithmTester {

	// creates the test data
	public static Stream<Arguments> getProblemSets() throws IOException, Exception {
		return Stream.of(Arguments.of(new OpenMLProblemSet(3)) // kr-vs-kp
		// Arguments.of(new OpenMLProblemSet(1150)); // AP_Breast_Lung
		// Arguments.of(new OpenMLProblemSet(1156)); // AP_Omentum_Ovary
		// Arguments.of(new OpenMLProblemSet(1152)); // AP_Prostate_Ovary
		// Arguments.of(new OpenMLProblemSet(1240)); // AirlinesCodrnaAdult
		// Arguments.of(new OpenMLProblemSet(1457)); // amazon
		// Arguments.of(new OpenMLProblemSet(149)); // CovPokElec
		// Arguments.of(new OpenMLProblemSet(41103)); // cifar-10
		// Arguments.of(new OpenMLProblemSet(40668)); // connect-4
		);
	}

	@Override
	public IAlgorithm<?, ?> getAlgorithm(final Object problem) throws AlgorithmCreationException {
		try {
			ILabeledDataset<?> dataset = (ILabeledDataset<?>) problem;
			return this.getAutoMLAlgorithm(dataset);
		} catch (Exception e) {
			throw new AlgorithmCreationException(e);
		}
	}

	public abstract IAlgorithm<? extends ILabeledDataset<?>, ? extends ISupervisedLearner<?, ?>> getAutoMLAlgorithm(ILabeledDataset<?> data) throws AlgorithmCreationException, IOException;
}
