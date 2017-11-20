package jaicore.search.algorithms.standard.nqueens;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.NodeExpansionDescription;
import jaicore.search.structure.core.NodeType;
import jaicore.search.structure.graphgenerator.GoalTester;
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
		
		GraphGenerator<QueenNode,String> gen = new GraphGenerator<QueenNode, String>(){

			@Override
			public RootGenerator<QueenNode> getRootGenerator() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public SuccessorGenerator<QueenNode, String> getSuccessorGenerator() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public GoalTester<QueenNode> getGoalTester() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public boolean isSelfContained() {
				// TODO Auto-generated method stub
				return false;
			}
		};
		
		System.out.println(gen.toString()+ n);
	}
}


