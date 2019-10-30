package ai.libs.jaicore.ml.classification;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.api4.java.ai.ml.classification.singlelabel.learner.ISingleLabelClassificationPredictionBatch;
import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.exception.PredictionException;
import org.api4.java.ai.ml.core.exception.TrainingException;
import org.junit.Test;

import ai.libs.jaicore.ml.core.dataset.serialization.ArffDatasetAdapter;
import ai.libs.jaicore.ml.weka.learner.WekaClassifier;

public class WekaClassifierTest {

	@Test
	public void testFit() throws TrainingException, PredictionException, FileNotFoundException, IOException, InterruptedException, DatasetDeserializationFailedException {
		WekaClassifier classifier = new WekaClassifier("weka.classifiers.trees.RandomForest", new String[] {});
		long timestampStart = System.currentTimeMillis();
		ILabeledDataset<ILabeledInstance> dataset = ArffDatasetAdapter.readDataset(new File("testrsc/dataset/arff/numeric_only_with_classindex.arff"));
		System.out.println((System.currentTimeMillis() - timestampStart));

		classifier.fit(dataset);
		ISingleLabelClassificationPredictionBatch yHat = classifier.predict(dataset);

		System.out.println(yHat);
	}

}
