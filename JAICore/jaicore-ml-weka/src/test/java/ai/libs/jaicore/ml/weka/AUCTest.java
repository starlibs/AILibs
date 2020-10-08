package ai.libs.jaicore.ml.weka;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassification;
import org.junit.jupiter.api.Test;

import ai.libs.jaicore.ml.classification.loss.dataset.AreaUnderPrecisionRecallCurve;
import ai.libs.jaicore.ml.classification.loss.dataset.AreaUnderROCCurve;
import ai.libs.jaicore.ml.classification.singlelabel.SingleLabelClassification;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.NominalPrediction;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.trees.J48;
import weka.core.Instances;

public class AUCTest {

	@Test
	public void testAUROCPerformanceMeasure() throws Exception {
		Instances data = new Instances(new FileReader(new File("testrsc/dataset/arff/breast.cancer.arff")));
		data.setClassIndex(data.numAttributes() - 1);

		int seeds = 20;
		List<Long> timeNeededWEKA = new ArrayList<>(seeds);
		List<Long> timeNeededJAICore = new ArrayList<>(seeds);

		long timestamp;
		long timeNeeded;
		for (int i = 0; i < seeds; i++) {
			J48 m = new J48();
			Evaluation eval = new Evaluation(data);
			eval.crossValidateModel(m, data, 5, new Random(i));
			timestamp = System.currentTimeMillis();
			double wekaAUC = eval.areaUnderROC(0);
			double wekaAUPRC = eval.areaUnderPRC(0);
			timeNeeded = System.currentTimeMillis() - timestamp;
			timeNeededWEKA.add(timeNeeded);

			List<Integer> expected = new ArrayList<>();
			List<ISingleLabelClassification> predicted = new ArrayList<>();

			for (Prediction p : eval.predictions()) {
				expected.add((int) p.actual());
				predicted.add(new SingleLabelClassification(((NominalPrediction) p).distribution()));
			}

			AreaUnderROCCurve auc = new AreaUnderROCCurve(0);
			AreaUnderPrecisionRecallCurve auprc = new AreaUnderPrecisionRecallCurve(0);
			timestamp = System.currentTimeMillis();

			double jaicoreAUC = auc.score(expected, predicted);
			double jaicoreAUPRC = auprc.score(expected, predicted);
			timeNeeded = System.currentTimeMillis() - timestamp;
			timeNeededJAICore.add(timeNeeded);

			assertEquals("AUC ROC is not equal", wekaAUC, jaicoreAUC, 1E-8);
			assertEquals("AUPRC is not equal", wekaAUPRC, jaicoreAUPRC, 5E-2);
		}
	}

}
