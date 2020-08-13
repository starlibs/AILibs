package ai.libs.jaicore.basic;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MathExtTest {

	@Test
	public void testLogBase() {
		assertEquals(3.0, MathExt.logBase(8.0, 2.0), 1E-8);
		assertEquals(3.0, MathExt.logBase(27.0, 3.0), 1E-8);
		assertEquals(4.0, MathExt.logBase(256.0, 4.0), 1E-8);
	}

}
