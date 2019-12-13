package ai.libs.jaicore.ml.classification.multilabel;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.api4.java.ai.ml.classification.multilabel.evaluation.IMultiLabelClassification;
import org.api4.java.ai.ml.classification.multilabel.evaluation.IMultiLabelClassificationPredictionBatch;
import org.junit.BeforeClass;
import org.junit.Test;

public class MultiLabelClassificationPredictionBatchTest {

	private static final double DOUBLE_DELTA = 1E-8;

	private static final double[][] GT_MATRIX = { { 0.231232, 0.84354239, 0.1234782394, 0.3423489 }, { 0.3, 0.4, 0.5, 0.6 } };
	private static final double THRESHOLD_SINGLE = 0.3;
	private static final double[] THRESHOLD_VECTOR = { 0.2, 0.9, 0.1, 0.4 };

	private static final int[][] EXPECTED_THRESHOLDED_SINGLE = { { 0, 1, 0, 1 }, { 1, 1, 1, 1 } };
	private static final int[][] EXPECTED_THRESHOLDED_VECTOR = { { 1, 0, 1, 0 }, { 1, 0, 1, 1 } };

	private static final double RELEVANCE_THRESHOLD = 0.5;
	private static final int[] RELEVANT_LABELS = { 1 };
	private static final int[] IRRELEVANT_LABELS = { 0, 2, 3 };

	private static IMultiLabelClassificationPredictionBatch classification;

	@BeforeClass
	public static void setup() {
		List<IMultiLabelClassification> preds = new ArrayList<>(GT_MATRIX.length);
		for (int i = 0; i < GT_MATRIX.length; i++) {
			preds.add(new MultiLabelClassification(GT_MATRIX[i]));
		}
		classification = new MultiLabelClassificationPredictionBatch(preds);
	}

	@Test
	public void testGetPrediction() {
		double[][] pred = classification.getPredictionMatrix();
		assertEquals("Prediction vector and ground truth value vector are not of the same length", GT_MATRIX.length, pred.length);
		for (int i = 0; i < GT_MATRIX.length; i++) {
			for (int j = 0; j < GT_MATRIX[i].length; j++) {
				assertEquals("Prediction vector is not as expected for index " + i + ".", GT_MATRIX[i][j], pred[i][j], DOUBLE_DELTA);
			}
		}
	}

	@Test
	public void testGetThresholdedLabelRelevanceVector() {
		int[][] pred = classification.getThresholdedPredictionMatrix(THRESHOLD_SINGLE);
		assertEquals("Prediction vector and ground truth value vector are not of the same length", EXPECTED_THRESHOLDED_SINGLE.length, pred.length);
		for (int i = 0; i < EXPECTED_THRESHOLDED_SINGLE.length; i++) {
			for (int j = 0; j < EXPECTED_THRESHOLDED_SINGLE[i].length; j++) {
				assertEquals("Prediction vector is not as expected for index " + i + ":" + j + ".", EXPECTED_THRESHOLDED_SINGLE[i][j], pred[i][j], DOUBLE_DELTA);
			}
		}
	}

	@Test
	public void testGetLabelWiseThresholdedLabelRelevanceVector() {
		int[][] pred = classification.getThresholdedPredictionMatrix(THRESHOLD_VECTOR);
		assertEquals("Prediction vector and ground truth value vector are not of the same length", EXPECTED_THRESHOLDED_VECTOR.length, pred.length);

		for (int i = 0; i < EXPECTED_THRESHOLDED_VECTOR.length; i++) {
			for (int j = 0; j < EXPECTED_THRESHOLDED_SINGLE[i].length; j++) {
				assertEquals("Prediction vector is not as expected for index " + i + ":" + j + ".", EXPECTED_THRESHOLDED_VECTOR[i][j], pred[i][j], DOUBLE_DELTA);
			}
		}
	}

}
