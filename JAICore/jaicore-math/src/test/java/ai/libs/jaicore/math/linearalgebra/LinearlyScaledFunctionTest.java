package ai.libs.jaicore.math.linearalgebra;

import static org.junit.Assert.assertEquals;

import java.util.function.DoubleFunction;

import org.junit.jupiter.api.Test;

public class LinearlyScaledFunctionTest {

	@Test
	public void test() {
		DoubleFunction<Double> fun = x -> Math.pow(x, 2);
		LinearlyScaledFunction scaledFun = new LinearlyScaledFunction(fun, 0, 0, 100, 1000);
		assertEquals(0.0, scaledFun.apply(0), 0.001);
		assertEquals(1000.0, scaledFun.apply(100), 0.001);

		for (int i = 0; i < 100; i++) {
			System.out.println(scaledFun.apply(i));
		}
	}

}
