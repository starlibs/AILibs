package ai.libs.jaicore.ml.classification;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.api4.java.ai.ml.core.dataset.supervised.INumericFeatureSupervisedDataset;
import org.api4.java.ai.ml.core.dataset.supervised.INumericFeatureSupervisedInstance;
import org.api4.java.ai.ml.core.evaluation.IPredictionBatch;
import org.api4.java.ai.ml.core.exception.PredictionException;
import org.api4.java.ai.ml.core.exception.TrainingException;
import org.junit.Test;

import ai.libs.jaicore.basic.kvstore.KVStore;
import ai.libs.jaicore.ml.core.dataset.ArffToNumericDatasetDeserializer;
import ai.libs.jaicore.ml.weka.learner.WekaClassifier;

public class WekaClassifierTest {

	@Test
	public void testFit() throws TrainingException, PredictionException, FileNotFoundException, IOException, InterruptedException {
		WekaClassifier classifier = new WekaClassifier("weka.classifiers.trees.RandomForest", new String[] {});
		KVStore s = new KVStore();
		long timestampStart = System.currentTimeMillis();
		INumericFeatureSupervisedDataset<Double, INumericFeatureSupervisedInstance<Double>> dataset = new ArffToNumericDatasetDeserializer().deserializeDataset(new File("testrsc/dataset/arff/numeric_only_with_classindex.arff"));
		System.out.println((System.currentTimeMillis() - timestampStart));

		classifier.fit(dataset);
		IPredictionBatch<Double> yHat = classifier.predict(dataset);

		System.out.println(yHat);
	}

}
