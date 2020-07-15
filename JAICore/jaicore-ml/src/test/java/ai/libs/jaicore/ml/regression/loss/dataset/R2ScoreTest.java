package ai.libs.jaicore.ml.regression.loss.dataset;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.Test;

public class R2ScoreTest {

	@Test
	public void testScore() {
		List<Double> expected = Arrays.asList(2.0, 4.0, 5.0, 4.0, 5.0);
		List<Double> actual = Arrays.asList(2.8, 3.4, 4.0, 4.6, 5.2);
		R2 score = new R2();
		assertEquals("R2Score does not return the expected value.", 0.6, score.score(expected, actual), 1E-8);
	}

	@Test
	public void testRandomNumbers() {
		R2 score = new R2();
		Random r = new Random();
		int n = 20;

		for (int k = 0; k < 10; k++) {
			List<Double> expected = new ArrayList<>(n);
			List<Double> mean = new ArrayList<>(n);
			for (int i = 0; i < n; i++) {
				expected.add(100 * r.nextDouble());
			}
			for (int i = 0; i < n; i++) {
				mean.add(expected.stream().mapToDouble(x -> x).average().getAsDouble());
			}
			assertEquals("Predicting mean does not go to 0.", 0.0, score.score(expected, mean), 1E-8);
			assertEquals("Predicting expected does not go to 1.", 1.0, score.score(expected, expected), 1E-8);
		}
	}

}
