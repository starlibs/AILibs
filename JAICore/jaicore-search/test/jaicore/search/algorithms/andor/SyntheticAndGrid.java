package jaicore.search.algorithms.andor;

import java.util.ArrayList;
import java.util.List;

import jaicore.search.algorithms.andor.SyntheticAndGrid.NodeLabel;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.model.travesaltree.NodeExpansionDescription;
import jaicore.search.model.travesaltree.NodeType;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

public class SyntheticAndGrid implements GraphGenerator<NodeLabel, String> {
	
	private int k = 10;
	private int b = 3;
	private int depth = 10;

	
	class NodeLabel {
		int depth;
		int task;
		public NodeLabel(int depth, int task) {
			super();
			this.depth = depth;
			this.task = task;
		}
	}
	
	public SyntheticAndGrid() {
		
	}
	
	public SyntheticAndGrid(int k, int b, int depth) {
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
			if (n.depth == depth)
				return l;
			boolean finalLayer = n.depth >= depth - 2;
			for (int i = 0; i < (finalLayer ? k : b); i++) {
				l.add(new NodeExpansionDescription<NodeLabel, String>(n, new NodeLabel(n.depth + 1, i), "" + i, finalLayer ? NodeType.OR : NodeType.AND));
			}
			return l;
		};
	}

	@Override
	public NodeGoalTester<NodeLabel> getGoalTester() {
		return n -> n.depth == depth;
	}

	@Override
	public boolean isSelfContained() {
		return false;
	}

	@Override
	public void setNodeNumbering(boolean nodenumbering) {
		// TODO Auto-generated method stub
		
	}
	
}
