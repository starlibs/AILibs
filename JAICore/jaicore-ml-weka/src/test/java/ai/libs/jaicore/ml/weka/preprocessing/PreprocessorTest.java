package ai.libs.jaicore.ml.weka.preprocessing;

import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.algorithm.IAlgorithm;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ai.libs.jaicore.basic.algorithm.AlgorithmCreationException;
import ai.libs.jaicore.basic.algorithm.GeneralAlgorithmTester;
import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.ml.weka.WekaUtil;
import ai.libs.jaicore.ml.weka.dataset.IWekaInstances;
import ai.libs.jaicore.ml.weka.dataset.WekaInstances;
import ai.libs.jaicore.test.MediumParameterizedTest;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.attributeSelection.AttributeSelection;

public class PreprocessorTest extends GeneralAlgorithmTester {

	public static Stream<Arguments> getProblemSets() {
		Collection<List<String>> validCombos = WekaUtil.getAdmissibleSearcherEvaluatorCombinationsForAttributeSelection();
		return validCombos.stream().map(c -> Arguments.of(new WekaPreprocessorProblemSet(c.get(0), c.get(1))));
	}

	@MediumParameterizedTest
	@MethodSource("getProblemSets")
	public void testFit(final WekaPreprocessorProblemSet problemSet) throws Exception {
		Pair<String, ILabeledDataset<ILabeledInstance>> ps = problemSet.getSimpleProblemInputForGeneralTestPurposes();
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
