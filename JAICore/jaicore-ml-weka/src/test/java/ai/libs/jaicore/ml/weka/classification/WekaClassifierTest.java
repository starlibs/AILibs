package ai.libs.jaicore.ml.weka.classification;

import static org.junit.Assert.assertEquals;

import java.util.stream.Stream;

import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassificationPredictionBatch;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.algorithm.IAlgorithm;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ai.libs.jaicore.basic.algorithm.AlgorithmCreationException;
import ai.libs.jaicore.basic.algorithm.GeneralAlgorithmTester;
import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.ml.weka.classification.learner.WekaClassifier;
import ai.libs.jaicore.ml.weka.classification.learner.WekaLearningAlgorithm;
import ai.libs.jaicore.ml.weka.dataset.IWekaInstances;
import ai.libs.jaicore.ml.weka.dataset.WekaInstances;
import ai.libs.jaicore.test.MediumParameterizedTest;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;

public class WekaClassifierTest extends GeneralAlgorithmTester {

	public static Stream<Arguments> getProblemSets() {
		return Stream.of(Arguments.of(new WekaClassifierProblemSet("weka.classifiers.functions.Logistic")));
		// return WekaUtil.getBasicClassifiers().stream().map(l -> Arguments.of(new WekaClassifierProblemSet(l)));
	}

	@MediumParameterizedTest
	@MethodSource("getProblemSets")
	public void testFit(final WekaClassifierProblemSet problemSet) throws Exception {
		Pair<String, ILabeledDataset<ILabeledInstance>> ps = problemSet.getSimpleProblemInputForGeneralTestPurposes();
		WekaClassifier classifier = new WekaClassifier(ps.getX(), new String[] {});
		Classifier oClassifier = AbstractClassifier.forName(ps.getX(), null);

		/* fit both classifiers */
		IWekaInstances dataset = new WekaInstances(ps.getY());
		classifier.fit(dataset);
		oClassifier.buildClassifier(dataset.getInstances());

		/* test that predictions are identical */
		ISingleLabelClassificationPredictionBatch yHat = classifier.predict(dataset);
		int n = yHat.size();
		assertEquals(dataset.size(), n);
		for (int i = 0; i < n; i++) {
			assertEquals(oClassifier.classifyInstance(dataset.get(i).getElement()), 1.0 * yHat.get(i).getIntPrediction(), 0.01);
		}
	}

	@Override
	public IAlgorithm<?, ?> getAlgorithm(final Object problem) throws AlgorithmCreationException {
		Pair<String, ILabeledDataset<ILabeledInstance>> cProblem = (Pair<String, ILabeledDataset<ILabeledInstance>>) problem;
		try {
			return new WekaLearningAlgorithm(Class.forName(cProblem.getX()), cProblem.getY());
		} catch (ClassNotFoundException e) {
			throw new AlgorithmCreationException(e);
		}
	}

}
