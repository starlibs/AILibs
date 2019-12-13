package ai.libs.jaicore.ml.classification.multilabel;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;

public class MultiLabelClassificationTest {

	private static final double DOUBLE_DELTA = 1E-8;

	private static final double[] GT_VECTOR = { 0.231232, 0.84354239, 0.1234782394, 0.3423489 };
	private static final double THRESHOLD_SINGLE = 0.3;
	private static final double[] THRESHOLD_VECTOR = { 0.2, 0.9, 0.1, 0.4 };

	private static final int[] EXPECTED_THRESHOLDED_SINGLE = { 0, 1, 0, 1 };
	private static final int[] EXPECTED_THRESHOLDED_VECTOR = { 1, 0, 1, 0 };

	private static final double RELEVANCE_THRESHOLD = 0.5;
	private static final int[] RELEVANT_LABELS = { 1 };
	private static final int[] IRRELEVANT_LABELS = { 0, 2, 3 };

	private static MultiLabelClassification classification;

	@BeforeClass
	public static void setup() {
		classification = new MultiLabelClassification(GT_VECTOR);
	}

	@Test
	public void testGetPrediction() {
		double[] pred = classification.getPrediction();
		assertEquals("Prediction vector and ground truth value vector are not of the same length", GT_VECTOR.length, pred.length);
		for (int i = 0; i < GT_VECTOR.length; i++) {
			assertEquals("Prediction vector is not as expected for index " + i + ".", GT_VECTOR[i], pred[i], DOUBLE_DELTA);
		}
	}

	@Test
	public void testGetThresholdedLabelRelevanceVector() {
		int[] pred = classification.getPrediction(THRESHOLD_SINGLE);
		assertEquals("Prediction vector and ground truth value vector are not of the same length", EXPECTED_THRESHOLDED_SINGLE.length, pred.length);
		for (int i = 0; i < EXPECTED_THRESHOLDED_SINGLE.length; i++) {
			assertEquals("Prediction vector is not as expected for index " + i + ".", EXPECTED_THRESHOLDED_SINGLE[i], pred[i], DOUBLE_DELTA);
		}
	}

	@Test
	public void testGetLabelWiseThresholdedLabelRelevanceVector() {
		int[] pred = classification.getPrediction(THRESHOLD_VECTOR);
		assertEquals("Prediction vector and ground truth value vector are not of the same length", EXPECTED_THRESHOLDED_VECTOR.length, pred.length);

		for (int i = 0; i < EXPECTED_THRESHOLDED_VECTOR.length; i++) {
			assertEquals("Prediction vector is not as expected for index " + i + ".", EXPECTED_THRESHOLDED_VECTOR[i], pred[i], DOUBLE_DELTA);
		}
	}

	@Test
	public void getRelevantLabels() {
		int[] relevantLabels = classification.getRelevantLabels(0.5);
		assertEquals("Prediction vector and ground truth value vector are not of the same length", RELEVANT_LABELS.length, relevantLabels.length);
		for (int i = 0; i < RELEVANT_LABELS.length; i++) {
			assertEquals("Relevant labels do not match. Expected: " + Arrays.toString(RELEVANT_LABELS) + " actual: " + Arrays.toString(relevantLabels), RELEVANT_LABELS[i], relevantLabels[i]);
		}
	}

	@Test
	public void getIrrelevantLabels() {
		int[] irrelevantLabels = classification.getIrrelevantLabels(0.5);
		assertEquals("Prediction vector and ground truth value vector are not of the same length", IRRELEVANT_LABELS.length, irrelevantLabels.length);
		for (int i = 0; i < IRRELEVANT_LABELS.length; i++) {
			assertEquals("Relevant labels do not match. Expected: " + Arrays.toString(IRRELEVANT_LABELS) + " actual: " + Arrays.toString(irrelevantLabels), IRRELEVANT_LABELS[i], irrelevantLabels[i]);
		}
	}

}
