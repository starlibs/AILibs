package ai.libs.jaicore.ml.classification;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import org.api4.java.ai.ml.dataset.supervised.INumericFeatureSupervisedDataset;
import org.api4.java.ai.ml.dataset.supervised.classification.INumericFeatureSingleLabelClassificationInstance;
import org.api4.java.ai.ml.learner.fit.TrainingException;
import org.api4.java.ai.ml.learner.predict.PredictionException;
import org.junit.Test;

import ai.libs.jaicore.basic.kvstore.KVStore;
import ai.libs.jaicore.ml.dataset.numeric.ArffToNumericDatasetDeserializer;

public class WekaClassifierTest {

	@Test
	public void testFit() throws TrainingException, PredictionException, FileNotFoundException, IOException {
		WekaClassifier classifier = new WekaClassifier("weka.classifiers.trees.RandomForest", new String[] {});
		KVStore s = new KVStore();
		long timestampStart = System.currentTimeMillis();
		INumericFeatureSupervisedDataset<String, INumericFeatureSingleLabelClassificationInstance> dataset = new ArffToNumericDatasetDeserializer().deserializeDataset(new File("testrsc/dataset/arff/numeric_only_with_classindex.arff"));
		System.out.println((System.currentTimeMillis() - timestampStart));

		classifier.fit(dataset);
		Double[] yHat = classifier.predict(dataset.getX());

		System.out.println(Arrays.toString(yHat));
	}

}
