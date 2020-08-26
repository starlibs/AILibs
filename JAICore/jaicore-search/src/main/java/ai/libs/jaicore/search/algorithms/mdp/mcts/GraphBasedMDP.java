package ai.libs.jaicore.search.algorithms.mdp.mcts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;
import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.IPathGoalTester;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.datastructure.graph.ILabeledPath;
import org.api4.java.datastructure.graph.implicit.INewNodeDescription;
import org.api4.java.datastructure.graph.implicit.ISingleRootGenerator;
import org.api4.java.datastructure.graph.implicit.ISuccessorGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.search.model.ILazyRandomizableSuccessorGenerator;
import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.probleminputs.IMDP;

public class GraphBasedMDP<N, A> implements IMDP<N, A, Double>, ILoggingCustomizable {

	private static final int MAX_SUCCESSOR_CACHE_SIZE = 100;

	private final IPathSearchWithPathEvaluationsInput<N, A, Double> graph;
	private final N root;
	private final ISuccessorGenerator<N, A> succGen;
	private final IPathGoalTester<N, A> goalTester;
	private final Map<N, Pair<N, A>> backPointers = new HashMap<>();
	private Logger logger = LoggerFactory.getLogger(GraphBasedMDP.class);
	private final Map<N, Map<A, N>> successorCache = new HashMap<>();

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
		Map<A, N> cache = new HashMap<>();
		for (INewNodeDescription<N, A> succ : successors) {
			A action = succ.getArcLabel();
			actions.add(action);
			cache.put(action, succ.getTo());
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

		/* clear the cache if we have too many entries */
		if (this.successorCache.size() > MAX_SUCCESSOR_CACHE_SIZE) {
			this.successorCache.clear();
		}
		this.successorCache.put(state, cache);
		return actions;
	}

	@Override
	public Map<N, Double> getProb(final N state, final A action) throws InterruptedException {

		/* first determine the successor node (either by cache or by constructing the successors again) */
		N successor = null;
		if (this.successorCache.containsKey(state) && this.successorCache.get(state).containsKey(action)) {
			successor = this.successorCache.get(state).get(action);
		}
		else {
			Optional<INewNodeDescription<N, A>> succOpt = this.succGen.generateSuccessors(state).stream().filter(nd -> nd.getArcLabel().equals(action)).findAny();
			if (!succOpt.isPresent()) {
				this.logger.error("THERE IS NO SUCCESSOR REACHABLE WITH ACTION {} IN THE MDP!", action);
				return null;
			}
			successor = succOpt.get().getTo();
		}

		/* now equip this successor with probability 1 */
		Map<N, Double> out = new HashMap<>();
		out.put(successor, 1.0);
		return out;
	}

	@Override
	public double getProb(final N state, final A action, final N successor) throws InterruptedException {
		return this.getProb(state, action).containsKey(successor) ? 1 : 0.0;
	}

	@Override
	public Double getScore(final N state, final A action, final N successor) throws PathEvaluationException, InterruptedException {

		/* now build the whole path using the back-pointer map */
		this.logger.info("Getting score for SAS-triple ({}, {}, {})", state, action, successor);
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
			boolean isTerminal = this.isTerminalState(path.getHead());
			if (isTerminal) {
				this.logger.debug("Found dead end! Returning null.");
				return null;
			}
			this.logger.info("Path {} is not a goal path, returning 0.0", path);
			return 0.0;
		}
		this.logger.info("Path is a goal path, invoking path evaluator.");
		double score = this.graph.getPathEvaluator().evaluate(path).doubleValue();
		this.logger.info("Obtained score {} for path", score);
		return score;

	}

	@Override
	public boolean isTerminalState(final N state) throws InterruptedException {
		return this.getApplicableActions(state).isEmpty();
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
		if (this.graph.getGraphGenerator() instanceof ILoggingCustomizable) {
			this.logger.info("Setting logger of successor generator to {}.gg", name);
			((ILoggingCustomizable) this.graph.getGraphGenerator()).setLoggerName(name + ".gg");
		}
		if (this.goalTester instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) this.goalTester).setLoggerName(name + ".gt");
		}
		if (this.graph.getPathEvaluator() instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) this.graph.getPathEvaluator()).setLoggerName(name + ".pe");
		}
	}

	@Override
	public A getUniformlyRandomApplicableAction(final N state, final Random random) throws InterruptedException {
		if (this.succGen instanceof ILazyRandomizableSuccessorGenerator) {
			INewNodeDescription<N, A> ne = ((ILazyRandomizableSuccessorGenerator<N, A>) this.succGen).getIterativeGenerator(state, random).next();
			if (this.successorCache.size() > MAX_SUCCESSOR_CACHE_SIZE) {
				this.successorCache.clear();
			}
			this.successorCache.computeIfAbsent(state, n -> new HashMap<>()).put(ne.getArcLabel(), ne.getTo());
			return ne.getArcLabel();
		}
		this.logger.debug("The successor generator {} does not support lazy AND randomized successor generation. Now computing all successors and drawing one at random.", this.succGen.getClass());
		Collection<INewNodeDescription<N, A>> successors = this.succGen.generateSuccessors(state);
		if (successors.isEmpty()) {
			throw new IllegalArgumentException("The given node has no successors: " + state);
		}
		return SetUtil.getRandomElement(successors, random).getArcLabel();
	}

	@Override
	public boolean isActionApplicableInState(final N state, final A action) throws InterruptedException {
		if (this.successorCache.containsKey(state) && this.successorCache.get(state).containsKey(action)) {
			return true;
		}
		return this.getApplicableActions(state).contains(action);
	}
}
