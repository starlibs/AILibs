package ai.libs.jaicore.search.testproblems.nqueens;

import java.util.ArrayList;
import java.util.List;

import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.IGraphGenerator;

import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.search.GraphGeneratorTester;

public class NQueensGraphGeneratorTester extends GraphGeneratorTester<QueenNode, String> {

	@Override
	public List<Pair<IGraphGenerator<QueenNode, String>,Integer>> getGraphGenerators() {
		List<Pair<IGraphGenerator<QueenNode, String>,Integer>> gg = new ArrayList<>();
		for (int n = 3; n <= 10; n++) {
			gg.add(new Pair<>(new NQueensGraphGenerator(n), 100000));
		}
		return gg;
	}

}
