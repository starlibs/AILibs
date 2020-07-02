package ai.libs.jaicore.search.algorithms.mdp.mcts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.stream.Collectors;

import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.common.attributedobjects.ObjectEvaluationFailedException;
import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.datastructure.graph.ILabeledPath;

import ai.libs.jaicore.basic.algorithm.AAlgorithm;
import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.probleminputs.IMDP;
import ai.libs.jaicore.search.probleminputs.MDPUtils;

public class MCTS<N, A> extends AAlgorithm<IMDP<N, A, Double>, IPolicy<N, A>> {

	private final IMDP<N, A, Double> mdp;
	private final int maxDepth;
	private final MDPUtils utils = new MDPUtils();
	private final IPathUpdatablePolicy<N, A, Double> treePolicy;
	private final IPolicy<N, A> defaultPolicy;
	private final double maxIterations;
	private int iterations = 0;
	private Collection<N> tpReadyStates = new HashSet<>();
	private Map<N, Queue<A>> untriedActionsOfIncompleteStates = new HashMap<>();
	private int lastProgressReport = 0;

	/* taboo management */
	private final boolean tabooExhaustedNodes;
	private Map<N, Collection<A>> tabooActions = new HashMap<>();

	private ILabeledPath<N, A> enforcedPrefixPath = null;

	public MCTS(final IMDP<N, A, Double> input, final IPathUpdatablePolicy<N, A, Double> treePolicy, final IPolicy<N, A> defaultPolicy, final double maxIterations, final double gamma, final double epsilon,
			final boolean tabooExhaustedNodes) {
		super(input);
		Objects.requireNonNull(input);
		Objects.requireNonNull(treePolicy);
		Objects.requireNonNull(defaultPolicy);
		this.mdp = input;
		this.treePolicy = treePolicy;
		this.defaultPolicy = defaultPolicy;
		this.maxIterations = maxIterations;
		this.maxDepth = MDPUtils.getTimeHorizon(gamma, epsilon);
		this.tabooExhaustedNodes = tabooExhaustedNodes;
	}

