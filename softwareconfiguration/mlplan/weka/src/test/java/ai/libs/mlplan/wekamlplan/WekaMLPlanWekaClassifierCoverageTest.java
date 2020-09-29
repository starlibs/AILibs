package ai.libs.mlplan.wekamlplan;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import ai.libs.jaicore.ml.weka.WekaUtil;
import ai.libs.mlplan.weka.weka.WekaMLPlanWekaClassifier;

public class WekaMLPlanWekaClassifierCoverageTest {

	@Test
	public void testClassifierCoverage() throws IOException {
		WekaMLPlanWekaClassifier mlplan = new WekaMLPlanWekaClassifier();
		Collection<String> componentNames = mlplan.getComponents().stream().map(c -> c.getName()).collect(Collectors.toList());
		System.out.println("Components:");
		componentNames.forEach(n -> System.out.println("\t" + n));

		/* check that every standard classifier is in */
		for (String classifier : WekaUtil.getNativeMultiClassClassifiers()) {
			if (classifier.toLowerCase().contains("m5")) {
				continue;
			}
			assertTrue(componentNames.contains(classifier), "Classifier " + classifier + " not covered in component set.");
		}
		for (String classifier : WekaUtil.getBinaryClassifiers()) {
			assertTrue(componentNames.contains(classifier), "Classifier " + classifier + " not covered in component set.");
		}
		for (String searcher : WekaUtil.getSearchers()) {
			assertTrue(componentNames.contains(searcher), "Searcher " + searcher + " not covered in component set.");
		}
		for (String evaluators : WekaUtil.getFeatureEvaluators()) {
			assertTrue(componentNames.contains(evaluators), "Evaluator " + evaluators + " not covered in component set.");
		}
	}

}
