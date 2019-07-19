package ai.libs.jaicore.search.algorithms.andor;

import java.util.ArrayList;
import java.util.List;

import org.api4.java.datastructure.graph.implicit.IGraphGenerator;
import org.api4.java.datastructure.graph.implicit.NodeExpansionDescription;
import org.api4.java.datastructure.graph.implicit.NodeType;
import org.api4.java.datastructure.graph.implicit.SingleRootGenerator;
import org.api4.java.datastructure.graph.implicit.SuccessorGenerator;

import ai.libs.jaicore.search.algorithms.andor.SyntheticAndGrid.NodeLabel;

public class SyntheticAndGrid implements IGraphGenerator<NodeLabel, String> {

	private int k = 10;
	private int b = 3;
	private int depth = 10;


	public class NodeLabel {
		int depth;
		int task;
		public NodeLabel(final int depth, final int task) {
			super();
			this.depth = depth;
			this.task = task;
		}
	}

	public SyntheticAndGrid() {

	}

	public SyntheticAndGrid(final int k, final int b, final int depth) {
		super();
		this.k = k;
		this.b = b;
		this.depth = depth;
	}

	@Override
	public SingleRootGenerator<NodeLabel> getRootGenerator() {
		return () -> new NodeLabel(0,0);
	}

	@Override
	public SuccessorGenerator<NodeLabel, String> getSuccessorGenerator() {
		return n -> {
			List<NodeExpansionDescription<NodeLabel,String>> l = new ArrayList<>();
			if (n.depth == this.depth) {
				return l;
			}
			boolean finalLayer = n.depth >= this.depth - 2;
			for (int i = 0; i < (finalLayer ? this.k : this.b); i++) {
				l.add(new NodeExpansionDescription<>(new NodeLabel(n.depth + 1, i), "" + i, finalLayer ? NodeType.OR : NodeType.AND));
			}
			return l;
		};
	}

	public int getDepth() {
		return this.depth;
	}
}
