package ai.libs.jaicore.ml.classification.multilabel.learner;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileReader;
import java.util.List;

import org.api4.java.ai.ml.classification.multilabel.evaluation.IMultiLabelClassificationPredictionBatch;
import org.junit.BeforeClass;
import org.junit.Test;

import ai.libs.jaicore.ml.weka.WekaUtil;
import ai.libs.jaicore.ml.weka.dataset.IWekaInstances;
import ai.libs.jaicore.ml.weka.dataset.WekaInstances;
import meka.classifiers.multilabel.BR;
import meka.classifiers.multilabel.Evaluation;
import meka.core.MLUtils;
import meka.core.Result;
import weka.core.Instances;

public class MekaClassifierTest {

	private static List<IWekaInstances> split;

	@BeforeClass
	public static void setup() throws Exception {
		Instances data = new Instances(new FileReader(new File("testrsc/flags.arff")));
		MLUtils.prepareData(data);
		split = WekaUtil.getStratifiedSplit(new WekaInstances(data), 42, .7);
	}

	@Test
	public void testFitAndPredict() throws Exception {
		BR br = new BR();
		br.buildClassifier(split.get(0).getInstances());
		Result res = Evaluation.testClassifier(br, split.get(1).getInstances());
		double[][] mekaPredictions = res.allPredictions();

		MekaClassifier classifier = new MekaClassifier(new BR());
		classifier.fit(split.get(0));
		IMultiLabelClassificationPredictionBatch pred = classifier.predict(split.get(1));

		assertEquals("Number of predictions is not consistent.", split.get(1).size(), pred.getNumPredictions());

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
