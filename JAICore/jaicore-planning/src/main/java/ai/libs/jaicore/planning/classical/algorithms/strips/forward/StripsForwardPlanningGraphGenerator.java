package ai.libs.jaicore.planning.classical.algorithms.strips.forward;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.api4.java.datastructure.graph.implicit.IGraphGenerator;
import org.api4.java.datastructure.graph.implicit.INewNodeDescription;
import org.api4.java.datastructure.graph.implicit.ISingleRootGenerator;
import org.api4.java.datastructure.graph.implicit.ISuccessorGenerator;
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

	@Override
	public ISuccessorGenerator<StripsForwardPlanningNode, String> getSuccessorGenerator() {
		return new ISuccessorGenerator<StripsForwardPlanningNode, String>() {

			@Override
			public List<INewNodeDescription<StripsForwardPlanningNode, String>> generateSuccessors(final StripsForwardPlanningNode node) throws InterruptedException {
				if (StripsForwardPlanningGraphGenerator.this.completelyExpandedNodes.contains(node)) {
					throw new IllegalArgumentException("Successors of node " + node + " have already been computed.");
				}
				long start = System.currentTimeMillis();
				List<INewNodeDescription<StripsForwardPlanningNode, String>> successors = new ArrayList<>();
				List<StripsAction> applicableActions = StripsForwardPlanningGraphGenerator.this.getApplicableActionsInNode(node);
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
		};
	}
}
