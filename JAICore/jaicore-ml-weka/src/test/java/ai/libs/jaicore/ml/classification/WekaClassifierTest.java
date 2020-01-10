package ai.libs.jaicore.ml.classification;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassificationPredictionBatch;
import org.junit.Test;

import ai.libs.jaicore.ml.core.dataset.serialization.ArffDatasetAdapter;
import ai.libs.jaicore.ml.weka.classification.learner.WekaClassifier;
import ai.libs.jaicore.ml.weka.dataset.IWekaInstances;
import ai.libs.jaicore.ml.weka.dataset.WekaInstances;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;

public class WekaClassifierTest {

	@Test
	public void testFit() throws Exception {
		WekaClassifier classifier = new WekaClassifier("weka.classifiers.trees.RandomForest", new String[] {});
		Classifier oClassifier = AbstractClassifier.forName("weka.classifiers.trees.RandomForest", null);

		/* fit both classifiers */
		IWekaInstances dataset = new WekaInstances(ArffDatasetAdapter.readDataset(new File("testrsc/dataset/arff/numeric_only_with_classindex.arff")));
		classifier.fit(dataset);
		oClassifier.buildClassifier(dataset.getInstances());

		/* test that predictions are identical */
		ISingleLabelClassificationPredictionBatch yHat = classifier.predict(dataset);
		int n = yHat.size();
		assertEquals(dataset.size(), n);
		for (int i = 0; i < n; i++) {
			assertEquals(oClassifier.classifyInstance(dataset.get(i).getElement()), yHat.get(i));
		}
	}

}
