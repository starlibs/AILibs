package ai.libs.jaicore.search.testproblems.nqueens;

import java.util.ArrayList;
import java.util.List;

import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.NodeExpansionDescription;
import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.NodeGoalTester;
import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.NodeType;
import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.SerializableGraphGenerator;
import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.SingleRootGenerator;
import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.SuccessorGenerator;

@SuppressWarnings("serial")
public class NQueensGraphGenerator implements SerializableGraphGenerator<QueenNode, String> {

	private final int dimension;
	private int countSinceLastSleep = 0;

	public NQueensGraphGenerator(final int dimension) {
		this.dimension = dimension;
	}

	@Override
	public SingleRootGenerator<QueenNode> getRootGenerator() {
		return () -> new QueenNode(this.dimension);
	}

	@Override
	public SuccessorGenerator<QueenNode, String> getSuccessorGenerator() {
		return n -> {
			List<NodeExpansionDescription<QueenNode, String>> l = new ArrayList<>();
			int currentRow = n.getPositions().size();
			for (int i = 0; i < this.dimension; i++, this.countSinceLastSleep ++) {
				if (this.countSinceLastSleep % 100 == 0) {
					Thread.sleep(5);
				}
				if (Thread.interrupted()) {
					throw new InterruptedException("Successor generation has been interrupted.");
				}
				if (!n.attack(currentRow, i)) {
					l.add(new NodeExpansionDescription<>(new QueenNode(n, i), "" + i, NodeType.OR));
				}
			}
			return l;
		};
	}

	@Override
	public NodeGoalTester<QueenNode, String> getGoalTester() {
		return n -> n.getNumberOfQueens() == this.dimension;
	}
}
