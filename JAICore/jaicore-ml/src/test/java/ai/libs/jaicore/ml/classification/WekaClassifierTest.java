package ai.libs.jaicore.ml.classification;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.algorithm.PredictionException;
import org.api4.java.ai.ml.algorithm.TrainingException;
import org.junit.Test;

import ai.libs.jaicore.basic.kvstore.KVStore;
import ai.libs.jaicore.ml.dataset.numeric.ArffToNumericDatasetDeserializer;
import ai.libs.jaicore.ml.dataset.numeric.NumericDataset;

public class WekaClassifierTest {

	@Test
	public void testFit() throws TrainingException, PredictionException, FileNotFoundException, IOException {
		WekaClassifier classifier = new WekaClassifier("weka.classifiers.trees.RandomForest", new String[] {});
		KVStore s = new KVStore();
		long timestampStart = System.currentTimeMillis();
		NumericDataset dataset = new ArffToNumericDatasetDeserializer().deserializeDataset(new File("testrsc/dataset/arff/numeric_only_with_classindex.arff"));
		System.out.println((System.currentTimeMillis() - timestampStart));

		Double[] targets = new Double[dataset.getY().length];
		IntStream.range(0, dataset.getY().length).forEach(x -> {
			targets[x] = dataset.getY()[x][0];
		});

		classifier.fit(dataset.getX(), targets);
		Double[] yHat = classifier.predict(dataset.getX());

		System.out.println(Arrays.toString(yHat));
	}

}
