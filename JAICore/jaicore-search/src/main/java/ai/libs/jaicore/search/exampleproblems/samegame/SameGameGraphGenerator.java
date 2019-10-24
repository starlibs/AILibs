package ai.libs.jaicore.search.exampleproblems.samegame;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.api4.java.datastructure.graph.implicit.IGraphGenerator;
import org.api4.java.datastructure.graph.implicit.NodeExpansionDescription;
import org.api4.java.datastructure.graph.implicit.NodeType;
import org.api4.java.datastructure.graph.implicit.SingleRootGenerator;
import org.api4.java.datastructure.graph.implicit.SuccessorGenerator;

import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.problems.samegame.SameGameState;

public class SameGameGraphGenerator implements IGraphGenerator<SameGameState, Pair<Integer, Integer>> {

	private final SameGameState initState;

	public SameGameGraphGenerator(final SameGameState initState) {
		super();
		this.initState = initState;
	}

	@Override
	public SingleRootGenerator<SameGameState> getRootGenerator() {
		return () -> this.initState;
	}

	@Override
	public SuccessorGenerator<SameGameState, Pair<Integer, Integer>> getSuccessorGenerator() {
		return n -> {
			List<NodeExpansionDescription<SameGameState, Pair<Integer, Integer>>> succ = new ArrayList<>();
			for (Collection<Pair<Integer, Integer>> block : n.getBlocksOfPieces()) {
				if (block.size() > 1) {
					succ.add(new NodeExpansionDescription<>(n.getStateAfterMove(block), block.iterator().next(), NodeType.OR));
				}
			}
			return succ;
		};
	}
}
