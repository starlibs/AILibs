package ai.libs.jaicore.ml.classification.multilabel.evaluation.loss.nonadditive;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.api4.java.ai.ml.classification.multilabel.evaluation.IMultiLabelClassification;
import org.junit.Test;

import ai.libs.jaicore.ml.classification.multilabel.MultiLabelClassification;
import ai.libs.jaicore.ml.classification.multilabel.evaluation.loss.nonadditive.owa.MoebiusTransformOWAValueFunction;
import ai.libs.jaicore.ml.classification.multilabel.evaluation.loss.nonadditive.owa.PolynomialOWAValueFunction;

public class OWARelevanceLossTest {
	private static final double DELTA = 1E-8;

	private static final int[][] EXPECTEDS = { { 1, 0, 1, 1, 0, 1, 1 }, { 1, 0, 1, 0, 0, 1, 0 }, { 0, 1, 0, 0, 1, 1, 0 } };
	private static final int[][] ACTUALS = { { 1, 0, 1, 1, 0, 1, 1 }, { 1, 0, 1, 1, 0, 0, 1 }, { 1, 0, 1, 1, 0, 0, 0 } };
	private static final double EXPECTED_HAM = 0.428571428;
	private static final double EXPECTED_S01 = 2.0 / 3;

	private List<IMultiLabelClassification> toClassifications(final int[][] values) {
		List<IMultiLabelClassification> list = new ArrayList<>();
		for (int i = 0; i < values.length; i++) {
			list.add(new MultiLabelClassification(Arrays.stream(values[i]).mapToDouble(x -> x).toArray()));
		}
		return list;
	}

	@Test
	public void testHammingOWALoss() {
		OWARelevanceLoss loss = new OWARelevanceLoss(new PolynomialOWAValueFunction(1));
		double owaLoss = loss.loss(Arrays.stream(EXPECTEDS).collect(Collectors.toList()), this.toClassifications(ACTUALS));
		assertEquals("Hamming loss differs", EXPECTED_HAM, owaLoss, DELTA);
	}

	@Test
	public void testSubsetZeroOneOWALoss() {
		OWARelevanceLoss loss = new OWARelevanceLoss(new PolynomialOWAValueFunction(1E10));
		double owaLoss = loss.loss(Arrays.stream(EXPECTEDS).collect(Collectors.toList()), this.toClassifications(ACTUALS));
		assertEquals("Subset 0/1 loss differs", EXPECTED_S01, owaLoss, DELTA);
	}

	@Test
	public void testSubsetZeroOneMoebiusTransformOWALoss() {
		OWARelevanceLoss loss = new OWARelevanceLoss(new MoebiusTransformOWAValueFunction(EXPECTEDS[0].length));
		double owaLoss = loss.loss(Arrays.stream(EXPECTEDS).collect(Collectors.toList()), this.toClassifications(ACTUALS));
		assertEquals("Subset 0/1 loss differs", EXPECTED_S01, owaLoss, DELTA);
	}

}
