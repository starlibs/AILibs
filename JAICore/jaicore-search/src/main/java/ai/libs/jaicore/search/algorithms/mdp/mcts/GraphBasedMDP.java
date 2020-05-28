package ai.libs.jaicore.search.algorithms.mdp.mcts;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.datastructure.graph.implicit.ISingleRootGenerator;
import org.api4.java.datastructure.graph.implicit.ISuccessorGenerator;

import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.probleminputs.IMDP;

public class GraphBasedMDP<N, A> implements IMDP<N, A, Double> {

	private final IPathSearchWithPathEvaluationsInput<N, A, Double> graph;
	private final ISuccessorGenerator<N, A> succGen;

	public GraphBasedMDP(final IPathSearchWithPathEvaluationsInput<N, A, Double> graph) {
		super();
		this.graph = graph;
		this.succGen = graph.getGraphGenerator().getSuccessorGenerator();
	}

	@Override
	public N getInitState() {
		return ((ISingleRootGenerator<N>) this.graph.getGraphGenerator().getRootGenerator()).getRoot();
	}

	@Override
	public boolean isMaximizing() { // path searches are, by definition, always minimization problems
		return false;
	}

	@Override
	public Collection<A> getApplicableActions(final N state) throws InterruptedException {
		return this.succGen.generateSuccessors(state).stream().map(nd -> nd.getArcLabel()).collect(Collectors.toList());
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
		return this.graph.getPathEvaluator().evaluate(new SearchGraphPath<>(successor)).doubleValue();

	}

	@Override
	public boolean isTerminalState(final N state) throws InterruptedException {
		return this.getApplicableActions(state).isEmpty();
	}
}
