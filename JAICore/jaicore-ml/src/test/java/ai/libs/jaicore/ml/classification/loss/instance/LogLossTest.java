package ai.libs.jaicore.ml.classification.loss.instance;

import org.junit.Test;

public class LogLossTest {

	@Test
	public void testLogLoss() {
		LogLoss ll = new LogLoss();
		System.out.println(ll.loss(2, new double[] { 0.0, 0.1, 0.0, 0.03 }));
	}

}
