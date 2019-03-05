package jaicore.search.testproblems.knapsack;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.model.travesaltree.NodeExpansionDescription;
import jaicore.search.model.travesaltree.NodeType;
import jaicore.search.structure.graphgenerator.PathGoalTester;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

public class SimplePathProblemTester {

	@Test
	public void test() throws InterruptedException {

		GraphGenerator<Integer, Object> gen = new GraphGenerator<Integer, Object>() {

			@Override
			public SingleRootGenerator<Integer> getRootGenerator() {
				return () -> 0;
			}

			@Override
			public SuccessorGenerator<Integer, Object> getSuccessorGenerator() {
				return n -> {
					List<NodeExpansionDescription<Integer, Object>> succ = new ArrayList<>();
					succ.add(new NodeExpansionDescription<Integer, Object>(n, n * 2, null, NodeType.OR));
					succ.add(new NodeExpansionDescription<Integer, Object>(n, (n + 1) * 2, null, NodeType.OR));
					return succ;
				};
			}

			@Override
			public PathGoalTester<Integer> getGoalTester() {
				return p -> {
					int sum = 0;
					for (Integer i : p) {
						sum += i;
					}
					return sum > 0 && (sum % 100) == 0;
				};
			}

			@Override
			public boolean isSelfContained() {
				return false;
			}

			@Override
			public void setNodeNumbering(boolean nodenumbering) {
				// TODO Auto-generated method stub

			}
		};

//		BestFirst<Integer, Object, Double> bf = new BestFirst<>(gen, n -> 0.0);
		// new SimpleGraphVisualizationWindow<>(bf.getEventBus()).getPanel().setTooltipGenerator(n -> n.getPoint().toString());
//		assertNotNull(bf.nextSolution());

		// while (true);
	}
}
