package jaicore.search.graphgenerators.nqueens;

import java.util.ArrayList;
import java.util.List;

import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.NodeExpansionDescription;
import jaicore.search.structure.core.NodeType;
import jaicore.search.structure.graphgenerator.MultipleRootGenerator;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

public class NQueenGenerator implements GraphGenerator<QueenNode,String> {
	
	int dimension;
	MultipleRootGenerator<QueenNode> root;
	
	public NQueenGenerator(int dimension) {
		this.dimension = dimension;		
	}

//	@Override
//	public MultipleRootGenerator<QueenNode> getRootGenerator() {
//		return () ->{
//			List<QueenNode> l = new ArrayList<>();
//			for(int i = 0; i < dimension; i++) {
//				l.add(new QueenNode(0,i, dimension));
//			}
//			return l;
//		};
//	}
	
	@Override
	public SingleRootGenerator<QueenNode> getRootGenerator(){
		return () ->  new QueenNode(dimension);
	}

	@Override
	public SuccessorGenerator<QueenNode, String> getSuccessorGenerator() {
		return n ->{
			List<NodeExpansionDescription<QueenNode,String>> l = new ArrayList<>();
			int currentRow = n.getPositions().size();
			for(int i = 0; i < dimension; i++) {
				if(! n.attack(currentRow, i)){
					l.add(new NodeExpansionDescription<>(n, new QueenNode(n, i), "(" + currentRow + ", " + i + ")", NodeType.OR));
				}
			}
			return l;
		};
	}

	@Override
	public NodeGoalTester<QueenNode> getGoalTester() {
		return n -> {
			if(n.getNumberOfQueens() == dimension)
				return true;
			else
				return false;
			
		};
	}

	@Override
	public boolean isSelfContained() {
		return true;
	}
	
	

}
