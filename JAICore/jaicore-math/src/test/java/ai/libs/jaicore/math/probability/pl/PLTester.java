package ai.libs.jaicore.math.probability.pl;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.junit.Test;

public class PLTester {

	@Test
	public void test() throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {

		/* create rankings */
		Random random = new Random(0);
		List<String> objects = Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M");
		List<List<String>> rankings = new ArrayList<>();
		for (int i = 0; i < 1000; i++) {
			int splitIndex = random.nextInt(objects.size());
			List<String> firstHalf = new ArrayList<>(objects.subList(0, splitIndex));
			List<String> secondHalf = new ArrayList<>(objects.subList(splitIndex, objects.size()));
			Collections.shuffle(firstHalf, random);
			Collections.shuffle(secondHalf, random);
			firstHalf.addAll(secondHalf);
			rankings.add(firstHalf);
		}

		/* create PL problem */
		PLInferenceProblemEncoder encoder = new PLInferenceProblemEncoder();
		PLInferenceProblem prob = encoder.encode(rankings);
		PLMMAlgorithm algo = new PLMMAlgorithm(prob);
		assertNotNull(algo.call());
	}
}
