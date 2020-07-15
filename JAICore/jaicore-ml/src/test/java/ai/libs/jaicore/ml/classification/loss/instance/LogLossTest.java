package ai.libs.jaicore.ml.classification.loss.instance;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LogLossTest {

	private static final double[] SCORES = { 0.0, 0.4, 1.0 };

	@Test
	public void testLogLoss() {
		LogLoss ll = new LogLoss();
		assertEquals("Log loss does not work as expected.", -Math.log(1E-15), ll.loss(0, SCORES), 1E-8);
		assertEquals("Log loss does not work as expected.", -Math.log(0.4), ll.loss(1, SCORES), 1E-8);
		assertEquals("Log loss does not work as expected.", -Math.log(1.0 - 1E-15), ll.loss(2, SCORES), 1E-8);
	}

}
