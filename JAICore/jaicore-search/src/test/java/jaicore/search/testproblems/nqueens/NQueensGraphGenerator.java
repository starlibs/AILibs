package jaicore.search.testproblems.nqueens;

import java.util.ArrayList;
import java.util.List;

import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.SerializableGraphGenerator;
import jaicore.search.model.travesaltree.NodeExpansionDescription;
import jaicore.search.model.travesaltree.NodeType;
import jaicore.search.structure.graphgenerator.MultipleRootGenerator;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

@SuppressWarnings("serial")
public class NQueensGraphGenerator implements SerializableGraphGenerator<QueenNode,String> {

	int dimension;
	MultipleRootGenerator<QueenNode> root;

	public NQueensGraphGenerator(final int dimension) {
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
		return () ->  new QueenNode(this.dimension);
	}

	@Override
	public SuccessorGenerator<QueenNode, String> getSuccessorGenerator() {
		return n ->{
			List<NodeExpansionDescription<QueenNode,String>> l = new ArrayList<>();
			int currentRow = n.getPositions().size();
			for(int i = 0; i < this.dimension; i++) {
				if(! n.attack(currentRow, i)){
					l.add(new NodeExpansionDescription<>(n, new QueenNode(n, i), "" + i, NodeType.OR));
				}
			}
			return l;
		};
	}

	@Override
	public NodeGoalTester<QueenNode> getGoalTester() {
		return n -> {
			if(n.getNumberOfQueens() == this.dimension) {
				return true;
			} else {
				return false;
			}

		};
	}

	@Override
	public boolean isSelfContained() {
		return true;
	}

	@Override
	public void setNodeNumbering(final boolean nodenumbering) {
		// TODO Auto-generated method stub

	}



}
