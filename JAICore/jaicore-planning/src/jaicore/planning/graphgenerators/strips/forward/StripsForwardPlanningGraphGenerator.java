package jaicore.planning.graphgenerators.strips.forward;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.logic.fol.structure.Monom;
import jaicore.planning.model.core.PlannerUtil;
import jaicore.planning.model.strips.StripsAction;
import jaicore.planning.model.strips.StripsPlanningDomain;
import jaicore.planning.model.strips.StripsPlanningProblem;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.model.travesaltree.NodeExpansionDescription;
import jaicore.search.model.travesaltree.NodeType;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

public class StripsForwardPlanningGraphGenerator implements GraphGenerator<StripsForwardPlanningNode,String> {

	private final StripsPlanningProblem problem;
	private static final Logger logger = LoggerFactory.getLogger(StripsForwardPlanningGraphGenerator.class);
		
	public StripsForwardPlanningGraphGenerator(StripsPlanningProblem problem) {
		this.problem = problem;
	}

	@Override
	public SingleRootGenerator<StripsForwardPlanningNode> getRootGenerator() {
		return () -> new StripsForwardPlanningNode(problem.getInitState(), null);
	}

	@Override
	public SuccessorGenerator<StripsForwardPlanningNode,String> getSuccessorGenerator() {
		return l -> {
			logger.debug("Computing applicable actions for state {}", l.getState());
			List<NodeExpansionDescription<StripsForwardPlanningNode,String>> successors = new ArrayList<>();
			Monom state = l.getState();
			for (StripsAction action : PlannerUtil.getApplicableActionsInState(state, (StripsPlanningDomain)problem.getDomain())) {
				Monom successorState = new Monom(state);
				successorState.removeAll(action.getDeleteList());
				successorState.addAll(action.getAddList());
				successors.add(new NodeExpansionDescription<>(l, new StripsForwardPlanningNode(successorState, action), "edge label", NodeType.OR));
			}
			logger.debug("Identified {} applicable actions.", successors.size());
			return successors;
		};
	}

	@Override
	public NodeGoalTester<StripsForwardPlanningNode> getGoalTester() {
		return l -> problem.getGoalStateFunction().isGoalState(l.getState());
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
