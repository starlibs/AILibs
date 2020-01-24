package ai.libs.mlplan.multilabel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileReader;
import java.util.List;
import java.util.Random;

import org.api4.java.ai.ml.core.evaluation.IPredictionBatch;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ai.libs.jaicore.ml.weka.WekaUtil;
import ai.libs.jaicore.ml.weka.classification.learner.IWekaClassifier;
import ai.libs.jaicore.ml.weka.classification.learner.WekaClassifier;
import ai.libs.jaicore.ml.weka.dataset.IWekaInstances;
import ai.libs.jaicore.ml.weka.dataset.WekaInstances;
import meka.classifiers.multilabel.BR;
import meka.core.MLUtils;
import weka.core.Instances;

public class MekaClassifierTest {
	private static List<IWekaInstances> datasetSplit;

	private IWekaClassifier classifier;

	@BeforeClass
	public static void setup() throws Exception {
		Instances data = new Instances(new FileReader("testrsc/flags.arff"));
		MLUtils.prepareData(data);
		IWekaInstances dataset = new WekaInstances(data);
		datasetSplit = WekaUtil.realizeSplit(dataset, WekaUtil.getArbitrarySplit(dataset, new Random(42), 0.7));
	}

	@Before
	public void init() {
		this.classifier = new WekaClassifier(new BR());
	}

	@Test
	public void testFit() throws Exception {
		this.classifier.fit(datasetSplit.get(0));
	}

	@Test
	public void testPredict() throws Exception {
		this.classifier.fit(datasetSplit.get(0));
		IPredictionBatch result = this.classifier.predict(datasetSplit.get(1));
		assertNotNull("The returned prediction was null", result);
		assertNotNull("The returned prediction batch has no entries.", result.get(0));
		assertEquals("The size of the prediction batch does not equal the number of test instances.", result.getNumPredictions(), datasetSplit.get(1).size());
		assertEquals("The number of label relevance scores does not match.", 12, result.get(0).getClassDistribution().size());
	}

	@Test
	public void testFitPredict() throws Exception {
		IPredictionBatch result = this.classifier.fitAndPredict(datasetSplit.get(0), datasetSplit.get(1));
		assertNotNull("The returned prediction was null", result);
		assertNotNull("The returned prediction batch has no entries.", result.get(0));
		assertEquals("The size of the prediction batch does not equal the number of test instances.", result.getNumPredictions(), datasetSplit.get(1).size());
		assertEquals("The number of label relevance scores does not match.", 12, result.get(0).getClassDistribution().size());
	}

	@Test
	public void testFailPredict() throws Exception {
		boolean exceptionOccurred = false;
		try {
			this.classifier.predict(datasetSplit.get(1));
		} catch (Exception e) {
			exceptionOccurred = true;
		}
		assertTrue("Could predict with classifier not yet fitted.", exceptionOccurred);
	}

}
