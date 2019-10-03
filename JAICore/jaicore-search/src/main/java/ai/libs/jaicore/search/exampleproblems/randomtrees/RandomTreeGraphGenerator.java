package ai.libs.jaicore.search.exampleproblems.randomtrees;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.api4.java.datastructure.graph.implicit.IGraphGenerator;
import org.api4.java.datastructure.graph.implicit.NodeExpansionDescription;
import org.api4.java.datastructure.graph.implicit.NodeType;
import org.api4.java.datastructure.graph.implicit.SingleRootGenerator;
import org.api4.java.datastructure.graph.implicit.SuccessorGenerator;

public class RandomTreeGraphGenerator implements IGraphGenerator<List<Integer>, Integer> {

	private final int b;
	private final int d;
	private final long seed;
	private final int maxPerDepth;

	public RandomTreeGraphGenerator(final int b, final int d, final long seed, final int maxPerDepth) {
		super();
		this.b = b;
		this.d = d;
		this.seed = seed;
		this.maxPerDepth = maxPerDepth;
	}

	@Override
	public SingleRootGenerator<List<Integer>> getRootGenerator() {
		return () -> Arrays.asList();
	}

	@Override
	public SuccessorGenerator<List<Integer>, Integer> getSuccessorGenerator() {
		return n -> {
			List<NodeExpansionDescription<List<Integer>, Integer>> l = new ArrayList<>();
			if (n.size() == this.d) {
				return l;
			}
			for (int i = 0; i < this.b; i++) {
				List<Integer> nP = new ArrayList<>(n);
				nP.add((int)(i * 1.0 * this.maxPerDepth + new Random(this.seed + (i * n.hashCode()) + this.b).nextInt(this.maxPerDepth)) / this.b);
				l.add(new NodeExpansionDescription<>(nP, i, NodeType.OR));
			}
			return l;
		};
	}
}
