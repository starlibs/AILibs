package ai.libs.jaicore.search.testproblems.nqueens;

import java.util.ArrayList;
import java.util.List;

import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.search.GraphGeneratorTester;
import ai.libs.jaicore.search.core.interfaces.GraphGenerator;

public class NQueensGraphGeneratorTester extends GraphGeneratorTester<QueenNode, String> {

	@Override
	public List<Pair<GraphGenerator<QueenNode, String>,Integer>> getGraphGenerators() {
		List<Pair<GraphGenerator<QueenNode, String>,Integer>> gg = new ArrayList<>();
		for (int n = 3; n <= 10; n++) {
			gg.add(new Pair<>(new NQueensGraphGenerator(n), 100000));
		}
		return gg;
	}

}
