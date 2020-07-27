package ai.libs.mlplan.multilabel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileReader;
import java.util.List;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.evaluation.IPredictionBatch;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

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

	@BeforeClass
	public static void setup() throws Exception {
		Instances data = new Instances(new FileReader("testrsc/flags.arff"));
		MLUtils.prepareData(data);
		IMekaInstances dataset = new MekaInstances(data);

		datasetSplit = SplitterUtil.getSimpleTrainTestSplit(dataset, 0, 0.7);
	}

	@Before
	public void init() {
		this.classifier = new MekaClassifier(new BR());
	}

	@Test
	public void testFit() throws Exception {
		this.classifier.fit(datasetSplit.get(0));
	}

	@Ignore
	@Test
	public void testPredict() throws Exception {
		this.classifier.fit(datasetSplit.get(0));
		IPredictionBatch result = this.classifier.predict(datasetSplit.get(1));
		assertNotNull("The returned prediction was null", result);
		assertNotNull("The returned prediction batch has no entries.", result.get(0));
		assertEquals("The size of the prediction batch does not equal the number of test instances.", result.getNumPredictions(), datasetSplit.get(1).size());
		assertEquals("The number of label relevance scores does not match.", 12, result.get(0).getClassDistribution().size());
	}

	@Ignore
	@Test
	public void testFitPredict() throws Exception {
		IPredictionBatch result = this.classifier.fitAndPredict(datasetSplit.get(0), datasetSplit.get(1));
		assertNotNull("The returned prediction was null", result);
		assertNotNull("The returned prediction batch has no entries.", result.get(0));
		assertEquals("The size of the prediction batch does not equal the number of test instances.", result.getNumPredictions(), datasetSplit.get(1).size());
		assertEquals("The number of label relevance scores does not match.", 12, result.get(0).getClassDistribution().size());
	}

	@Ignore
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
