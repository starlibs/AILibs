package ai.libs.jaicore.ml.classification.loss.instance;

import static org.junit.Assert.assertEquals;

import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassification;
import org.junit.jupiter.api.Test;

import ai.libs.jaicore.ml.classification.singlelabel.SingleLabelClassification;

public class LogLossTest {

	private static final ISingleLabelClassification PREDICTED = new SingleLabelClassification(new double[] { 0.0, 0.4, 1.0 });

	@Test
	public void testLogLoss() {
		LogLoss ll = new LogLoss();
		assertEquals("Log loss does not work as expected.", -Math.log(1E-15), ll.loss(0, PREDICTED), 1E-8);
		assertEquals("Log loss does not work as expected.", -Math.log(0.4), ll.loss(1, PREDICTED), 1E-8);
		assertEquals("Log loss does not work as expected.", -Math.log(1.0 - 1E-15), ll.loss(2, PREDICTED), 1E-8);
	}

}
