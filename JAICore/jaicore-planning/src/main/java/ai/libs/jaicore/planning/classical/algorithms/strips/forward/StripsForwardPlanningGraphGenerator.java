package ai.libs.jaicore.planning.classical.algorithms.strips.forward;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.api4.java.datastructure.graph.implicit.IGraphGenerator;
import org.api4.java.datastructure.graph.implicit.ILazySuccessorGenerator;
import org.api4.java.datastructure.graph.implicit.INewNodeDescription;
import org.api4.java.datastructure.graph.implicit.ISingleRootGenerator;
import org.api4.java.datastructure.graph.implicit.NodeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.logic.fol.structure.Monom;
import ai.libs.jaicore.planning.classical.problems.strips.StripsAction;
import ai.libs.jaicore.planning.classical.problems.strips.StripsPlanningProblem;
import ai.libs.jaicore.search.model.NodeExpansionDescription;

public class StripsForwardPlanningGraphGenerator implements IGraphGenerator<StripsForwardPlanningNode, String> {

	private final StripsPlanningProblem problem;
	private static final Logger logger = LoggerFactory.getLogger(StripsForwardPlanningGraphGenerator.class);
	private final Monom initState;
	private final Map<StripsForwardPlanningNode, List<StripsAction>> returnedActions = new HashMap<>(); // maintain a local copy of the graph here
	private final Map<StripsForwardPlanningNode, List<StripsAction>> unreturnedActions = new HashMap<>(); // maintain a local copy of the graph here
	private final Set<StripsForwardPlanningNode> completelyExpandedNodes = new HashSet<>();

	public StripsForwardPlanningGraphGenerator(final StripsPlanningProblem problem) {
		this.problem = problem;
		this.initState = problem.getInitState();
	}

	@Override
	public ISingleRootGenerator<StripsForwardPlanningNode> getRootGenerator() {
		return () -> {
			StripsForwardPlanningNode root = new StripsForwardPlanningNode(new Monom(), new Monom(), null);
			return root;
		};
	}

	private List<StripsAction> getApplicableActionsInNode(final StripsForwardPlanningNode node) {
		logger.info("Computing successors for node {}", node);
		long start = System.currentTimeMillis();
		Monom state = node.getStateRelativeToInitState(this.initState);
		List<StripsAction> applicableActions = StripsUtil.getApplicableActionsInState(state, this.problem.getDomain());
		logger.debug("Computation of applicable actions took {}ms", System.currentTimeMillis() - start);
		return applicableActions;
	}

	private StripsAction getRandomApplicableActionInNode(final StripsForwardPlanningNode node) {
		logger.info("Computing random successor for node {}", node);
		long start = System.currentTimeMillis();
		Monom state = node.getStateRelativeToInitState(this.initState);
		long timeToComputeState = System.currentTimeMillis() - start;
		List<StripsAction> applicableActions = StripsUtil.getApplicableActionsInState(state, this.problem.getDomain(), true, 2);
		logger.debug("Computation of applicable actions took {}ms of which {}ms were used to reproduce the state.", System.currentTimeMillis() - start, timeToComputeState);
		return applicableActions.isEmpty() ? null : applicableActions.get(0);
	}

