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
import jaicore.search.structure.graphgenerator.SingleSuccessorGenerator;

public class StripsForwardPlanningGraphGenerator implements GraphGenerator<StripsForwardPlanningNode, String> {

	private final StripsPlanningProblem problem;
	private static final Logger logger = LoggerFactory.getLogger(StripsForwardPlanningGraphGenerator.class);
	private final Monom initState;

	public StripsForwardPlanningGraphGenerator(StripsPlanningProblem problem) {
		this.problem = problem;
		this.initState = problem.getInitState();
	}

	@Override
	public SingleRootGenerator<StripsForwardPlanningNode> getRootGenerator() {
		return () -> new StripsForwardPlanningNode(new Monom(), new Monom(), null);
	}
	
	private List<StripsAction> getApplicableActionsInNode(StripsForwardPlanningNode node) {
		logger.info("Computing successors for node {}", node);
		long start = System.currentTimeMillis();
		Monom state = node.getStateRelativeToInitState(initState);
		List<StripsAction> applicableActions = PlannerUtil.getApplicableActionsInState(state, (StripsPlanningDomain) problem.getDomain());
		logger.debug("Computation of applicable actions took {}ms", System.currentTimeMillis() - start);
		return applicableActions;
	}

	@Override
	public SingleSuccessorGenerator<StripsForwardPlanningNode, String> getSuccessorGenerator() {
		return new SingleSuccessorGenerator<StripsForwardPlanningNode, String>() {

			@Override
			public List<NodeExpansionDescription<StripsForwardPlanningNode, String>> generateSuccessors(StripsForwardPlanningNode node) throws InterruptedException {
				long start = System.currentTimeMillis();
				List<NodeExpansionDescription<StripsForwardPlanningNode, String>> successors = new ArrayList<>();
				for (StripsAction action : getApplicableActionsInNode(node)) {
					long t = System.currentTimeMillis();
					Monom del = new Monom(node.getDel());
					Monom add = new Monom(node.getAdd());
					del.addAll(action.getDeleteList());
					add.removeAll(action.getDeleteList());
					add.addAll(action.getAddList());
					StripsForwardPlanningNode newNode = new StripsForwardPlanningNode(add, del, action);
					successors.add(new NodeExpansionDescription<>(node, newNode, "edge label", NodeType.OR));
					if (logger.isTraceEnabled())
						logger.trace("Created the node expansion description within {}ms. New state size is {}.", System.currentTimeMillis() - t, newNode.getStateRelativeToInitState(initState).size());
				}
				logger.info("Generated {} successors in {}ms.", successors.size(), System.currentTimeMillis() - start);
				return successors;
			}

			@Override
			public NodeExpansionDescription<StripsForwardPlanningNode, String> generateSuccessor(StripsForwardPlanningNode node, int i) throws InterruptedException {
				System.out.println("Compute single successor!");
				long start = System.currentTimeMillis();
				List<StripsAction> applicableActions = getApplicableActionsInNode(node);
				StripsAction action = applicableActions.get(i % applicableActions.size());
				long t = System.currentTimeMillis();
				Monom del = new Monom(node.getDel());
				Monom add = new Monom(node.getAdd());
				del.addAll(action.getDeleteList());
				add.removeAll(action.getDeleteList());
				add.addAll(action.getAddList());
				StripsForwardPlanningNode newNode = new StripsForwardPlanningNode(add, del, action);
				NodeExpansionDescription<StripsForwardPlanningNode, String> successor = new NodeExpansionDescription<>(node, newNode, "edge label", NodeType.OR);
				if (logger.isTraceEnabled())
					logger.trace("Created the node expansion description within {}ms. New state size is {}.", System.currentTimeMillis() - t, newNode.getStateRelativeToInitState(initState).size());
				logger.info("Generated {}-th successor in {}ms.", i, System.currentTimeMillis() - start);
				return successor;
			}

		};
	}

	@Override
	public NodeGoalTester<StripsForwardPlanningNode> getGoalTester() {
		return l -> problem.getGoalStateFunction().isGoalState(l.getStateRelativeToInitState(initState));
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
