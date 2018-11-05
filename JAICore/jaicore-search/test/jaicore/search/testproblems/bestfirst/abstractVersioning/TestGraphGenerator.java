package jaicore.search.testproblems.bestfirst.abstractVersioning;

import java.util.ArrayList;
import java.util.List;

import jaicore.search.model.travesaltree.AbstractGraphGenerator;
import jaicore.search.model.travesaltree.NodeExpansionDescription;
import jaicore.search.model.travesaltree.NodeType;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;


public class TestGraphGenerator extends AbstractGraphGenerator<TestNode,String> {
	private int size = 0;
	
	@Override
	public SingleRootGenerator<TestNode> getRootGenerator() {
		return () -> createTestNode();
	}

	@Override
	public SuccessorGenerator<TestNode, String> getSuccessorGenerator() {
		return n ->{
			List<NodeExpansionDescription<TestNode, String>> list = new ArrayList<>(3);
			for(int i = 0; i < 3; i++) {
				list.add(new NodeExpansionDescription<>(n, createTestNode(), "edge label", NodeType.OR));
			}
			return list;
		};

	}

	@Override
	public NodeGoalTester<TestNode> getGoalTester() {
		return n -> n.getValue()==100;
	}

	@Override
	public boolean isSelfContained() {
		return false;
	}
	
	
	private TestNode createTestNode() {
		this.size  ++;
		TestNode n = new TestNode(size);
		n.setId(this.nextID());
		return n;
	}
	
	public void reset() {
		this.size = 0;
		super.reset(1);
	}
	
	

}
