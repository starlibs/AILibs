package ai.libs.jaicore.basic.metric;

import static org.junit.Assert.assertEquals;

import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

public class ConfusionMetricsTest {

	private static final int[][] a = { { 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0 } };
	private static final int[][] b = { { 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0 } };

	private static final int[] tp = { 0 };
	private static final int[] fp = { 2 };
	private static final int[] tn = { 8 };
	private static final int[] fn = { 2 };

	private static final double[] precision = { 0.0 };
	private static final double[] recall = { 0.0 };
	private static final double[] f1score = { 0.0 };

	@Test
	public void testPrecision() {
		for (int i = 0; i < a.length; i++) {
			assertEquals("Precision not correct", precision[i], ConfusionMetrics.getPrecision(tp(a[i], b[i]), fp(a[i], b[i])), 1E-8);
		}
	}

	@Test
	public void testRecall() {
		for (int i = 0; i < a.length; i++) {
			assertEquals("Recall not correct", recall[i], ConfusionMetrics.getRecall(tp(a[i], b[i]), fn(a[i], b[i])), 1E-8);
		}
	}

	@Test
	public void testF1Score() {
		for (int i = 0; i < a.length; i++) {
			assertEquals("F1Score not correct", f1score[i], ConfusionMetrics.getF1Score(tp(a[i], b[i]), fp(a[i], b[i]), fn(a[i], b[i])), 1E-8);
		}
	}

	@Test
	public void testTP() {
		for (int i = 0; i < a.length; i++) {
			assertEquals("TP not correct", tp[i], tp(a[i], b[i]));
		}
	}

	@Test
	public void testFP() {
		for (int i = 0; i < a.length; i++) {
			assertEquals("FP not correct", fp[i], fp(a[i], b[i]));
		}
	}

	@Test
	public void testTN() {
		for (int i = 0; i < a.length; i++) {
			assertEquals("TN not correct", tn[i], tn(a[i], b[i]));
		}
	}

	@Test
	public void testFN() {
		for (int i = 0; i < a.length; i++) {
			assertEquals("FN not correct", fn[i], fn(a[i], b[i]));
		}
	}

	private static int tp(final int[] x, final int[] y) {
		return (int) IntStream.range(0, x.length).filter(i -> x[i] == 1 && x[i] == y[i]).count();
	}

	private static int fp(final int[] x, final int[] y) {
		return (int) IntStream.range(0, x.length).filter(i -> x[i] == 0 && x[i] != y[i]).count();
	}

	private static int tn(final int[] x, final int[] y) {
		return (int) IntStream.range(0, x.length).filter(i -> x[i] == 0 && x[i] == y[i]).count();
	}

	private static int fn(final int[] x, final int[] y) {
		return (int) IntStream.range(0, x.length).filter(i -> x[i] == 1 && x[i] != y[i]).count();
	}
}
