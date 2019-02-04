package jaicore.search.testproblems.nqueens;

import java.util.ArrayList;
import java.util.List;

import jaicore.basic.sets.SetUtil.Pair;
import jaicore.search.GraphGeneratorTester;
import jaicore.search.core.interfaces.GraphGenerator;

public class NQueenGraphGeneratorTester extends GraphGeneratorTester<QueenNode, String> {

	@Override
	public List<Pair<GraphGenerator<QueenNode, String>,Integer>> getGraphGenerators() {
		List<Pair<GraphGenerator<QueenNode, String>,Integer>> gg = new ArrayList<>();
		for (int n = 3; n <= 10; n++) {
			gg.add(new Pair<>(new NQueenGenerator(n), 100000));
		}
		return gg;
	}

}