	@Override
	public IAlgorithmEvent nextWithException() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {
		switch (this.getState()) {
		case CREATED:
			this.logger.info("Initialized MCTS algorithm {}.", this.getClass().getName());
			return this.activate();
		case ACTIVE:
			if (this.iterations >= this.maxIterations) {
				this.logger.info("Number of iterations reached limit of {}.", this.maxIterations);
				return this.terminate();
			} else {
				try {

					this.iterations++;

					/* draw playout */
					this.logger.info("Draw next playout: #{}.", this.iterations);
					List<Double> scores = new ArrayList<>();
					ILabeledPath<N, A> path = new SearchGraphPath<>(this.mdp.getInitState());
					N current = path.getRoot();
					A action = null;
					int phase = 1;
					Collection<A> possibleActions;
					long lastTerminationCheck = 0;
					while (path.getNumberOfNodes() < this.maxDepth) {

						/* compute possible actions (this is done first since this may take long/timeout/interrupt, so that we check afterwards whether we are still active */
						possibleActions = this.mdp.getApplicableActions(current);

						/* make sure that we have not been canceled/timeouted/interrupted */
						long now = System.currentTimeMillis();
						if (now - lastTerminationCheck > 1000) {
							this.checkAndConductTermination();
							lastTerminationCheck = now;
						}

						/* determine possible actions */
						this.logger.debug("Found {} possible actions for state {}", possibleActions.size(), current);
						if (this.tabooExhaustedNodes) {
							Collection<A> tabooActionsForThisState = this.tabooActions.get(current);
							if (tabooActionsForThisState != null) {
								possibleActions = possibleActions.stream().filter(a -> !tabooActionsForThisState.contains(a)).collect(Collectors.toList());
								if (possibleActions.isEmpty()) {
									this.tabooLastActionOfPath(path);
								}
							}
						} else if (possibleActions.isEmpty()) {
							if (path.isPoint()) { // if we are in the root and cannot do anything anymore, then stop the algorithm.
								this.logger.info("There are no possible actions in the root. Finishing.");
								return this.terminate();
							}
							break;
						}

						/* first case */
						if (phase == 1 && this.tpReadyStates.contains(current)) {
							this.logger.debug("Ask tree policy to choose one action of: {}.", possibleActions);
							action = this.treePolicy.getAction(current, possibleActions);
							this.logger.debug("Tree policy recommended action {}.", action);
						} else {
							if (phase == 1) { // switch to next phase
								this.logger.debug("Switching to roll-out phase 2.");
								phase = 2;
							}
							if (phase == 2) {
								Queue<A> untriedActions;
								if (!this.untriedActionsOfIncompleteStates.containsKey(current)) {
									untriedActions = new LinkedList<>(possibleActions);
									if (untriedActions.isEmpty()) {
										this.treePolicy.updatePath(path, scores);
										IAlgorithmEvent event = new MCTSIterationCompletedEvent<>(this, this.treePolicy, new SearchGraphPath<>(path), scores);
										this.post(event);
										return event;
									}
									this.untriedActionsOfIncompleteStates.put(current, new LinkedList<>(possibleActions));
								} else {
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
								this.logger.debug("Switching to roll-out phase 3.");
							} else if (phase == 3) {
								this.logger.debug("Ask default policy to choose one action of: {}.", possibleActions);
								action = this.defaultPolicy.getAction(current, possibleActions);
								assert possibleActions.contains(action);
								this.logger.debug("Default policy chose action {}.", action);
							}
						}

						N nextState = this.utils.drawSuccessorState(this.mdp, current, action);
						scores.add(this.mdp.getScore(current, action, nextState));
						current = nextState;
						path.extend(current, action);
					}

					/* update tree policy with accumulated score */
					int progress = (int) Math.round(this.iterations * 100.0 / this.maxIterations);
					if (progress > this.lastProgressReport && progress % 5 == 0) {
						this.logger.info("Progress: {}%", Math.round(this.iterations * 100.0 / this.maxIterations));
						this.lastProgressReport = progress;
					}

					/* if we touched the ground with the tree policy, add the last action to the taboo list */
					if (this.tabooExhaustedNodes && phase == 1) {
						this.tabooLastActionOfPath(path);
					}

					/* create and publish roll-out event */
					double totalUndiscountedScore = scores.stream().reduce(0.0, (a, b) -> a.doubleValue() + b.doubleValue());
					this.logger.info("Found playout of length {} and (undiscounted) score {}", path.getNumberOfNodes(), totalUndiscountedScore);
					this.logger.debug("Found leaf node with score {}. Now propagating this score over the path with actions {}. Leaf state is: {}.", totalUndiscountedScore, path.getArcs(), path.getHead());
					if (!path.isPoint()) {
						this.treePolicy.updatePath(path, scores);
					}
					IAlgorithmEvent event = new MCTSIterationCompletedEvent<>(this, this.treePolicy, new SearchGraphPath<>(path), scores);
					this.post(event);
					return event;
				} catch (ActionPredictionFailedException | ObjectEvaluationFailedException e) {
					throw new AlgorithmException("Could not create playout!", e);
				}
			}
		default:
			throw new IllegalStateException("Don't know what to do in state " + this.getState());
		}
	}

	private void tabooLastActionOfPath(final ILabeledPath<N, A> path) {
		if (path.isPoint()) {
			throw new IllegalArgumentException("The path is a point, which has no first action to taboo.");
		}
		N lastStatePriorToEnd = path.getParentOfHead();
		A lastAction = path.getOutArc(lastStatePriorToEnd);
		this.tabooActions.computeIfAbsent(lastStatePriorToEnd, n -> new HashSet<>()).add(lastAction);
		this.logger.debug("Adding action {} to taboo list of state {}", lastAction, lastStatePriorToEnd);
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

	public ILabeledPath<N, A> getEnforcedPrefixPath() {
		return this.enforcedPrefixPath.getUnmodifiableAccessor();
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
