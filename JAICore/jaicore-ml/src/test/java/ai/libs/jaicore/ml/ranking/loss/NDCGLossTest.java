package ai.libs.jaicore.ml.ranking.loss;

import java.util.Arrays;

import org.api4.java.ai.ml.ranking.IRanking;
import org.junit.jupiter.api.Test;

import ai.libs.jaicore.ml.ranking.label.learner.clusterbased.customdatatypes.Ranking;


public class NDCGLossTest {
	
	@Test
	public void testLoss() {
		IRanking<?> expected = new Ranking<>(Arrays.asList("A","B","C"));
		IRanking<?> actual = new Ranking<>(Arrays.asList("A","C","B"));
		System.out.println(new NDCGLoss(1).loss(expected, actual));
		System.out.println(new NDCGLoss(2).loss(expected, actual));
		System.out.println(new NDCGLoss(3).loss(expected, actual));
	}

}
