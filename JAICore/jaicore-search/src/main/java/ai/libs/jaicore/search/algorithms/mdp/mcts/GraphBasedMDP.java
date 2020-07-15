package ai.libs.jaicore.search.algorithms.mdp.mcts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;
import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.IPathGoalTester;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.datastructure.graph.ILabeledPath;
import org.api4.java.datastructure.graph.implicit.INewNodeDescription;
import org.api4.java.datastructure.graph.implicit.ISingleRootGenerator;
import org.api4.java.datastructure.graph.implicit.ISuccessorGenerator;

import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.probleminputs.IMDP;

public class GraphBasedMDP<N, A> implements IMDP<N, A, Double> {

	private final IPathSearchWithPathEvaluationsInput<N, A, Double> graph;
	private final N root;
	private final ISuccessorGenerator<N, A> succGen;
	private final IPathGoalTester<N, A> goalTester;
	private final Map<N, Pair<N, A>> backPointers = new HashMap<>();

	public GraphBasedMDP(final IPathSearchWithPathEvaluationsInput<N, A, Double> graph) {
		super();
		this.graph = graph;
		this.root = ((ISingleRootGenerator<N>) this.graph.getGraphGenerator().getRootGenerator()).getRoot();
		this.succGen = graph.getGraphGenerator().getSuccessorGenerator();
		this.goalTester = graph.getGoalTester();
	}

	@Override
	public N getInitState() {
		return this.root;
	}

	@Override
	public boolean isMaximizing() { // path searches are, by definition, always minimization problems
		return false;
	}

	@Override
	public Collection<A> getApplicableActions(final N state) throws InterruptedException {
		Collection<INewNodeDescription<N, A>> successors = this.succGen.generateSuccessors(state);
		Collection<A> actions = new ArrayList<>();
		for (INewNodeDescription<N, A> succ : successors) {
			A action = succ.getArcLabel();
			actions.add(action);
			if (this.backPointers.containsKey(succ.getTo())) {
				Pair<N, A> backpointer = this.backPointers.get(succ.getTo());
				boolean sameParent = backpointer.getX().equals(state);
				boolean sameAction = backpointer.getY().equals(action);
				if (!sameParent || !sameAction) {
					N otherNode = null;
					for (N key : this.backPointers.keySet()) {
						if (key.equals(succ.getTo())) {
							otherNode = key;
							break;
						}
					}
					throw new IllegalStateException("Reaching state " + succ.getTo() + " on a second way, which must not be the case in trees!\n\t1st way: " + backpointer.getX() + "; " + backpointer.getY() + "\n\t2nd way: " + state + "; " + action + "\n\ttoString of existing node: " + otherNode + "\n\tSame parent: " + sameParent + "\n\tSame Action: " + sameAction);
				}
			}
			this.backPointers.put(succ.getTo(), new Pair<>(state, action));
		}
		return actions;
	}

	@Override
	public Map<N, Double> getProb(final N state, final A action) throws InterruptedException {
		N succ = this.succGen.generateSuccessors(state).stream().filter(nd -> nd.getArcLabel().equals(action)).findAny().get().getTo();
		Map<N, Double> out = new HashMap<>();
		out.put(succ, 1.0);
		return out;
	}

	@Override
	public double getProb(final N state, final A action, final N successor) throws InterruptedException {
		return this.getProb(state, action).containsKey(successor) ? 1 : 0.0;
	}

	@Override
	public Double getScore(final N state, final A action, final N successor) throws PathEvaluationException, InterruptedException {

		/* now build the whole path using the back-pointer map */
		N cur = successor;
		List<N> nodes = new ArrayList<>();
		List<A> arcs = new ArrayList<>();
		nodes.add(cur);
		while (cur != this.root) {
			Pair<N, A> parentEdge = this.backPointers.get(cur);
			cur = parentEdge.getX();
			nodes.add(0, cur);
			arcs.add(0, parentEdge.getY());
		}
		ILabeledPath<N, A> path = new SearchGraphPath<>(nodes, arcs);

		/* check whether path is a goal path */
		if (!this.goalTester.isGoal(path)) { // in the MDP-view of a node, partial paths do not yield a reward but only full paths.
			return 0.0;
		}
		return this.graph.getPathEvaluator().evaluate(path).doubleValue();

	}

	@Override
	public boolean isTerminalState(final N state) throws InterruptedException {
		return this.getApplicableActions(state).isEmpty();
	}
}
