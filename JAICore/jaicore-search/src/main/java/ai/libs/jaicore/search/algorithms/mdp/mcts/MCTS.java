package ai.libs.jaicore.search.algorithms.mdp.mcts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.api4.java.algorithm.Timeout;
import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.common.attributedobjects.ObjectEvaluationFailedException;
import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.common.event.IEvent;
import org.api4.java.common.event.IRelaxedEventEmitter;
import org.api4.java.datastructure.graph.ILabeledPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import ai.libs.jaicore.basic.algorithm.AAlgorithm;
import ai.libs.jaicore.graphvisualizer.events.graph.GraphInitializedEvent;
import ai.libs.jaicore.graphvisualizer.events.graph.NodeAddedEvent;
import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.probleminputs.IMDP;
import ai.libs.jaicore.search.probleminputs.MDPUtils;
import ai.libs.jaicore.timing.TimedComputation;

/**
 *
 * @author Felix Mohr
 *
 * @param <N>
 *            Type of states (nodes)
 * @param <A>
 *            Type of actions
 */
public class MCTS<N, A> extends AAlgorithm<IMDP<N, A, Double>, IPolicy<N, A>> {

	private Logger logger = LoggerFactory.getLogger(MCTS.class);
	private static final Runtime runtime = Runtime.getRuntime();

	private final IMDP<N, A, Double> mdp;
	private final int maxDepth;
	private final MDPUtils utils = new MDPUtils();
	private final IPathUpdatablePolicy<N, A, Double> treePolicy;
	private final IPolicy<N, A> defaultPolicy;
	private final boolean uniformSamplingDefaultPolicy;
	private final Random randomSourceOfUniformSamplyPolicy;
	private final int maxIterations;

	/* variables describing the state of the search */
	private int iterations = 0;
	private final Collection<N> tpReadyStates = new HashSet<>();
	private final Map<N, Collection<A>> applicableActionsPerState = new HashMap<>();
	private final Map<N, List<A>> untriedActionsOfIncompleteStates = new HashMap<>();
	private int lastProgressReport = 0;

	/* stats variables */
	private int msSpentInRollouts;
	private int msSpentInTreePolicyQueries;
	private int msSpentInTreePolicyUpdates;

	/* taboo management */
	private final boolean tabooExhaustedNodes;
	private Map<N, Collection<A>> tabooActions = new HashMap<>();

	private ILabeledPath<N, A> enforcedPrefixPath = null;

	public MCTS(final IMDP<N, A, Double> input, final IPathUpdatablePolicy<N, A, Double> treePolicy, final IPolicy<N, A> defaultPolicy, final int maxIterations, final double gamma, final double epsilon, final boolean tabooExhaustedNodes) {
		super(input);
		Objects.requireNonNull(input);
		Objects.requireNonNull(treePolicy);
		Objects.requireNonNull(defaultPolicy);
		this.mdp = input;
		this.treePolicy = treePolicy;
		this.defaultPolicy = defaultPolicy;
		this.uniformSamplingDefaultPolicy = defaultPolicy instanceof UniformRandomPolicy;
		this.randomSourceOfUniformSamplyPolicy = this.uniformSamplingDefaultPolicy ? ((UniformRandomPolicy<?, ?, ?>) defaultPolicy).getRandom() : null;
		this.maxIterations = maxIterations;
		this.maxDepth = MDPUtils.getTimeHorizon(gamma, epsilon);
		this.tabooExhaustedNodes = tabooExhaustedNodes;

		/* forward event of tree policy or default policy if they send some */
		if (treePolicy instanceof IRelaxedEventEmitter) {
			((IRelaxedEventEmitter) treePolicy).registerListener(new Object() {

				@Subscribe
				public void receiveEvent(final IEvent event) {
					MCTS.this.post(event);
				}
			});
		}
	}

	public List<A> getPotentialActions(final ILabeledPath<N, A> path, final Collection<A> applicableActions) {
		N current = path.getHead();
		List<A> possibleActions = new ArrayList<>(applicableActions);
		if (possibleActions.isEmpty()) {
			this.logger.warn("Computing potential actions for an empty set of applicable actions makes no sense! Returning an empty set for node {}.", current);
			return possibleActions;
		}

		/* determine possible actions */
		this.logger.debug("Computing potential actions based on {} applicable ones for state {}", applicableActions.size(), current);
		if (this.tabooExhaustedNodes) {
			Collection<A> tabooActionsForThisState = this.tabooActions.get(current);
			this.logger.debug("Found {} tabooed actions for this state.", tabooActionsForThisState != null ? tabooActionsForThisState.size() : 0);
			if (tabooActionsForThisState != null) {
				possibleActions = possibleActions.stream().filter(a -> !tabooActionsForThisState.contains(a)).collect(Collectors.toList());
			}
			if (possibleActions.isEmpty() && path.getNumberOfNodes() > 1) { // otherwise we are in the root and the thing ends
				this.tabooLastActionOfPath(path);
			}
		}
		return possibleActions;
	}

