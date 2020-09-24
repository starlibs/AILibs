package ai.libs.jaicore.ml.preprocessing;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.algorithm.IAlgorithm;
import org.junit.jupiter.api.Test;
import org.junit.runners.Parameterized.Parameters;

import ai.libs.jaicore.basic.algorithm.AlgorithmCreationException;
import ai.libs.jaicore.basic.algorithm.GeneralAlgorithmTester;
import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.ml.weka.WekaUtil;
import ai.libs.jaicore.ml.weka.dataset.IWekaInstances;
import ai.libs.jaicore.ml.weka.dataset.WekaInstances;
import ai.libs.jaicore.ml.weka.preprocessing.WekaPreprocessorFitter;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.attributeSelection.AttributeSelection;

public class PreprocessorTest extends GeneralAlgorithmTester {

	// creates the test data
	@Parameters(name = "{0}")
	public static Collection<Object[]> data() {
		Collection<List<String>> validCombos = WekaUtil.getAdmissibleSearcherEvaluatorCombinationsForAttributeSelection();
		List<Object> problemSets = validCombos.stream().map(c -> new WekaPreprocessorProblemSet(c.get(0), c.get(1))).collect(Collectors.toList());
		Object[][] data = new Object[problemSets.size()][1];
		for (int i = 0; i < data.length; i++) {
			data[i][0] = problemSets.get(i);
		}
		return Arrays.asList(data);
	}

	@Test
	public void testFit() throws Exception {
		Pair<String, ILabeledDataset<ILabeledInstance>> ps = (Pair<String, ILabeledDataset<ILabeledInstance>>)this.problemSet.getSimpleProblemInputForGeneralTestPurposes();
		String[] parts = ps.getX().split("/");
		ASSearch search = ASSearch.forName(parts[0], null);
		ASEvaluation eval = ASEvaluation.forName(parts[1], null);
		AttributeSelection as = new AttributeSelection();
		as.setSearch(search);
		as.setEvaluator(eval);

		/* fit pre-processor */
		IWekaInstances dataset = new WekaInstances(ps.getY());
		as.SelectAttributes(dataset.getInstances());

		/* test that pre-processor can be applied */
		as.reduceDimensionality(dataset.getInstances());
		assertTrue(true);
	}

	@Override
	public IAlgorithm<?, ?> getAlgorithm(final Object problem) throws AlgorithmCreationException {
		Pair<String, ILabeledDataset<ILabeledInstance>> cProblem = (Pair<String, ILabeledDataset<ILabeledInstance>>)problem;
		String[] parts = cProblem.getX().split("/");
		return new WekaPreprocessorFitter(cProblem.getY(), parts[0], parts[1]);
	}
}
