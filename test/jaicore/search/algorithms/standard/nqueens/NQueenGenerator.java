package jaicore.search.algorithms.standard.nqueens;

import java.util.ArrayList;
import java.util.List;

import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.NodeExpansionDescription;
import jaicore.search.structure.core.NodeType;
import jaicore.search.structure.graphgenerator.GoalTester;
import jaicore.search.structure.graphgenerator.RootGenerator;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

public class NQueenGenerator implements GraphGenerator<QueenNode,String> {
	
	int dimension;
	SingleRootGenerator<QueenNode> root;
	
	public NQueenGenerator(int dimension) {
		this.dimension = dimension;
		int initialPlacement = (int) (Math.random() * dimension);
		root = ()->new QueenNode(0, initialPlacement,dimension);
	}

	@Override
	public RootGenerator<QueenNode> getRootGenerator() {
		return root;
	}

	@Override
	public SuccessorGenerator<QueenNode, String> getSuccessorGenerator() {
		return n ->{
			List<NodeExpansionDescription<QueenNode,String>> l = new ArrayList<>();
			int currentRow = n.getPositions().get(n.getPositions().size()).getX();
			for(int i = 0; i < dimension; i++) {
				if(! n.attack(currentRow, i)){
					l.add(new NodeExpansionDescription<>(n, new QueenNode(n, currentRow, i), "edge label", NodeType.OR));
				}
			}
			return l;
		};
	}

	@Override
	public GoalTester<QueenNode> getGoalTester() {
		return n->{
			return false;
		};
		
	}

	@Override
	public boolean isSelfContained() {
		// TODO Auto-generated method stub
		return false;
	}
	
	

}
