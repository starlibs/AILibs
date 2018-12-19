package jaicore.planning.graphgenerators.strips.forward;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	private final Map<StripsForwardPlanningNode, List<StripsAction>> appliedActions = new HashMap<>(); // maintain a local copy of the graph here
	private final Set<StripsForwardPlanningNode> completelyExpandedNodes = new HashSet<>();

	public StripsForwardPlanningGraphGenerator(StripsPlanningProblem problem) {
		this.problem = problem;
		this.initState = problem.getInitState();
	}

	@Override
	public SingleRootGenerator<StripsForwardPlanningNode> getRootGenerator() {
		return () -> {
			StripsForwardPlanningNode root = new StripsForwardPlanningNode(new Monom(), new Monom(), null);
			appliedActions.put(root, new ArrayList<>());
			return root;
		};
	}

	private List<StripsAction> getApplicableActionsInNode(StripsForwardPlanningNode node) {
		logger.info("Computing successors for node {}", node);
		long start = System.currentTimeMillis();
		Monom state = node.getStateRelativeToInitState(initState);
		List<StripsAction> applicableActions = PlannerUtil.getApplicableActionsInState(state, (StripsPlanningDomain) problem.getDomain());
		logger.debug("Computation of applicable actions took {}ms", System.currentTimeMillis() - start);
		return applicableActions;
	}

	private StripsAction getRandomApplicableActionInNode(StripsForwardPlanningNode node) {
		logger.info("Computing random successor for node {}", node);
		long start = System.currentTimeMillis();
		Monom state = node.getStateRelativeToInitState(initState);
		long timeToComputeState = System.currentTimeMillis() - start;
		List<StripsAction> applicableActions = PlannerUtil.getApplicableActionsInState(state, (StripsPlanningDomain) problem.getDomain(), true, 5);
		logger.debug("Computation of applicable actions took {}ms of which {}ms were used to reproduce the state.", System.currentTimeMillis() - start, timeToComputeState);
		return applicableActions.isEmpty() ? null : applicableActions.get(0);
	}

	@Override
	public SingleSuccessorGenerator<StripsForwardPlanningNode, String> getSuccessorGenerator() {
		return new SingleSuccessorGenerator<StripsForwardPlanningNode, String>() {

			private Set<StripsForwardPlanningNode> completelyExpandedNodes = new HashSet<>();

			@Override
			public List<NodeExpansionDescription<StripsForwardPlanningNode, String>> generateSuccessors(StripsForwardPlanningNode node) throws InterruptedException {
				long start = System.currentTimeMillis();
				List<NodeExpansionDescription<StripsForwardPlanningNode, String>> successors = new ArrayList<>();
				List<StripsAction> applicableActions = getApplicableActionsInNode(node);
				appliedActions.put(node, applicableActions);
				for (StripsAction action : applicableActions) {
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
				completelyExpandedNodes.add(node);
				return successors;
			}

			@Override
			public NodeExpansionDescription<StripsForwardPlanningNode, String> generateSuccessor(StripsForwardPlanningNode node, int i) throws InterruptedException {

				long start = System.currentTimeMillis();
				
				/* if no successor has been computed for this node, add the list */
				if (!appliedActions.containsKey(node))
					appliedActions.put(node, new ArrayList<>());

				/* determine action (if index here is high, just compute all of them) */
				assert i >= 0 : "Index must not be negative!";
				StripsAction action;
				if (completelyExpandedNodes.contains(node)) {
					action = appliedActions.get(node).get(i % appliedActions.get(node).size());
				} else if (appliedActions.get(node).size() >= 3) {
					generateSuccessors(node);
					assert completelyExpandedNodes.contains(node);
					action = appliedActions.get(node).get(i % appliedActions.get(node).size());
				} else {
					int counter = 0;
					while ((action = getRandomApplicableActionInNode(node)) != null && appliedActions.get(node).contains(action) && counter < 10) {
						logger.debug("Created the same action for the same time, iterating again.");
						counter++;
					}
					if (action == null) {
						logger.debug("Generating ALL successors, since the previous procedure has not revealed any new action within {} iterations", counter);
						generateSuccessors(node);
						assert completelyExpandedNodes.contains(node);
						if (appliedActions.get(node).isEmpty())
							return null;
						action = appliedActions.get(node).get(i % appliedActions.get(node).size());
					}
				}
				
				/* action should not be null at this point */
				assert action != null;
				appliedActions.get(node).add(action);
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

			@Override
			public boolean allSuccessorsComputed(StripsForwardPlanningNode node) {
				return completelyExpandedNodes.contains(node);
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
