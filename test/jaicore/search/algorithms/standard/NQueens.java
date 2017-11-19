package jaicore.search.algorithms.standard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.NodeExpansionDescription;
import jaicore.search.structure.core.NodeType;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.PathGoalTester;
import jaicore.search.structure.graphgenerator.RootGenerator;
import jaicore.search.structure.graphgenerator.SelfContained;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

public class NQueens {
	
	
	
	public static void main(String [] args) {
		int n = 0;
		if(args.length != 0)
			n = Integer.parseInt(args[0]);
		
		GraphGenerator<QueenNode,String> gen = new GraphGenerator<QueenNode, String>() {

			@Override
			public RootGenerator getRootGenerator() {
				return () -> {
					return Arrays.asList(new QueenNode((int)(Math.random()*8),1));
				};
			}

			@Override
			public SuccessorGenerator getSuccessorGenerator() {
				return n -> {
					List<NodeExpansionDescription<QueenNode, String>> l = new ArrayList<>();
					l.add(new NodeExpansionDescription(n, new QueenNode(2,2),"edge label", NodeType.OR));
					return l;
				};
			}

			@Override
			public PathGoalTester getPathGoalTester() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public NodeGoalTester getNodeGoalTester() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public SelfContained isSelfContained() {
				return ()-> true;
			}
		
		};
		
		System.out.println(gen.toString()+ n);
	}
}


