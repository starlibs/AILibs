package ai.libs.jaicore.ml.classification.multilabel.evaluation.loss;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.api4.java.ai.ml.classification.multilabel.evaluation.IMultiLabelClassification;
import org.junit.jupiter.api.Test;

import ai.libs.jaicore.ml.classification.multilabel.MultiLabelClassification;

public class MultilabelMeasureTest {

	private static final double DELTA = 1E-8;

	private static final int[][] EXPECTEDS = { { 1, 0, 1, 1, 0, 1, 1 }, { 1, 0, 1, 0, 0, 1, 0 } };
	private static final int[][] ACTUALS = { { 0, 1, 1, 1, 1, 0, 1 }, { 1, 0, 1, 1, 0, 0, 1 } };

	private static final double EXPECTED_IF1 = 0.5857142857142857;
	private static final double EXPECTED_LF1 = 0.4285714285714285;
	private static final double EXPECTED_HAM = 0.5;

	private List<IMultiLabelClassification> toClassifications(final int[][] values) {
		List<IMultiLabelClassification> list = new ArrayList<>();
		for (int i = 0; i < values.length; i++) {
			list.add(new MultiLabelClassification(Arrays.stream(values[i]).mapToDouble(x -> x).toArray()));
		}
		return list;
	}

	@Test
	public void testInstanceWiseF1Measure() {
		assertEquals("Instance-Wise F1 Measure is not as expected.", EXPECTED_IF1, new InstanceWiseF1().score(Arrays.stream(EXPECTEDS).collect(Collectors.toList()), this.toClassifications(ACTUALS)), DELTA);
	}

	@Test
	public void testLabelWiseF1Measure() {
		assertEquals("Label-Wise F1 Measure is not as expected.", EXPECTED_LF1, new F1MacroAverageL().score(Arrays.stream(EXPECTEDS).collect(Collectors.toList()), this.toClassifications(ACTUALS)), DELTA);
	}

	public void testHamming() {
		assertEquals("Hamming is not as expected.", EXPECTED_HAM, new Hamming().loss(Arrays.stream(EXPECTEDS).collect(Collectors.toList()), this.toClassifications(ACTUALS)), DELTA);
	}
}
