package ai.libs.jaicore.ml.classification.multilabel.learner;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileReader;
import java.util.List;

import org.api4.java.ai.ml.classification.multilabel.evaluation.IMultiLabelClassificationPredictionBatch;
import org.junit.BeforeClass;
import org.junit.Test;

import ai.libs.jaicore.ml.classification.multilabel.dataset.IMekaInstances;
import ai.libs.jaicore.ml.classification.multilabel.dataset.MekaInstances;
import ai.libs.jaicore.ml.core.dataset.splitter.RandomHoldoutSplitter;
import meka.classifiers.multilabel.BR;
import meka.classifiers.multilabel.Evaluation;
import meka.core.MLUtils;
import meka.core.Result;
import weka.core.Instances;

public class MekaClassifierTest {

	private static IMekaInstances dataset;
	private static List<IMekaInstances> splitterSplit;

	@BeforeClass
	public static void setup() throws Exception {
		Instances data = new Instances(new FileReader(new File("testrsc/flags.arff")));
		MLUtils.prepareData(data);
		dataset = new MekaInstances(data);
		splitterSplit = RandomHoldoutSplitter.createSplit(dataset, 42, .7);
	}

	@Test
	public void testFitAndPredictWithHoldoutSplitter() throws Exception {
		BR br = new BR();
		br.buildClassifier(splitterSplit.get(0).getInstances());
		Result res = Evaluation.testClassifier(br, splitterSplit.get(1).getInstances());
		double[][] mekaPredictions = res.allPredictions();

		MekaClassifier classifier = new MekaClassifier(new BR());
		classifier.fit(splitterSplit.get(0));
		IMultiLabelClassificationPredictionBatch pred = classifier.predict(splitterSplit.get(1));

		assertEquals("Number of predictions is not consistent.", splitterSplit.get(1).size(), pred.getNumPredictions());

		double[][] jaicorePredictions = pred.getPredictionMatrix();
		assertEquals("Length of prediction matrices is not consistent.", mekaPredictions.length, jaicorePredictions.length);
		assertEquals("Width of prediction matrices is not consistent.", mekaPredictions[0].length, jaicorePredictions[0].length);

		for (int i = 0; i < mekaPredictions.length; i++) {
			for (int j = 0; j < mekaPredictions[i].length; j++) {
				assertEquals("The prediction for instance " + i + " and label " + j + " is not consistent.", mekaPredictions[i][j], jaicorePredictions[i][j], 1E-8);
			}
		}
	}

}
