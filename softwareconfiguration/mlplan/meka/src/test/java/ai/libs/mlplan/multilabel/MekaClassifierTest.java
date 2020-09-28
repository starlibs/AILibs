package ai.libs.mlplan.multilabel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileReader;
import java.util.List;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.evaluation.IPredictionBatch;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ai.libs.jaicore.ml.classification.multilabel.dataset.IMekaInstances;
import ai.libs.jaicore.ml.classification.multilabel.dataset.MekaInstances;
import ai.libs.jaicore.ml.classification.multilabel.learner.IMekaClassifier;
import ai.libs.jaicore.ml.classification.multilabel.learner.MekaClassifier;
import ai.libs.jaicore.ml.core.filter.SplitterUtil;
import meka.classifiers.multilabel.BR;
import meka.core.MLUtils;
import weka.core.Instances;

public class MekaClassifierTest {
	private static List<ILabeledDataset<?>> datasetSplit;

	private IMekaClassifier classifier;

	@BeforeAll
	public static void setup() throws Exception {
		Instances data = new Instances(new FileReader("testrsc/flags.arff"));
		MLUtils.prepareData(data);
		IMekaInstances dataset = new MekaInstances(data);

		datasetSplit = SplitterUtil.getSimpleTrainTestSplit(dataset, 0, 0.7);
	}

	@BeforeEach
	public void init() {
		this.classifier = new MekaClassifier(new BR());
	}

	@Test
	public void testFit() throws Exception {
		this.classifier.fit(datasetSplit.get(0));
	}

	@Disabled
	@Test
	public void testPredict() throws Exception {
		this.classifier.fit(datasetSplit.get(0));
		IPredictionBatch result = this.classifier.predict(datasetSplit.get(1));
		assertNotNull(result, "The returned prediction was null");
		assertNotNull(result.get(0), "The returned prediction batch has no entries.");
		assertEquals(result.getNumPredictions(), datasetSplit.get(1).size(), "The size of the prediction batch does not equal the number of test instances.");
		assertEquals(12, result.get(0).getClassDistribution().size(), "The number of label relevance scores does not match.");
	}

	@Disabled
	@Test
	public void testFitPredict() throws Exception {
		IPredictionBatch result = this.classifier.fitAndPredict(datasetSplit.get(0), datasetSplit.get(1));
		assertNotNull(result, "The returned prediction was null");
		assertNotNull(result.get(0), "The returned prediction batch has no entries.");
		assertEquals(result.getNumPredictions(), datasetSplit.get(1).size(), "The size of the prediction batch does not equal the number of test instances.");
		assertEquals(12, result.get(0).getClassDistribution().size(), "The number of label relevance scores does not match.");
	}

	@Disabled
	@Test
	public void testFailPredict() throws Exception {
		boolean exceptionOccurred = false;
		try {
			this.classifier.predict(datasetSplit.get(1));
		} catch (Exception e) {
			exceptionOccurred = true;
		}
		assertTrue(exceptionOccurred, "Could predict with classifier not yet fitted.");
	}

}
