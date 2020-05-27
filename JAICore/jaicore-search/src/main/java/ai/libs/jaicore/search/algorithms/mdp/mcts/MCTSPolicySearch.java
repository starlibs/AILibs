package ai.libs.jaicore.search.algorithms.mdp.mcts;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.datastructure.graph.ILabeledPath;

import ai.libs.jaicore.basic.algorithm.AAlgorithm;
import ai.libs.jaicore.search.algorithms.mdp.mcts.old.ActionPredictionFailedException;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.probleminputs.IMDP;
import ai.libs.jaicore.search.probleminputs.MDPUtils;

public class MCTSPolicySearch<N, A> extends AAlgorithm<IMDP<N, A, Double>, IPolicy<N, A>> {

	private final IMDP<N, A, Double> mdp;
	private final double gamma;
	private final int maxDepth;
	private final MDPUtils utils = new MDPUtils();
	private final IPathUpdatablePolicy<N, A, Double> treePolicy;
	private final IPolicy<N, A> defaultPolicy;
	private final double maxIterations;
	private int iterations = 0;
	private Collection<N> tpReadyStates = new HashSet<>();
	private Map<N, Queue<A>> untriedActionsOfIncompleteStates = new HashMap<>();
	private DescriptiveStatistics pathScoreStats = new DescriptiveStatistics();
	private DescriptiveStatistics pathLengthStats = new DescriptiveStatistics();
	private int lastProgressReport = 0;

	private ILabeledPath<N, A> enforcedPrefixPath = null;

	public MCTSPolicySearch(final IMDP<N, A, Double> input, final IPathUpdatablePolicy<N, A, Double> treePolicy, final IPolicy<N, A> defaultPolicy, final double maxIterations, final double gamma, final double epsilon) {
		super(input);
		Objects.requireNonNull(input);
		Objects.requireNonNull(treePolicy);
		Objects.requireNonNull(defaultPolicy);
		this.mdp = input;
		this.treePolicy = treePolicy;
		this.defaultPolicy = defaultPolicy;
		this.maxIterations = maxIterations;
		this.gamma = gamma;
		this.maxDepth = gamma < 1 ? (int)Math.ceil(Math.log(epsilon) / Math.log(gamma)) : Integer.MAX_VALUE;
	}