	private Collection<A> getApplicableActions(final N state) throws AlgorithmTimeoutedException, ExecutionException, InterruptedException, AlgorithmExecutionCanceledException {
		Timeout toForSuccessorComputation = new Timeout(this.getRemainingTimeToDeadline().milliseconds() - 1000, TimeUnit.MILLISECONDS);
		this.logger.debug("Computing all applicable actions with timeout {}.", toForSuccessorComputation);
		try {
			Collection<A> applicableActions = Collections.unmodifiableCollection(TimedComputation.compute(() -> this.mdp.getApplicableActions(state), toForSuccessorComputation, "Timeout bound hit."));
			this.logger.debug("Number of applicable actions is {}", applicableActions.size());
			return applicableActions;
		}
		catch (InterruptedException e) {
			this.checkAndConductTermination(); // check whether we have been canceled internally
			throw e; // otherwise just throw new Interrupted exception
		}
	}

	@Override
	public IAlgorithmEvent nextWithException() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {
		this.logger.debug("Stepping MCTS with thread {}", Thread.currentThread());
		this.registerActiveThread();
		try {
			switch (this.getState()) {
			case CREATED:
				this.logger.info("Initialized MCTS algorithm {}.\n\tTree Policy: {}\n\tDefault Policy: {}\n\tMax Iterations: {}\n\tMax Depth: {}\n\tTaboo Exhausted Nodes: {}", this.getClass().getName(), this.treePolicy, this.defaultPolicy,
						this.maxIterations, this.maxDepth, this.tabooExhaustedNodes);
				return this.activate();
			case ACTIVE:
				if (this.iterations >= this.maxIterations) {
					this.logger.info("Number of iterations reached limit of {}.", this.maxIterations);
					return this.terminate();
				} else {
					try {
						long timeStart = System.currentTimeMillis();
						this.iterations++;

						/* if the number of (estimated) remaining rollouts is relevant for the tree policy, tell it */
						if (this.treePolicy instanceof IRolloutLimitDependentPolicy && this.isTimeoutDefined()) {
							double avgTimeOfRollouts = this.msSpentInRollouts * 1.0 / this.iterations;
							int expectedRemainingNumberOfRollouts = (int) Math.floor(this.getRemainingTimeToDeadline().milliseconds() / avgTimeOfRollouts);
							((IRolloutLimitDependentPolicy) this.treePolicy).setEstimatedNumberOfRemainingRollouts(expectedRemainingNumberOfRollouts);
						}

						/* draw playout */
						this.logger.info("Draw next playout: #{}.", this.iterations);
						int invocationsOfTreePolicyInThisIteration = 0;
						int invocationsOfDefaultPolicyInThisIteration = 0;
						long timeSpentInActionApplicabilityComputationThisIteration = 0;
						long timeSpentInSuccessorGenerationThisIteration = 0;
						long timeSpentInTreePolicyQueriesThisIteration = 0;
						long timeSpentInTreePolicyUpdatesThisIteration = 0;
						long timeSpentInDefaultPolicyThisIteration = 0;
						List<Double> scores = new ArrayList<>();
						ILabeledPath<N, A> path = new SearchGraphPath<>(this.mdp.getInitState());
						N current = path.getRoot();
						A action = null;
						int phase = 1;
						long lastTerminationCheck = 0;
						int depth = 0;
						while (path.getNumberOfNodes() < this.maxDepth) {

							/* check whether this is a terminal state, and check this within a try-block to catch a potential interrupt */
							try {
								if (this.mdp.isTerminalState(current)) {
									break;
								}
							}
							catch (InterruptedException e) {
								this.checkAndConductTermination(); // if we have been canceled, throw the corresponding exception
								throw e; // otherwise re-throw the InterruptedException
							}

							this.logger.debug("Now extending the roll-out in depth {}", depth);
							depth++;

							/* make sure that we have not been canceled/timeouted/interrupted */
							long now = System.currentTimeMillis();
							if (now - lastTerminationCheck > 1000) {
								this.checkAndConductTermination();
								lastTerminationCheck = now;
							}

							/* first case: Tree policy can be applied */
							if (phase == 1 && this.tpReadyStates.contains(current)) {

								/* here we assume that the set of applicable actions is stored in memory, and we just compute the subset of them for the case that taboo is active */
								this.logger.debug("Computing possible actions for node {}", current);
								assert this.applicableActionsPerState.containsKey(current) && !this.applicableActionsPerState.get(current).isEmpty() : "It makes no sense to apply the TP to a node without applicable actions!";
								List<A> possibleActions = this.getPotentialActions(path, this.applicableActionsPerState.get(current));
								if (possibleActions.isEmpty()) {
									if (path.isPoint()) { // if we are in the root and cannot do anything anymore, then stop the algorithm.
										this.logger.info("There are no possible actions in the root. Finishing.");
										this.summarizeIteration(System.currentTimeMillis() - timeStart, timeSpentInActionApplicabilityComputationThisIteration, timeSpentInSuccessorGenerationThisIteration, invocationsOfTreePolicyInThisIteration,
												invocationsOfDefaultPolicyInThisIteration, timeSpentInTreePolicyQueriesThisIteration, timeSpentInTreePolicyUpdatesThisIteration, timeSpentInDefaultPolicyThisIteration);
										return this.terminate();
									}
									break;
								}

								this.logger.debug("Ask tree policy to choose one action of: {}.", possibleActions);
								long tpStart = System.currentTimeMillis();
								try {
									action = this.treePolicy.getAction(current, possibleActions);
								}
								catch (InterruptedException e) {
									this.checkAndConductTermination(); // if we have been canceled, throw the corresponding exception
									throw e; // otherwise re-throw the InterruptedException
								}
								timeSpentInTreePolicyQueriesThisIteration += (System.currentTimeMillis() - tpStart);
								invocationsOfTreePolicyInThisIteration++;
								Objects.requireNonNull(action, "Actions in MCTS must never be null, but tree policy returned null!");
								this.logger.debug("Tree policy recommended action {}.", action);
							} else {
								if (phase == 1) { // switch to next phase
									this.logger.debug("Switching to roll-out phase 2.");
									phase = 2;
								}
								if (phase == 2) { // this phase is for the first node on the path that is not TP ready. This node has (unless it is a leaf) untried actions

									/* compute the actions that have not been tried for this node */
									List<A> untriedActions;
									if (!this.untriedActionsOfIncompleteStates.containsKey(current)) { // if this is the first time we see this node, compute *all* its successors

										/* compute possible actions (this is done first since this may take long/timeout/interrupt, so that we check afterwards whether we are still active */
										long startActionTime = System.currentTimeMillis();
										if (this.getRemainingTimeToDeadline().milliseconds() < 2000) {
											if (this.getRemainingTimeToDeadline().milliseconds() > 0) {
												Thread.sleep(this.getRemainingTimeToDeadline().milliseconds());
											}
											this.checkAndConductTermination();
										}
										Collection<A> applicableActions = this.getApplicableActions(current);
										untriedActions = new ArrayList<>(applicableActions);
										timeSpentInActionApplicabilityComputationThisIteration += (System.currentTimeMillis() - startActionTime);
										this.applicableActionsPerState.put(current, applicableActions);

										/* if there are no applicable actions for this node (dead-end) conduct back-propagation */
										if (untriedActions.isEmpty()) {
											long tpStart = System.currentTimeMillis();
											this.treePolicy.updatePath(path, scores);
											timeSpentInTreePolicyUpdatesThisIteration += (System.currentTimeMillis() - tpStart);
											IAlgorithmEvent event = new MCTSIterationCompletedEvent<>(this, this.treePolicy, new SearchGraphPath<>(path), scores);
											this.post(event);
											this.summarizeIteration(System.currentTimeMillis() - timeStart, timeSpentInActionApplicabilityComputationThisIteration, timeSpentInSuccessorGenerationThisIteration,
													invocationsOfTreePolicyInThisIteration, invocationsOfDefaultPolicyInThisIteration, timeSpentInTreePolicyQueriesThisIteration, timeSpentInTreePolicyUpdatesThisIteration,
													timeSpentInDefaultPolicyThisIteration);
											return event;
										}
										this.untriedActionsOfIncompleteStates.put(current, untriedActions);
									} else {
										untriedActions = this.untriedActionsOfIncompleteStates.get(current);
									}

									/* now remove the first untried action from the list */
									this.logger.debug("There are {} untried actions: {}", untriedActions.size(), untriedActions);
									assert !untriedActions.isEmpty() : "Untried actions must not be empty!";
									action = untriedActions.remove(0);
									this.logger.debug("Choosing untried action {}. There are {} remaining untried actions: {}", action, untriedActions.size(), untriedActions);
									Objects.requireNonNull(action, "Actions in MCTS must never be null!");

									/* if this was the last untried action, remove it from that field and add it to the tree policy pool */
									if (untriedActions.isEmpty()) {
										this.untriedActionsOfIncompleteStates.remove(current);
										this.tpReadyStates.add(current);
										if (path.isPoint()) {
											this.post(new GraphInitializedEvent<>(this, current));
										} else {
											this.post(new NodeAddedEvent<>(this, path.getPathToParentOfHead().getHead(), current, "none"));
										}
										this.logger.debug("Adding state {} to tree policy domain.", current);
									}
									phase = 3;
									this.logger.debug("Switching to roll-out phase 3.");
								} else if (phase == 3) {

									long startDP = System.currentTimeMillis();

									/* if the default policy is a uniform sampler, just directly ask the MDP */
									if (this.uniformSamplingDefaultPolicy) {
										this.logger.debug("Sample a single action directly from the MDP.");
										try {
											action = this.mdp.getUniformlyRandomApplicableAction(current, this.randomSourceOfUniformSamplyPolicy);
										}
										catch (InterruptedException e) {
											this.checkAndConductTermination(); // if we have been canceled, throw the corresponding exception
											throw e; // otherwise re-throw the InterruptedException
										}
									} else {

										/* determine possible actions and ask default policy which one to choose */
										long startActionTime = System.currentTimeMillis();
										Collection<A> applicableActions = this.getApplicableActions(current);
										timeSpentInActionApplicabilityComputationThisIteration += (System.currentTimeMillis() - startActionTime);
										this.logger.debug("Ask default policy to choose one action of: {}.", applicableActions);
										try {
											action = this.defaultPolicy.getAction(current, applicableActions);
										}
										catch (InterruptedException e) {
											this.checkAndConductTermination(); // if we have been canceled, throw the corresponding exception
											throw e; // otherwise re-throw the InterruptedException
										}
										assert applicableActions.contains(action);
									}
									timeSpentInDefaultPolicyThisIteration += (System.currentTimeMillis() - startDP);
									invocationsOfDefaultPolicyInThisIteration++;
									Objects.requireNonNull(action, "Actions in MCTS must never be null, but default policy has returned null!");
									this.logger.debug("Default policy chose action {}.", action);
								} else {
									throw new IllegalStateException("Invalid phase " + phase);
								}
							}

							/* we now have the action chosen for this node. Now draw a successor state */
							try {
								long startSuccessorComputation = System.currentTimeMillis();
								N nextState = this.utils.drawSuccessorState(this.mdp, current, action);
								timeSpentInSuccessorGenerationThisIteration += System.currentTimeMillis() - startSuccessorComputation;
								scores.add(this.mdp.getScore(current, action, nextState));
								current = nextState;
								path.extend(current, action);
							}
							catch (InterruptedException e) {
								this.checkAndConductTermination(); // if we have been canceled, throw the corresponding exception
								throw e; // otherwise re-throw the InterruptedException
							}
						}

						/* if we touched the ground with the tree policy, add the last action to the taboo list */
						if (this.tabooExhaustedNodes && phase == 1) {
							this.tabooLastActionOfPath(path);
						}

						/* decide whether to show a progress report */
						int progress = (int) Math.round(this.iterations * 100.0 / this.maxIterations);
						if (progress > this.lastProgressReport && progress % 5 == 0) {
							this.logger.info("Progress: {}%", Math.round(this.iterations * 100.0 / this.maxIterations));
							this.lastProgressReport = progress;
						}

						boolean hasNullScore = scores.contains(null);
						// if (hasNullScore) {
						// this.logger.warn("Found playout with null-score. Ignoring this run.");
						// this.summarizeIteration(System.currentTimeMillis() - timeStart, timeSpentInActionApplicabilityComputationThisIteration, timeSpentInSuccessorGenerationThisIteration, invocationsOfTreePolicyInThisIteration,
						// invocationsOfDefaultPolicyInThisIteration, timeSpentInTreePolicyQueriesThisIteration, timeSpentInTreePolicyUpdatesThisIteration, timeSpentInDefaultPolicyThisIteration);
						// return this.nextWithException();
						// }

						/* create and publish roll-out event */
						boolean isGoalPath;
						try {
							isGoalPath = this.mdp.isTerminalState(path.getHead());
						}
						catch (InterruptedException e) {
							this.checkAndConductTermination(); // if we have been canceled, throw the corresponding exception
							throw e; // otherwise re-throw the InterruptedException
						}
						double totalUndiscountedScore = hasNullScore ? Double.NaN : scores.stream().reduce(0.0, (a, b) -> a.doubleValue() + b.doubleValue());
						this.logger.info("Found playout of length {}. Head is goal: {}. (Undiscounted) score of path is {}.", path.getNumberOfNodes(), isGoalPath, totalUndiscountedScore);
						this.logger.debug("Found leaf node with score {}. Now propagating this score over the path with actions {}. Leaf state is: {}.", totalUndiscountedScore, path.getArcs(), path.getHead());
						if (!path.isPoint()) {
							long tpStart = System.currentTimeMillis();
							this.treePolicy.updatePath(path, scores);
							timeSpentInTreePolicyUpdatesThisIteration += (System.currentTimeMillis() - tpStart);
						}
						IAlgorithmEvent event = new MCTSIterationCompletedEvent<>(this, this.treePolicy, new SearchGraphPath<>(path), scores);
						this.post(event);
						this.summarizeIteration(System.currentTimeMillis() - timeStart, timeSpentInActionApplicabilityComputationThisIteration, timeSpentInSuccessorGenerationThisIteration, invocationsOfTreePolicyInThisIteration,
								invocationsOfDefaultPolicyInThisIteration, timeSpentInTreePolicyQueriesThisIteration, timeSpentInTreePolicyUpdatesThisIteration, timeSpentInDefaultPolicyThisIteration);
						return event;
					} catch (ActionPredictionFailedException | ObjectEvaluationFailedException e) {
						throw new AlgorithmException("Could not create playout due to an exception! MCTS cannot deal with this in general. Please modify your MDP such that this kind of exceptions is resolved to some kind of score.", e);
					} catch (ExecutionException e) {
						throw new AlgorithmException("Observed error during timed computation.", e);
					}
				}
			default:
				throw new IllegalStateException("Don't know what to do in state " + this.getState());
			}
		}
		finally {
			this.logger.debug("Unregistering thread {}", Thread.currentThread());
			this.unregisterActiveThread();
		}
	}

