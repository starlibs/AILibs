package jaicore.planning.graphgenerators.pddl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fr.uga.pddl4j.encoding.CodedProblem;
import fr.uga.pddl4j.heuristics.relaxation.Heuristic;
import fr.uga.pddl4j.heuristics.relaxation.HeuristicToolKit;
import fr.uga.pddl4j.planners.ProblemFactory;
import fr.uga.pddl4j.planners.hsp.Node;
import fr.uga.pddl4j.util.BitOp;
import fr.uga.pddl4j.util.BitState;
import fr.uga.pddl4j.util.SequentialPlan;
import jaicore.search.model.travesaltree.AbstractGraphGenerator;
import jaicore.search.model.travesaltree.NodeExpansionDescription;
import jaicore.search.model.travesaltree.NodeType;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

public class PDDLGraphGenerator extends AbstractGraphGenerator<PDDLNode,String> {

	ProblemFactory factory;
	CodedProblem problem;
	
    /**
     * The type of heuristics that must use to solve the problem.
     */
    private Heuristic.Type heuristicType;
	
	Heuristic heuristic;
	
	/**
	 * Constructor for the pddlgraphgenerator which gets a domain file and a problem file
	 * @param domain
	 * 		The domain file.
	 * @param problem
	 * 		The problem file.
	 */
	public PDDLGraphGenerator(File domainFile, File problemFile) {
		this.factory= new ProblemFactory();
		try {
			factory.parse(domainFile, problemFile);
			problem = factory.encode();
			this.setHeuristicType(Heuristic.Type.FAST_FORWARD);
			heuristic = HeuristicToolKit.createHeuristic(this.heuristicType, problem);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}

	@Override
	public SingleRootGenerator<PDDLNode> getRootGenerator() {
		//Create a root node and return it
		BitState init = new BitState(problem.getInit());
		return () -> new PDDLNode(new Node(init, null, -1, 0, heuristic.estimate(init, problem.getGoal())), this.nextID());
	}

	@Override
	public SuccessorGenerator<PDDLNode, String> getSuccessorGenerator() {
		return s->{
			Node current = s.getNode();
			List<NodeExpansionDescription<PDDLNode, String>> list = new ArrayList<>();
			for(BitOp op: problem.getOperators()) {
				if(op.isApplicable(current)) {
					Node state = new Node(current);
					op.getCondEffects().stream().filter(ce -> current.satisfy(ce.getCondition())).forEach(ce ->
                    // Apply the effect to the successor node
                    state.apply(ce.getEffects()));
					list.add(new NodeExpansionDescription<PDDLNode, String>(s, new PDDLNode(state, this.nextID()), "edge label", NodeType.OR));
//					list.add(new NodeExpansionDescription<Node, String>(s, state, "edge label", NodeType.OR));
				}
			}
			
			return list;
		};
	}

	@Override
	public NodeGoalTester<PDDLNode> getGoalTester() {
		return state ->{
			return state.getNode().satisfy(problem.getGoal());
		};
	}

	@Override
	public boolean isSelfContained() {
		return false;
	}

	public Heuristic.Type getHeuristicType() {
		return heuristicType;
	}

	public void setHeuristicType(Heuristic.Type heuristicType) {
		this.heuristicType = heuristicType;
	}

	public Heuristic getHeuristic() {
		return heuristic;
	}

	public void setHeuristic(Heuristic heuristic) {
		this.heuristic = heuristic;
	}

	public CodedProblem getProblem() {
		return problem;
	}

	public void setProblem(CodedProblem problem) {
		this.problem = problem;
	}
	
	//extracts the plan similar to extractPlan in HSP from pddl4j
	public SequentialPlan extractPlan(List<PDDLNode> solution) {
		int i = solution.size()-1;
		Node n = solution.get(i).getNode();
		SequentialPlan plan = new SequentialPlan();
		while(n.getOperator() != -1) {
			final BitOp op = problem.getOperators().get(n.getOperator());
			plan.add(0, op);
			i--;
			n = solution.get(i).getNode();
		}
		
		return plan;
		
	}
	
}