	@Override
	public IAlgorithmEvent nextWithException() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {
		switch (this.getState()) {
		case CREATED:
			return this.activate();
		case ACTIVE:
			if (this.iterations >= this.maxIterations) {
				return this.terminate();
			}
			else {
				try {

					this.iterations ++;

					/* draw playout */
					this.logger.debug("Draw next playout.");
					double score = 0;
					ILabeledPath<N, A> path = new SearchGraphPath<>(this.mdp.getInitState());
					N current = path.getRoot();
					A action = null;
					int phase = 1;
					double discount = 1.0;
					Collection<A> possibleActions;
					while (path.getNumberOfNodes() < this.maxDepth) {

						/* determine possible actions */
						possibleActions = this.mdp.getApplicableActions(current);
						if (possibleActions.isEmpty()) {
							break;
						}

						/* first case */
						if (phase == 1 && this.tpReadyStates.contains(current)) {
							this.logger.debug("Ask tree policy to choose one action of: {}.", possibleActions);
							action = this.treePolicy.getAction(current, possibleActions);
							this.logger.debug("Tree policy recommended action {}.", action);
						}
						else {
							if (phase == 1) { // switch to next phase
								phase = 2;
							}
							if (phase == 2) {
								Queue<A> untriedActions;
								if (!this.untriedActionsOfIncompleteStates.containsKey(current)) {
									untriedActions = new LinkedList<>(possibleActions);
									if (untriedActions.isEmpty()) {
										this.treePolicy.updatePath(path, score);
										IAlgorithmEvent event = new MCTSIterationCompletedEvent<>(this, this.treePolicy, new EvaluatedSearchGraphPath<>(path, score));
										this.post(event);
										return event;
									}
									this.untriedActionsOfIncompleteStates.put(current, new LinkedList<>(possibleActions));
								}
								else {
									untriedActions = this.untriedActionsOfIncompleteStates.get(current);
								}
								assert !untriedActions.isEmpty() : "Untried actions must not be empty!";
								action = untriedActions.poll();
								if (untriedActions.isEmpty()) { // if this was the last untried action, remove it from that field and add it to the tree policy pool
									this.untriedActionsOfIncompleteStates.remove(current);
									this.tpReadyStates.add(current);
									this.logger.debug("Adding state {} to tree policy domain.", current);
								}
								phase = 3;
							}
							else if (phase == 3) {
								this.logger.debug("Ask default policy to choose one action of: {}.", possibleActions);
								action = this.defaultPolicy.getAction(current, possibleActions);
								assert possibleActions.contains(action);
								this.logger.debug("Default policy chose action {}.", action);
							}
						}

						N nextState = this.utils.drawSuccessorState(this.mdp, current, action);
						score += discount * this.mdp.getScore(current, action, nextState);
						discount *= this.gamma;
						current = nextState;
						path.extend(current, action);
					}

					/* update tree policy with accumulated score */
					//					if (score > 0) {
					//						System.err.println(score);
					//					}
					int progress = (int)Math.round(this.iterations * 100.0 / this.maxIterations);
					//					List<N> nodes = path.getNodes();
					//					List<A> arcs = path.getArcs();
					//					for (int i = 0; i < nodes.size(); i++) {
					//						System.out.println(nodes.get(i));
					//						if (i < arcs.size()) {
					//							System.out.println(arcs.get(i));
					//						}
					//					}
					//					System.out.println();
					//
					//					System.exit(0);
					if (progress > this.lastProgressReport && progress % 5 == 0) {
						this.logger.info("Progress: {}%" , Math.round(this.iterations * 100.0 / this.maxIterations));
						this.lastProgressReport = progress;
					}
					this.pathScoreStats.addValue(score);
					this.pathLengthStats.addValue(path.getNumberOfNodes());
					if (this.iterations % 1000 == 0) {
						//						System.out.println(this.pathScoreStats.getMean());
						//						this.pathScoreStats.clear();
						//						System.out.println(path.getNodes());
					}
					this.logger.debug("Found leaf node with score {}. Now propagating this score over the path with actions {}. Leaf state is: {}.", score, path.getArcs(), path.getHead());
					if (!path.isPoint()) {
						this.treePolicy.updatePath(path, score);
					}
					IAlgorithmEvent event = new MCTSIterationCompletedEvent<>(this, this.treePolicy, new EvaluatedSearchGraphPath<>(path, score));
					this.post(event);
					return event;
				}
				catch (ActionPredictionFailedException e) {
					throw new AlgorithmException("Could not create playout!", e);
				}
			}
		default:
			throw new IllegalStateException("Don't know what to do in state " + this.getState());
		}
	}

	public int getNumberOfRealizedPlayouts() {
		return this.iterations;
	}

	public IPathUpdatablePolicy<N, A, Double> getTreePolicy() {
		return this.treePolicy;
	}

	@Override
	public IPolicy<N, A> call() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {
		while (this.hasNext()) {
			this.nextWithException();
		}
		return this.treePolicy;
	}

	public void enforcePrefixPathOnAllRollouts(final ILabeledPath<N, A> path) {
		if (!path.getRoot().equals(this.mdp.getInitState())) {
			throw new IllegalArgumentException("Illegal prefix, since root does not coincide with algorithm root. Proposed root is: " + path.getRoot());
		}
		this.enforcedPrefixPath = path;
		N last = null;
		for (N node : path.getNodes()) {
			if (last != null) {
				this.tpReadyStates.remove(last);
				this.tpReadyStates.add(node);
			}
			last = node;
		}
		throw new UnsupportedOperationException("Currently, enforced prefixes are ignored!");
	}

	@Override
	public void setLoggerName(final String name) {
		super.setLoggerName(name);

		/* set logger of tree policy */
		if (this.treePolicy instanceof ILoggingCustomizable) {
			this.logger.info("Setting logger of tree policy to {}.treepolicy", name);
			((ILoggingCustomizable) this.treePolicy).setLoggerName(name + ".tp");
		} else {
			this.logger.info("Not setting logger of tree policy, because {} is not customizable.", this.treePolicy.getClass().getName());
		}

		/* set logger of default policy */
		if (this.defaultPolicy instanceof ILoggingCustomizable) {
			this.logger.info("Setting logger of default policy to {}.defaultpolicy", name);
			((ILoggingCustomizable) this.defaultPolicy).setLoggerName(name + ".dp");
		} else {
			this.logger.info("Not setting logger of default policy, because {} is not customizable.", this.defaultPolicy.getClass().getName());
		}
		this.utils.setLoggerName(name + ".utils");
	}


	public boolean hasTreePolicyReachedLeafs() {
		throw new UnsupportedOperationException("Currently not implemented.");
	}

}
