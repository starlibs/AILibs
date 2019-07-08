import java.util.ArrayList;
import java.util.List;

import ai.libs.jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import ai.libs.jaicore.basic.algorithm.exceptions.AlgorithmException;
import ai.libs.jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;
import ai.libs.jaicore.search.algorithms.standard.random.RandomSearch;
import ai.libs.jaicore.search.core.interfaces.GraphGenerator;
import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.model.travesaltree.NodeExpansionDescription;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;
import ai.libs.jaicore.search.structure.graphgenerator.GoalTester;
import ai.libs.jaicore.search.structure.graphgenerator.NodeGoalTester;
import ai.libs.jaicore.search.structure.graphgenerator.RootGenerator;
import ai.libs.jaicore.search.structure.graphgenerator.SingleRootGenerator;
import ai.libs.jaicore.search.structure.graphgenerator.SuccessorGenerator;

public class SearchTest {

	static GraphGenerator<Integer, Object> gg = new GraphGenerator<Integer, Object>() {

		@Override
		public RootGenerator<Integer> getRootGenerator() {
			return (SingleRootGenerator<Integer>)(() -> 0);
		}

		@Override
		public SuccessorGenerator<Integer, Object> getSuccessorGenerator() {
			return i -> {
				List<NodeExpansionDescription<Integer, Object>> successors = new ArrayList<>();
				successors.add(new NodeExpansionDescription<>(i * 2 + 1));
				successors.add(new NodeExpansionDescription<>(i * 2 + 2));
				return successors;
			};
		}

		@Override
		public GoalTester<Integer> getGoalTester() {
			return (NodeGoalTester<Integer>)(n -> n == 42 || n > 100);
		}
	};

	public static void main(final String[] args) throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		GraphSearchInput<Integer, Object> input = new GraphSearchInput<>(gg);
		RandomSearch<Integer, Object> rs = new RandomSearch<>(input);
		rs.setLoggerName("testedalgorithm");
		SearchGraphPath<Integer, Object> solution = rs.call();
		System.out.println(solution);

	}
}
