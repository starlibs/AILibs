package jaicore.search.algorithms.standard.bestfirst.nodeevaluation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.sets.SetUtil.Pair;
import jaicore.logging.LoggerUtil;
import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.SerializableGraphGenerator;
import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.SerializableNodeEvaluator;
import jaicore.search.algorithms.standard.bestfirst.StandardBestFirst;
import jaicore.search.algorithms.standard.bestfirst.events.GraphSearchSolutionCandidateFoundEvent;
import jaicore.search.algorithms.standard.bestfirst.events.NodeAnnotationEvent;
import jaicore.search.algorithms.standard.gbf.SolutionEventBus;
import jaicore.search.algorithms.standard.rdfs.RandomizedDepthFirstSearch;
import jaicore.search.algorithms.standard.uncertainty.IUncertaintySource;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.core.interfaces.ISolutionEvaluator;
import jaicore.search.core.interfaces.PathUnifyingGraphGenerator;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.model.probleminputs.GeneralEvaluatedTraversalTree;
import jaicore.search.model.travesaltree.Node;
import jaicore.search.structure.graphgenerator.SubGraphGenerator;

@SuppressWarnings("serial")
public class RandomCompletionBasedNodeEvaluator<T, V extends Comparable<V>> implements IGraphDependentNodeEvaluator<T, String, V>, SerializableNodeEvaluator<T, V>,
		ISolutionReportingNodeEvaluator<T, V>, ICancelableNodeEvaluator, IUncertaintyAnnotatingNodeEvaluator<T, V> {

	private final static Logger logger = LoggerFactory.getLogger(RandomCompletionBasedNodeEvaluator.class);

	private final int timeoutForSingleCompletionEvaluationInMS;
	private final int timeoutForNodeEvaluationInMS;

	protected Map<List<T>, List<T>> completions = new ConcurrentHashMap<>();
	protected Set<List<T>> unsuccessfulPaths = Collections.synchronizedSet(new HashSet<>());
	protected Set<List<T>> postedSolutions = new HashSet<>();
	protected Map<List<T>, Integer> timesToComputeEvaluations = new HashMap<>();

	protected Map<List<T>, V> scoresOfSolutionPaths = new ConcurrentHashMap<>();
	protected Map<Node<T, ?>, V> fValues = new ConcurrentHashMap<>();
	protected Map<String, Integer> ppFails = new ConcurrentHashMap<>();
	protected Map<String, Integer> plFails = new ConcurrentHashMap<>();
	protected Map<String, Integer> plSuccesses = new ConcurrentHashMap<>();

	protected SerializableGraphGenerator<T, String> generator;
	private boolean generatorProvidesPathUnification;
	protected long timestampOfFirstEvaluation;

	/* algorithm parameters */
	protected boolean pathCachingActivated = false;
	protected final Random random;
	protected int samples;

	protected final ISolutionEvaluator<T, V> solutionEvaluator;
	protected IUncertaintySource<T, V> uncertaintySource;
	protected transient SolutionEventBus<T> eventBus = new SolutionEventBus<>();

	public RandomCompletionBasedNodeEvaluator(final Random random, final int samples, final ISolutionEvaluator<T, V> solutionEvaluator) {
		this(random, samples, solutionEvaluator, -1, -1);
	}

	public RandomCompletionBasedNodeEvaluator(final Random random, final int samples, final ISolutionEvaluator<T, V> solutionEvaluator, int timeoutForSingleCompletionEvaluationInMS,
			int timeoutForNodeEvaluationInMS) {
		super();
		if (random == null) {
			throw new IllegalArgumentException("Random source must not be null!");
		}
		if (samples <= 0) {
			throw new IllegalArgumentException("Sample size must be greater than 0!");
		}
		if (solutionEvaluator == null) {
			throw new IllegalArgumentException("Solution evaluator must not be null!");
		}

		this.random = random;
		this.samples = samples;
		this.solutionEvaluator = solutionEvaluator;
		this.timeoutForSingleCompletionEvaluationInMS = timeoutForSingleCompletionEvaluationInMS;
		this.timeoutForNodeEvaluationInMS = timeoutForNodeEvaluationInMS;
		logger.info("Initialized RandomCompletionEvaluator with timeout {}ms for single evaluations and {}ms in total per node", timeoutForSingleCompletionEvaluationInMS, timeoutForNodeEvaluationInMS);

		/* check whether assertions are on */
		boolean assertOn = false;
		assert assertOn = true;
		if (assertOn) {
			System.out.println("--------------------------------------------------------");
			System.out.println("Attention: assertions are activated.");
			System.out.println("This causes significant performance loss using RandomCompleter.");
			System.out.println("If you are not in debugging mode, we strongly suggest to deactive assertions.");
			System.out.println("--------------------------------------------------------");
		}
	}

	protected double getExpectedUpperBoundForRelativeDistanceToOptimalSolution(final Node<T, ?> n, final List<T> path) {
		return 0.0;
	}

	@Override
	public V f(final Node<T, ?> n) throws Exception {
		long startOfComputation = System.currentTimeMillis();
		long deadline = timeoutForNodeEvaluationInMS > 0 ? startOfComputation + timeoutForNodeEvaluationInMS - 50 : Long.MAX_VALUE;
		if (this.timestampOfFirstEvaluation == 0) {
			this.timestampOfFirstEvaluation = startOfComputation;
		}
		logger.info("Received request for f-value of node {}", n);

		if (!this.fValues.containsKey(n)) {

			/* abort if not graph generator is set*/
			if (this.generator == null) {
				throw new IllegalStateException("Cannot compute f-values before the generator is set!");
			}

			/* compute path and partial plan belonging to the node */
			List<T> path = n.externalPath();

			/* annotate node with estimated relative distance to optimal solution */
			if (this.eventBus == null) { // this is necessary if the node evaluator was stored to the disc
				this.eventBus = new SolutionEventBus<>();
			}
			this.eventBus.post(new NodeAnnotationEvent<>(n.getPoint(), "EUBRD2OS", this.getExpectedUpperBoundForRelativeDistanceToOptimalSolution(n, path)));
			double uncertainty = 0.0;
			if (!n.isGoal()) {

				/* if there was no relevant change in comparison to parent, apply parent's f */
				if (path.size() > 1 && !this.solutionEvaluator.doesLastActionAffectScoreOfAnySubsequentSolution(path)) {
					assert this.fValues.containsKey(n
							.getParent()) : "The solution evaluator tells that the solution on the path has not significantly changed, but no f-value has been stored before for the parent. The path is: "
									+ path;
					V score = this.fValues.get(n.getParent());
					this.fValues.put(n, score);
					logger.info("Score {} of parent can be used since the last action did not affect the performance.", score);
					if (score == null)
						logger.warn("Returning score NULL inherited from parent, this should not happen.");
					return score;
				}

				/* create randomized dfs searcher */
				GeneralEvaluatedTraversalTree<T, String, Double> completionProblem = new GeneralEvaluatedTraversalTree<>(new SubGraphGenerator<>(generator, n.getPoint()), null);
				
//				new SimpleGraphVisualizationWindow<>(completer);

				/* draw random completions and determine best solution */
				V best = null;
				List<T> bestCompletion = null;
				int i = 0;
				int j = 0;
				final int maxSamples = this.samples * 2;
				List<V> evaluations = new ArrayList<>();
				List<List<T>> completedPaths = new ArrayList<>();
				for (; i < this.samples && !Thread.currentThread().isInterrupted() && System.currentTimeMillis() < deadline; i++) {

					/* complete the current path by the dfs-solution; we assume that this goes in almost constant time */
					List<T> completedPath = new ArrayList<>(n.externalPath());
					logger.info("Starting search for next solution ...");
					StandardBestFirst<T, String, Double> completer = new RandomizedDepthFirstSearch<>(completionProblem, this.random);
					EvaluatedSearchGraphPath<T, String, Double> solutionPathFromN = completer.nextSolution();
					if (solutionPathFromN == null) {
						logger.warn("No completion was found for path {}. Nodes expanded in search: {}", path, completer.getExpandedCounter());
						return null;
					}
					logger.info("Found solution {}", solutionPathFromN);
					List<T> pathCompletion = new ArrayList<>(solutionPathFromN.getNodes());
					pathCompletion.remove(0);
					completedPath.addAll(pathCompletion);
					completedPaths.add(completedPath);

					/* evaluate the found solution */
					AtomicBoolean nodeEvaluationTimedOut = new AtomicBoolean(false);
					Thread executingThread = Thread.currentThread();
					Timer timeoutTimer = new Timer();
					TimerTask abortionTask = new TimerTask() {
						
						@Override
						public void run() {
							
							/* if the executing thread has not been interrupted from outside */
							if (!executingThread.isInterrupted()) {
								logger.info("Sending an controlled interrupt to the evaluating thread to get it back here.");
								nodeEvaluationTimedOut.set(true);
								executingThread.interrupt();
							}
						}
					};
					long timeoutForJob = deadline - System.currentTimeMillis();
					if (timeoutForJob < 0)
						break;
					if (timeoutForSingleCompletionEvaluationInMS > 0 && timeoutForSingleCompletionEvaluationInMS < timeoutForJob)
						timeoutForJob = timeoutForSingleCompletionEvaluationInMS;
					timeoutTimer.schedule(abortionTask, timeoutForJob);
					j++;
					try {
						V val = this.getFValueOfSolutionPath(completedPath);
						if (val != null) {
							evaluations.add(val);
							if (best == null || val.compareTo(best) < 0) {
								best = val;
								bestCompletion = completedPath;
							}
						}
						else
							logger.warn("Got NULL result as score for path {}", completedPath);
					} catch (InterruptedException e) {
						boolean intentionalInterrupt = nodeEvaluationTimedOut.get();
						logger.info("Recognized {} interrupt", intentionalInterrupt ? "intentional" : "external");
						if (!intentionalInterrupt)
							throw e;
						else {
							Thread.interrupted(); // set interrupted to false
						}
					} catch (Exception ex) {
						if (j == maxSamples) {
							logger.warn("Too many retry attempts, giving up.");
							throw ex;
						} else {
							logger.error("Could not evaluate solution candidate ... retry another completion. {}", LoggerUtil.getExceptionInfo(ex));
							i--;
						}
					}
					timeoutTimer.cancel();
				}
				
				/* the only reason why we have no score at this point is that all evaluations have failed with exception or were interrupted */
				if (best == null) {
					if (deadline < System.currentTimeMillis())
						throw new TimeoutException("The timeout of " + timeoutForNodeEvaluationInMS + "ms for node evaluation has been exhausted.");
					else
						throw new NoSuchElementException("Among " + j + " evaluated candidates, we could not identify any candidate that did not throw an exception.");
				}
				
				/* if we are still interrupted, throw an exception */
				if (Thread.currentThread().isInterrupted())
					throw new InterruptedException("Node evaluation interrupted");

				/* add number of samples to node */
				n.setAnnotation("fRPSamples", i);
				if (uncertaintySource != null)
					uncertainty = this.uncertaintySource.calculateUncertainty((Node<T, V>) n, completedPaths, evaluations);
				
				/* cache this result and possible determine whether we already had a solution path that goes over this node before */
				if (pathCachingActivated) {
					if (generatorProvidesPathUnification) {
						Optional<List<T>> bestPreviouslyKnownPathOverThisNode = Optional.empty();
						AtomicBoolean interruptedInCheck = new AtomicBoolean();
						PathUnifyingGraphGenerator<T, ?> castedGenerator = (PathUnifyingGraphGenerator<T, ?>) generator;
						bestPreviouslyKnownPathOverThisNode = this.completions.keySet().stream().filter(p -> {
							try {
								if (interruptedInCheck.get())
									return false;
								return castedGenerator.isPathSemanticallySubsumed(path, p);
							} catch (InterruptedException e) {
								interruptedInCheck.set(true);
								return false;
							}
						}).min((p1, p2) -> this.scoresOfSolutionPaths.get(p1).compareTo(this.scoresOfSolutionPaths.get(p2)));
						if (interruptedInCheck.get())
							throw new InterruptedException("Node evaluation interrupted");
						if (bestPreviouslyKnownPathOverThisNode.isPresent() && this.scoresOfSolutionPaths.get(bestPreviouslyKnownPathOverThisNode.get()).compareTo(best) < 0) {
							bestCompletion = bestPreviouslyKnownPathOverThisNode.get();
							best = this.scoresOfSolutionPaths.get(bestCompletion);
						}
					}
					this.completions.put(path, bestCompletion);
				}
				this.fValues.put(n, best);
			}

			/* the node is a goal node */
			else {
				V score = this.getFValueOfSolutionPath(path);
				if (score == null) {
					logger.warn("No score was computed");
					return null;
				}
				this.fValues.put(n, score);
				if (!this.postedSolutions.contains(path)) {
					logger.error("Found a goal node whose solution has not been posted before!");
				}
				uncertainty = 0.0;
			}

			/* set uncertainty if an uncertainty source has been set */
			if (uncertaintySource != null)
				n.setAnnotation("uncertainty", uncertainty);
		}
		V f = this.fValues.get(n);
		logger.info("Returning f-value: {}", f);
		return f;
	}

	protected V getFValueOfSolutionPath(final List<T> path) throws Exception {

		boolean knownPath = this.scoresOfSolutionPaths.containsKey(path);
		if (!knownPath) {
			if (this.unsuccessfulPaths.contains(path)) {
				logger.warn("Asking again for the reevaluation of a path that was evaluated unsuccessfully in a previous run; returning NULL: {}", path);
				return null;
			}
			logger.info("Associated plan is new. Calling solution evaluator {} to compute f-value for complete path {}", solutionEvaluator, path);

			/* compute value of solution */
			long start = System.currentTimeMillis();
			V val = null;
			try {
				val = this.solutionEvaluator.evaluateSolution(path);
			} catch (InterruptedException e) {
				logger.info("Computing the solution quality of {} was interrupted.", path);
				throw e;
			} catch (Throwable e) {
				logger.error("Computing the solution quality of {} failed due to an exception. Here is the trace:\n\t{}\n\t{}\n\t{}", path, e.getClass().getName(), e.getMessage(),
						Arrays.asList(e.getStackTrace()).stream().map(n -> "\n\t" + n.toString()).collect(Collectors.toList()));
				this.unsuccessfulPaths.add(path);
				throw e;
			}
			long duration = System.currentTimeMillis() - start;
			
			/* at this point, the value should not be NULL */
			logger.info("Result: {}, Size: {}", val, this.scoresOfSolutionPaths.size());
			if (val == null) {
				logger.warn("The solution evaluator has returned NULL, which should not happen.");
				this.unsuccessfulPaths.add(path);
				return null;
			}

			this.scoresOfSolutionPaths.put(path, val);
			this.timesToComputeEvaluations.put(path, (int) duration);
			this.postSolution(path);
		} else {
			logger.info("Associated plan is known. Reading score from cache.");
			if (logger.isTraceEnabled()) {
				for (List<T> existingPath : this.scoresOfSolutionPaths.keySet()) {
					if (existingPath.equals(path)) {
						logger.trace("The following plans appear equal:\n\t{}\n\t{}", existingPath, path);
					}
				}
			}
			if (!this.postedSolutions.contains(path)) {
				throw new IllegalStateException("Reading cached score of a plan whose path has not been posted as a solution! Are there several paths to a plan?");
			}
		}
		V score = this.scoresOfSolutionPaths.get(path);
		assert score != null : "Stored scores must never be null";
		logger.info("Determined value {} for path {}.", score, path);
		return score;
	}

	protected void postSolution(final List<T> solution) {
		if (this.postedSolutions.contains(solution)) {
			throw new IllegalArgumentException("Solution " + solution.toString() + " already posted!");
		}
		this.postedSolutions.add(solution);
		// List<CEOCAction> plan = CEOCSTNUtil.extractPlanFromSolutionPath(solution);
		try {

			/* now post the solution to the event bus */
			int numberOfComputedFValues = this.scoresOfSolutionPaths.size();

			/* post solution and then the annotations */
			if (this.eventBus == null) {
				this.eventBus = new SolutionEventBus<>();
			}
			EvaluatedSearchGraphPath<T, ?, V> solutionObject = new EvaluatedSearchGraphPath<>(solution, null, this.scoresOfSolutionPaths.get(solution));
			solutionObject.setAnnotation("fTime", this.timesToComputeEvaluations.get(solution));
			solutionObject.setAnnotation("timeToSolution", (int) (System.currentTimeMillis() - this.timestampOfFirstEvaluation));
			solutionObject.setAnnotation("nodesEvaluatedToSolution", numberOfComputedFValues);
			this.eventBus.post(new GraphSearchSolutionCandidateFoundEvent<>(solutionObject));
		} catch (Throwable e) {
			List<Pair<String, Object>> explanations = new ArrayList<>();
			if (logger.isDebugEnabled()) {
				StringBuilder sb = new StringBuilder();
				solution.forEach(n -> sb.append(n.toString() + "\n"));
				explanations.add(new Pair<>("The path that has been tried to convert is as follows:", sb.toString()));
			}
			logger.error("Cannot post solution, because no valid MLPipeline object could be derived from it:\n{}", LoggerUtil.getExceptionInfo(e, explanations));
		}
	}

	@Override
	public void setGenerator(final GraphGenerator<T, String> generator) {
		this.generator = (SerializableGraphGenerator<T, String>) generator;
		this.generatorProvidesPathUnification = (this.generator instanceof PathUnifyingGraphGenerator);
		if (!this.generatorProvidesPathUnification)
			logger.warn("The graph generator passed to the RandomCompletion algorithm does not offer path subsumption checks, which may cause inefficiencies in some domains.");
	}

	@Override
	public void registerSolutionListener(Object listener) {
		eventBus.register(listener);
	}

	@Override
	public void cancel() {
		logger.info("Receive cancel signal.");
	}

	public void setNumberOfRandomCompletions(final int randomCompletions) {
		this.samples = randomCompletions;
	}

	public boolean isPathCachingActivated() {
		return pathCachingActivated;
	}

	public void setPathCachingActivated(boolean pathCachingActivated) {
		this.pathCachingActivated = pathCachingActivated;
	}

	@Override
	public void setUncertaintySource(IUncertaintySource<T, V> uncertaintySource) {
		this.uncertaintySource = uncertaintySource;
	}
}