	@Override
	public ILazySuccessorGenerator<StripsForwardPlanningNode, String> getSuccessorGenerator() {
		return new ILazySuccessorGenerator<StripsForwardPlanningNode, String>() {

			@Override
			public List<INewNodeDescription<StripsForwardPlanningNode, String>> generateSuccessors(final StripsForwardPlanningNode node) throws InterruptedException {
				if (StripsForwardPlanningGraphGenerator.this.completelyExpandedNodes.contains(node)) {
					throw new IllegalArgumentException("Successors of node " + node + " have already been computed.");
				}
				long start = System.currentTimeMillis();
				List<INewNodeDescription<StripsForwardPlanningNode, String>> successors = new ArrayList<>();
				List<StripsAction> applicableActions = StripsForwardPlanningGraphGenerator.this.getApplicableActionsInNode(node);
				StripsForwardPlanningGraphGenerator.this.returnedActions.put(node, applicableActions);
				for (StripsAction action : applicableActions) {
					long t = System.currentTimeMillis();
					Monom del = new Monom(node.getDel());
					Monom add = new Monom(node.getAdd());
					del.addAll(action.getDeleteList());
					add.removeAll(action.getDeleteList());
					add.addAll(action.getAddList());

					StripsForwardPlanningNode newNode = new StripsForwardPlanningNode(add, del, action);
					successors.add(new NodeExpansionDescription<>(newNode, "edge label"));
					if (logger.isTraceEnabled()) {
						logger.trace("Created the node expansion description within {}ms. New state size is {}.", System.currentTimeMillis() - t, newNode.getStateRelativeToInitState(StripsForwardPlanningGraphGenerator.this.initState).size());
					}
				}
				logger.info("Generated {} successors in {}ms.", successors.size(), System.currentTimeMillis() - start);
				StripsForwardPlanningGraphGenerator.this.completelyExpandedNodes.add(node);
				return successors;
			}

			@Override
			public NodeExpansionDescription<StripsForwardPlanningNode, String> generateSuccessor(final StripsForwardPlanningNode node, final int i) throws InterruptedException {

				/* if all successors of this node have been returned, return null */
				if (StripsForwardPlanningGraphGenerator.this.completelyExpandedNodes.contains(node) && StripsForwardPlanningGraphGenerator.this.unreturnedActions.get(node).isEmpty()) {
					return null;
				}

				/* if no successor has been computed for this node, add the list */
				long start = System.currentTimeMillis();
				assert i >= 0 : "Index must not be negative!";
				StripsAction action;

				/* if all successors of this node are known (but not all have been returned yet), just select the next action of the one already internally computed */
				if (StripsForwardPlanningGraphGenerator.this.completelyExpandedNodes.contains(node)) {
					action = StripsForwardPlanningGraphGenerator.this.unreturnedActions.get(node).get(0);
				}

				/* if this node is asked for the second time for a successor, internally just compute all successors */
				else if (StripsForwardPlanningGraphGenerator.this.returnedActions.containsKey(node)) {
					assert !StripsForwardPlanningGraphGenerator.this.returnedActions.get(node).isEmpty();
					assert StripsForwardPlanningGraphGenerator.this.unreturnedActions.get(node).isEmpty();
					logger.debug("Generating {}th successor of {}", StripsForwardPlanningGraphGenerator.this.returnedActions.get(node).size() + 1, node);

					/* memorize the actions already generated, generate all successors (re-generating the known ones), and update the stats about returned and unreturned actions */
					List<StripsAction> returnedSolutionBak = new ArrayList<>(StripsForwardPlanningGraphGenerator.this.returnedActions.get(node));
					this.generateSuccessors(node);
					StripsForwardPlanningGraphGenerator.this.unreturnedActions.put(node, StripsForwardPlanningGraphGenerator.this.returnedActions.get(node));
					StripsForwardPlanningGraphGenerator.this.returnedActions.put(node, returnedSolutionBak);
					StripsForwardPlanningGraphGenerator.this.unreturnedActions.get(node).removeAll(returnedSolutionBak);
					assert StripsForwardPlanningGraphGenerator.this.completelyExpandedNodes.contains(node);

					/* if, by chance, all successors already had been computed, there is no new successor we could return here. So return NULL */
					if (StripsForwardPlanningGraphGenerator.this.unreturnedActions.get(node).isEmpty()) {
						return null;
					}
					action = StripsForwardPlanningGraphGenerator.this.unreturnedActions.get(node).get(i % StripsForwardPlanningGraphGenerator.this.unreturnedActions.get(node).size());
				}

				/* if this node is asked for the first time for a successor, just compute a random successor */
				else {
					assert !StripsForwardPlanningGraphGenerator.this.completelyExpandedNodes.contains(node);
					assert !StripsForwardPlanningGraphGenerator.this.returnedActions.containsKey(node);
					assert !StripsForwardPlanningGraphGenerator.this.unreturnedActions.containsKey(node);

					/* try to generate a new and unknown successor of the node */
					action = StripsForwardPlanningGraphGenerator.this.getRandomApplicableActionInNode(node);
					if (action == null) {
						logger.debug("Apparently, the node {} has no successors.", node);
						return null;
					}
					else {
						if (!StripsForwardPlanningGraphGenerator.this.unreturnedActions.containsKey(node)) {
							StripsForwardPlanningGraphGenerator.this.unreturnedActions.put(node, new ArrayList<>());
						}
						StripsForwardPlanningGraphGenerator.this.unreturnedActions.get(node).add(action);
						assert !StripsForwardPlanningGraphGenerator.this.completelyExpandedNodes.contains(node);
					}
				}

				/* action should not be null at this point */
				assert action != null;
				if (!StripsForwardPlanningGraphGenerator.this.returnedActions.containsKey(node)) {
					StripsForwardPlanningGraphGenerator.this.returnedActions.put(node, new ArrayList<>());
				}
				StripsForwardPlanningGraphGenerator.this.returnedActions.get(node).add(action);
				StripsForwardPlanningGraphGenerator.this.unreturnedActions.get(node).remove(action);
				long t = System.currentTimeMillis();
				Monom del = new Monom(node.getDel());
				Monom add = new Monom(node.getAdd());
				del.addAll(action.getDeleteList());
				add.removeAll(action.getDeleteList());
				add.addAll(action.getAddList());
				StripsForwardPlanningNode newNode = new StripsForwardPlanningNode(add, del, action);
				NodeExpansionDescription<StripsForwardPlanningNode, String> successor = new NodeExpansionDescription<>(newNode, "edge label", NodeType.OR);
				if (logger.isTraceEnabled()) {
					logger.trace("Created the node expansion description within {}ms. New state size is {}.", System.currentTimeMillis() - t, newNode.getStateRelativeToInitState(StripsForwardPlanningGraphGenerator.this.initState).size());
				}
				logger.info("Generated {}-th successor in {}ms.", i, System.currentTimeMillis() - start);
				return successor;
			}

			@Override
			public boolean allSuccessorsComputed(final StripsForwardPlanningNode node) {
				return StripsForwardPlanningGraphGenerator.this.completelyExpandedNodes.contains(node);
			}

		};
	}
}
