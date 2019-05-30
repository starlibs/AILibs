package jaicore.planning.classical.algorithms.strips.forward;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.logic.fol.structure.Monom;
import jaicore.planning.classical.problems.strips.StripsAction;
import jaicore.planning.classical.problems.strips.StripsPlanningDomain;
import jaicore.planning.classical.problems.strips.StripsPlanningProblem;
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
	private final Map<StripsForwardPlanningNode, List<StripsAction>> returnedActions = new HashMap<>(); // maintain a local copy of the graph here
	private final Map<StripsForwardPlanningNode, List<StripsAction>> unreturnedActions = new HashMap<>(); // maintain a local copy of the graph here
	private final Set<StripsForwardPlanningNode> completelyExpandedNodes = new HashSet<>();

	public StripsForwardPlanningGraphGenerator(StripsPlanningProblem problem) {
		this.problem = problem;
		this.initState = problem.getInitState();
	}

	@Override
	public SingleRootGenerator<StripsForwardPlanningNode> getRootGenerator() {
		return () -> {
			StripsForwardPlanningNode root = new StripsForwardPlanningNode(new Monom(), new Monom(), null);
			return root;
		};
	}

	private List<StripsAction> getApplicableActionsInNode(StripsForwardPlanningNode node) {
		logger.info("Computing successors for node {}", node);
		long start = System.currentTimeMillis();
		Monom state = node.getStateRelativeToInitState(initState);
		List<StripsAction> applicableActions = StripsUtil.getApplicableActionsInState(state, (StripsPlanningDomain) problem.getDomain());
		logger.debug("Computation of applicable actions took {}ms", System.currentTimeMillis() - start);
		return applicableActions;
	}

	private StripsAction getRandomApplicableActionInNode(StripsForwardPlanningNode node) {
		logger.info("Computing random successor for node {}", node);
		long start = System.currentTimeMillis();
		Monom state = node.getStateRelativeToInitState(initState);
		long timeToComputeState = System.currentTimeMillis() - start;
		List<StripsAction> applicableActions = StripsUtil.getApplicableActionsInState(state, (StripsPlanningDomain) problem.getDomain(), true, 2);
		logger.debug("Computation of applicable actions took {}ms of which {}ms were used to reproduce the state.", System.currentTimeMillis() - start, timeToComputeState);
		return applicableActions.isEmpty() ? null : applicableActions.get(0);
	}

	@Override
	public SingleSuccessorGenerator<StripsForwardPlanningNode, String> getSuccessorGenerator() {
		return new SingleSuccessorGenerator<StripsForwardPlanningNode, String>() {

			@Override
			public List<NodeExpansionDescription<StripsForwardPlanningNode, String>> generateSuccessors(StripsForwardPlanningNode node) throws InterruptedException {
				if (completelyExpandedNodes.contains(node))
					throw new IllegalArgumentException("Successors of node " + node + " have already been computed.");
				long start = System.currentTimeMillis();
				List<NodeExpansionDescription<StripsForwardPlanningNode, String>> successors = new ArrayList<>();
				List<StripsAction> applicableActions = getApplicableActionsInNode(node);
				returnedActions.put(node, applicableActions);
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
				
				/* if all successors of this node have been returned, return null */
				if (completelyExpandedNodes.contains(node) && unreturnedActions.get(node).isEmpty()) {
					return null;
				}
				
				/* if no successor has been computed for this node, add the list */
				long start = System.currentTimeMillis();
				assert i >= 0 : "Index must not be negative!";
				StripsAction action;
				
				/* if all successors of this node are known (but not all have been returned yet), just select the next action of the one already internally computed */
				if (completelyExpandedNodes.contains(node)) {
					action = unreturnedActions.get(node).get(0);
				}
				
				/* if this node is asked for the second time for a successor, internally just compute all successors */
				else if (returnedActions.containsKey(node)) {
					assert !returnedActions.get(node).isEmpty();
					assert unreturnedActions.get(node).isEmpty();
					logger.debug("Generating {}th successor of {}", returnedActions.get(node).size() + 1, node);
					
					/* memorize the actions already generated, generate all successors (re-generating the known ones), and update the stats about returned and unreturned actions */
					List<StripsAction> returnedSolutionBak = new ArrayList<>(returnedActions.get(node));
					generateSuccessors(node);
					unreturnedActions.put(node, returnedActions.get(node));
					returnedActions.put(node, returnedSolutionBak);
					unreturnedActions.get(node).removeAll(returnedSolutionBak);
					assert completelyExpandedNodes.contains(node);
					
					/* if, by chance, all successors already had been computed, there is no new successor we could return here. So return NULL */
					if (unreturnedActions.get(node).isEmpty())
						return null;
					action = unreturnedActions.get(node).get(i % unreturnedActions.get(node).size());
				}
				
				/* if this node is asked for the first time for a successor, just compute a random successor */
				else {
					assert !completelyExpandedNodes.contains(node);
					assert !returnedActions.containsKey(node);
					assert !unreturnedActions.containsKey(node);
					
					/* try to generate a new and unknown successor of the node */
					action = getRandomApplicableActionInNode(node);
					if (action == null) {
//						generateSuccessors(node);
//						unreturnedActions.put(node, new ArrayList<>(returnedActions.get(node))); // undo the flags set by the generateSuccessors method
//						returnedActions.get(node).clear();
//						assert completelyExpandedNodes.contains(node);
//						if (unreturnedActions.get(node).isEmpty())
						logger.debug("Apparently, the node {} has no successors.", node);
						return null;
//						action = unreturnedActions.get(node).get(i % unreturnedActions.get(node).size());
					}
					else {
						if (!unreturnedActions.containsKey(node))
							unreturnedActions.put(node, new ArrayList<>());
						unreturnedActions.get(node).add(action);
						assert !completelyExpandedNodes.contains(node);
					}
				}
				
				/* action should not be null at this point */
				assert action != null;
				if (!returnedActions.containsKey(node))
					returnedActions.put(node, new ArrayList<>());
				returnedActions.get(node).add(action);
				unreturnedActions.get(node).remove(action);
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
