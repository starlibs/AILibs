package autofe.util.test;

import org.junit.Test;

import autofe.util.EvaluationUtils;
import junit.framework.Assert;

public class EvaluationUtilsTest {
	@Test
	public void rankKendallsTauTest() {

		// double[] ranking1 = { 0.15, 0.1, 0.2 };
		double[] ranking1 = { 0.15, 0.3 };
		// double[] ranking2 = { 0.1, 0.05, 1 };
		double[] ranking2 = { 0.1, 0.4 };

		Assert.assertEquals(1.0, EvaluationUtils.rankKendallsTau(ranking1, ranking2));
	}
}
