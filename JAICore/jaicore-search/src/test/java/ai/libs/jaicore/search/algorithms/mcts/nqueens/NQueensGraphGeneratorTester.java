package ai.libs.jaicore.search.algorithms.mcts.nqueens;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;

import ai.libs.jaicore.search.GraphGeneratorTester;
import ai.libs.jaicore.search.exampleproblems.nqueens.QueenNode;

public class NQueensGraphGeneratorTester extends GraphGeneratorTester<QueenNode, String> {

	public static Stream<Arguments> getGraphGenerators() {
		List<Arguments> gg = new ArrayList<>();
		for (int n = 3; n <= 10; n++) {
			gg.add(Arguments.of(n + "-Queens", new NQueensGraphGenerator(n)));
		}
		return gg.stream();
	}
}
