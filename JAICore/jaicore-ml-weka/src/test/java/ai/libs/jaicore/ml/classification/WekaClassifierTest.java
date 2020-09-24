package ai.libs.jaicore.ml.classification;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassificationPredictionBatch;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.algorithm.IAlgorithm;
import org.junit.jupiter.api.Test;
import org.junit.runners.Parameterized.Parameters;

import ai.libs.jaicore.basic.algorithm.AlgorithmCreationException;
import ai.libs.jaicore.basic.algorithm.GeneralAlgorithmTester;
import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.ml.weka.WekaUtil;
import ai.libs.jaicore.ml.weka.classification.learner.WekaClassifier;
import ai.libs.jaicore.ml.weka.classification.learner.WekaLearningAlgorithm;
import ai.libs.jaicore.ml.weka.dataset.IWekaInstances;
import ai.libs.jaicore.ml.weka.dataset.WekaInstances;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;

public class WekaClassifierTest extends GeneralAlgorithmTester {

	// creates the test data
	@Parameters(name = "{0}")
	public static Collection<Object[]> data() {
		List<Object> problemSets = WekaUtil.getBasicLearners().stream().map(WekaClassifierProblemSet::new).collect(Collectors.toList());
		Object[][] data = new Object[problemSets.size()][1];
		for (int i = 0; i < data.length; i++) {
			data[i][0] = problemSets.get(i);
		}
		return Arrays.asList(data);
	}

	@Test
	public void testFit() throws Exception {
		Pair<String, ILabeledDataset<ILabeledInstance>> ps = (Pair<String, ILabeledDataset<ILabeledInstance>>)this.problemSet.getSimpleProblemInputForGeneralTestPurposes();
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
		Pair<String, ILabeledDataset<ILabeledInstance>> cProblem = (Pair<String, ILabeledDataset<ILabeledInstance>>)problem;
		try {
			return new WekaLearningAlgorithm(Class.forName(cProblem.getX()), cProblem.getY());
		} catch (ClassNotFoundException e)  {
			throw new AlgorithmCreationException(e);
		}
	}

}