	private void summarizeIteration(final long timeForRolloutThisIteration, final long timeSpentInActionApplicability, final long timeSpentInSuccessorGenerationThisIteration, final int numInvocationsOfTP, final int numInvocationsOfDP,
			final long timeSpentInTreePolicyQueriesThisIteration, final long timeSpentInTreePolicyUpdatesThisIteration, final long timeSpentInDefaultPolicyThisIteration) {
		this.msSpentInRollouts += timeForRolloutThisIteration;
		this.msSpentInTreePolicyQueries += timeSpentInTreePolicyQueriesThisIteration;
		this.msSpentInTreePolicyUpdates += timeSpentInTreePolicyUpdatesThisIteration;
		this.logger.info(
				"Finished rollout in {}ms. Time for computing applicable actions was {}ms and for computing successors {}ms. Time for TP {} queries was {}ms, time to update TP {}ms, time for {} DP queries was {}ms. Currently used memory: {}MB.",
				timeForRolloutThisIteration, timeSpentInActionApplicability, timeSpentInSuccessorGenerationThisIteration, numInvocationsOfTP, timeSpentInTreePolicyQueriesThisIteration, timeSpentInTreePolicyUpdatesThisIteration,
				numInvocationsOfDP, timeSpentInDefaultPolicyThisIteration, (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024));
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
		this.logger = LoggerFactory.getLogger(name);
		super.setLoggerName(name + ".abstract");

		/* set logger name in MDP */
		if (this.mdp instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) this.mdp).setLoggerName(name + ".mdp");
		}

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

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	public int getNumberOfNodesInMemory() {
		return this.tpReadyStates.size();
	}

	public int getMsSpentInRollouts() {
		return this.msSpentInRollouts;
	}

	public int getMsSpentInTreePolicyQueries() {
		return this.msSpentInTreePolicyQueries;
	}

	public int getMsSpentInTreePolicyUpdates() {
		return this.msSpentInTreePolicyUpdates;
	}

	public boolean isTabooExhaustedNodes() {
		return this.tabooExhaustedNodes;
	}
}
