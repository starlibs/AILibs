package jaicore.search.algorithms.standard.bestfirst;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.SetUtil.Pair;
import jaicore.logging.LoggerUtil;
import jaicore.search.algorithms.interfaces.IPathUnification;
import jaicore.search.algorithms.interfaces.ISolutionEvaluator;
import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.SerializableGraphGenerator;
import jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces.SerializableNodeEvaluator;
import jaicore.search.algorithms.standard.core.ICancelableNodeEvaluator;
import jaicore.search.algorithms.standard.core.IGraphDependentNodeEvaluator;
import jaicore.search.algorithms.standard.core.ISolutionReportingNodeEvaluator;
import jaicore.search.algorithms.standard.core.NodeAnnotationEvent;
import jaicore.search.algorithms.standard.core.SolutionAnnotationEvent;
import jaicore.search.algorithms.standard.core.SolutionEventBus;
import jaicore.search.algorithms.standard.core.SolutionFoundEvent;
import jaicore.search.algorithms.standard.rdfs.RandomizedDepthFirstSearch;
import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.Node;
import jaicore.search.structure.graphgenerator.GoalTester;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

@SuppressWarnings("serial")
public class RandomCompletionEvaluator<T,V extends Comparable<V>> implements IGraphDependentNodeEvaluator<T, String, V>, SerializableNodeEvaluator<T, V>, ISolutionReportingNodeEvaluator<T, V>, ICancelableNodeEvaluator {

	private final static Logger logger = LoggerFactory.getLogger(RandomCompletionEvaluator.class);
	private Map<List<T>, List<T>> completions = new ConcurrentHashMap<>();
	private Set<List<T>> unsuccessfulPaths = Collections.synchronizedSet(new HashSet<>());
	private Set<List<T>> postedSolutions = new HashSet<>();
	private Map<List<T>, Integer> timesToComputeEvaluations = new HashMap<>();
	
	private Map<List<T>, V> scoresOfSolutionPaths = new ConcurrentHashMap<>();
	protected Map<Node<T, ?>, V> fValues = new ConcurrentHashMap<>();
	protected Map<String, Integer> ppFails = new ConcurrentHashMap<>();
	protected Map<String, Integer> plFails = new ConcurrentHashMap<>();
	protected Map<String, Integer> plSuccesses = new ConcurrentHashMap<>();

	private final IPathUnification<T> pathUnifier;
	private SerializableGraphGenerator<T, String> generator;
	private long timestampOfFirstEvaluation;
	private final Random random;
	protected final int samples;
	protected final ISolutionEvaluator<T,V> solutionEvaluator;
	private transient SolutionEventBus<T> eventBus;

	public RandomCompletionEvaluator(Random random, int samples, IPathUnification<T> pathUnifier, ISolutionEvaluator<T,V> solutionEvaluator) {
		super();
		if (random == null)
			throw new IllegalArgumentException("Random source must not be null!");
		if (samples <= 0)
			throw new IllegalArgumentException("Sample size must be greater than 0!");
		if (solutionEvaluator == null)
			throw new IllegalArgumentException("Solution evaluator must not be null!");
		this.pathUnifier = pathUnifier;
		this.random = random;
		this.samples = samples;
		this.solutionEvaluator = solutionEvaluator;
		
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

	protected V computeEvaluationPriorToCompletion(Node<T, ?> n, List<T> path) throws Throwable {
		return null;
	}

	protected V convertErrorRateToNodeEvaluation(Integer errorRate) {
		return null;
	}

	protected double getExpectedUpperBoundForRelativeDistanceToOptimalSolution(Node<T, ?> n, List<T> path) {
		return 0.0;
	}

	public V f(Node<T, ?> n) throws Throwable {
		if (timestampOfFirstEvaluation == 0)
			timestampOfFirstEvaluation = System.currentTimeMillis();
		logger.info("Received request for f-value of node {}", n);

		if (!fValues.containsKey(n)) {

			/* if we already have a value for this path, do not continue */
			if (generator == null)
				throw new IllegalStateException("Cannot compute f-values before the generator is set!");

			/* compute path and partial plan belonging to the node */
			List<T> path = n.externalPath();
			T currentNode = path.get(path.size() - 1);
//			Literal currentTask = currentNode.getRemainingTasks().isEmpty() ? null : currentNode.getRemainingTasks().get(0);
//			List<CEOCAction> partialPlan = CEOCSTNUtil.extractPlanFromSolutionPath(path);
//			List<String> currentProgram = Arrays.asList(MLUtil.getJavaCodeFromPlan(partialPlan).split("\n"));

			/* annotate node with estimated relative distance to optimal solution */
			if (eventBus == null)
				eventBus = new SolutionEventBus<>();
			eventBus.post(new NodeAnnotationEvent<>(n.getPoint(), "EUBRD2OS", getExpectedUpperBoundForRelativeDistanceToOptimalSolution(n, path)));
			
//			List<Long> pathNodeIds = path.stream().map(node -> node.getID()).collect(Collectors.toList());

			if (!n.isGoal()) {
//				logger.info("This is an unknown node; computing score for path to node: {}", pathNodeIds);
//				assert !scoresOfSolutionPaths.containsKey(partialPlan) : "A non-goal path is stored in the list of scores of solution paths!";
				
				V evaluationPriorToCompletion = computeEvaluationPriorToCompletion(n, path);
				if (evaluationPriorToCompletion != null) {
					fValues.put(n, evaluationPriorToCompletion);
					return evaluationPriorToCompletion;
				}
				
				/* if there was no relevant change in comparison to parent, apply parent's f */
				if (path.size() > 1 && !solutionEvaluator.doesLastActionAffectScoreOfAnySubsequentSolution(path)) {
					assert fValues.containsKey(n.getParent()) : "The solution evaluator tells that the solution on the path has not significantly changed, but no f-value has been stored before for the parent. The path is: " + path;
					V score = fValues.get(n.getParent());
					fValues.put(n, score);
//					logger.info("Pipeline has not changed in node {}, adopting value of {} of parent.", n.getPoint().getID(), score);
					return score;
				}
				
//				T lastNode = path.get(path.size() - 1);
//				System.out.println(resolvedProblem.getPropertyName() + ". Resolved by " + (lastNode.getAppliedAction() != null ? lastNode.getAppliedAction().getEncoding() : lastNode.getAppliedMethodInstance().getEncoding()));

				/* check if we have an f-value for exactly this node */
				if (!completions.containsKey(path)) {
					
//					logger.info("No completion is explicitly known for path {}.", pathNodeIds);

					/* determine preprocessor and classifier of pipeline */
//					Optional<String> preprocessorLine = currentProgram.stream().filter(line -> line.contains("new") && line.contains("attributeSelection")).findAny();
//					String preprocessorName = preprocessorLine.isPresent() ? CodePlanningUtil.getPreprocessorEvaluatorFromPipelineGenerationCode(currentProgram) : "";
//					String classifierName = CodePlanningUtil.getClassifierFromPipelineGenerationCode(currentProgram);
//					String plName = preprocessorName + "&" + classifierName;
					
					/* ignore if preprocessing fails even with oneR */
//					String reference = preprocessorName + "&OneR";
//					if (plFails.containsKey(reference)) {
//						logger.info("Cancel {}, because even OneR does not finish within time using this preprocessor!", plName);
//						return null;
//					}
					
					/* if this specific pipeline has failed before, ignore it also now */
//					if (plFails.containsKey(plName)) {
//						logger.info("Ignoreing pipeline which has failed before.");
//						return null;
//					}

					/* if the space under this solution is overly searched, reject */
//					if (maxSolutionsPerTechnique >= 0 && solutionsPerTechnique.containsKey(preprocessorName)
//							&& solutionsPerTechnique.get(preprocessorName).containsKey(classifierName)
//							&& solutionsPerTechnique.get(preprocessorName).get(classifierName) >= maxSolutionsPerTechnique) {
//						logger.warn("Returning null to prevent oversearch");
//						return null;// new IllegalArgumentException("This node is in an oversearched region");
//					}

					/* determine whether we have a solution path (found by the oracle) that goes over this node */
					/* only if we have no path to a solution over this node, we compute a new one */
					if (pathUnifier == null)
						throw new IllegalStateException("Trying to check path unification, but no path unifier has been set. Path: " + path);
					List<T> pathWhoseCompletionSubsumesCurrentPath = pathUnifier.getSubsumingKnownPathCompletion(completions, path);
//					assert pathWhoseCompletionSubsumesCurrentPath == null || pathWhoseCompletionSubsumesCurrentPath.subList(0, path.size()).equals(path) : "The path completion " + pathWhoseCompletionSubsumesCurrentPath.stream().map(node -> node.getID()).collect(Collectors.toList()) + " does NOT subsume path " + pathNodeIds + ".\n\tStep-Wise Comparison (current above, (not) subsuming below): " + ContiguousSet.create(Range.closed(0, Math.max(path.size(), pathWhoseCompletionSubsumesCurrentPath.size())), DiscreteDomain.integers()).asList().stream().map(i -> "\n\t" + i + "\n\t\t" + (i < path.size() ? path.get(i).toString() : "") + "\n\t\t" + (i < pathWhoseCompletionSubsumesCurrentPath.size() ? pathWhoseCompletionSubsumesCurrentPath.get(i).toString() : "")).collect(Collectors.toList());
//					logger.info("Result of a look-up for a path that would subsume {}: {}.", pathNodeIds, pathWhoseCompletionSubsumesCurrentPath != null ? pathWhoseCompletionSubsumesCurrentPath.stream().map(node -> node.getID()).collect(Collectors.toList()) : null);

					boolean interrupted = false;
					if (pathWhoseCompletionSubsumesCurrentPath == null) {
						V best = null;
						List<T> bestCompletion = null;
						int i = 0;
						int j = 0;
						final int maxSamples = samples * 20;
						for (; i < samples; i++) {
							
							if (Thread.interrupted()) {
								interrupted = true;
								break;
							}

							/* create randomized dfs searcher */
							BestFirst<T, String> completer = new RandomizedDepthFirstSearch<>(new GraphGenerator<T, String>() {
								public SingleRootGenerator<T> getRootGenerator() {
									return () -> n.getPoint();
								}

								public SuccessorGenerator<T, String> getSuccessorGenerator() {
									return generator.getSuccessorGenerator();
								}

								public GoalTester<T> getGoalTester() {
									return generator.getGoalTester();
								}

								@Override
								public boolean isSelfContained() {
									// TODO Auto-generated method stub
									return false;
								}

								@Override
								public void setNodeNumbering(boolean nodenumbering) {
									// TODO Auto-generated method stub
									
								}
							}, random);

							/* now complete the current path by the dfs-solution */
//							new SimpleGraphVisualizationWindow<>(completer.getEventBus()).getPanel().setTooltipGenerator(new TFDTooltipGenerator<>());
							List<T> completedPath = new ArrayList<>(n.externalPath());
							logger.info("Starting search for next solution ...");
							List<T> pathCompletion = completer.nextSolution();
							if (pathCompletion == null) {
								logger.warn("No completion was found for path {}. Nodes expanded in search: {}", path, completer.getExpandedCounter());
								return null;
							}
							logger.info("Found solution {}", pathCompletion);
							pathCompletion.remove(0);
							completedPath.addAll(pathCompletion);

							/* now evaluate this solution */
							j++;
							try {
								V val = getFValueOfSolutionPath(completedPath);
								if (val != null) {
									if (best == null || val.compareTo(best) < 0) {
										best = val;
										bestCompletion = completedPath;
									}
								}
							} catch (InterruptedException e) {
								interrupted = true;
								break;
							} catch (Throwable ex) {
								if (j ==maxSamples) {
									logger.warn("Too many retry attempts, giving up.");
									throw ex;
								}
								else {
									LoggerUtil.logException("Could not evaluate solution candidate ... retry another completion.", ex, logger);
									i--;
								}
							}
						}
						
						/* add number of samples to node  */
						n.setAnnotation("fRPSamples", i);
						
						if (bestCompletion == null) {
//							countPLFail(plName);
//							logger.info("Did not find any successful completion for classifier {}. Interrupted: {}", classifierName, interrupted);
							if (interrupted)
								throw new InterruptedException();
							logger.warn("Did not find any completion");
							return null;
						}
						
						/* we have been interrupted, but there are intermediate results. We accept these */
						if (interrupted) {
							logger.info("Estimate {} is only based on {} instead of {} samples, because we received an interrupt.", best, i, samples);
						}
						
//						countPLSuccess(plName);
//						assert isSolutionPath(bestCompletion) : "Identified a completion that is no solution path!";
//						assert scoresOfSolutionPaths.containsKey(CEOCSTNUtil.extractPlanFromSolutionPath(bestCompletion)) : "Solution was detected but its score was not saved";
						completions.put(path, bestCompletion);
					} else {
//						assert isSolutionPath(completions.get(pathWhoseCompletionSubsumesCurrentPath)) : "Identified a subsuming completion "
//								+ pathWhoseCompletionSubsumesCurrentPath.stream().map(l -> l.toString() + "\n").collect(Collectors.toList()) + " that is no solution path!";
						completions.put(path, completions.get(pathWhoseCompletionSubsumesCurrentPath));
					}
				}
				fValues.put(n, getFValueOfSolutionPath(completions.get(path)));
			}

			/* the node is a goal node */
			else {

				/* record that we found a new solution for this technique */
//				String preprocessorName = CodePlanningUtil.getPreprocessorEvaluatorFromPipelineGenerationCode(currentProgram);
//				String classifierName = CodePlanningUtil.getClassifierFromPipelineGenerationCode(currentProgram);
//				if (!solutionsPerTechnique.containsKey(preprocessorName))
//					solutionsPerTechnique.put(preprocessorName, new HashMap<>());
//				if (!solutionsPerTechnique.get(preprocessorName).containsKey(classifierName))
//					solutionsPerTechnique.get(preprocessorName).put(classifierName, 0);
//				int currentlyExploredVariants = solutionsPerTechnique.get(preprocessorName).get(classifierName);
//				solutionsPerTechnique.get(preprocessorName).put(classifierName, currentlyExploredVariants + 1);

				V score = getFValueOfSolutionPath(path);
				if (score == null) {
					logger.warn("No score was computed");
					return null;
				}
				fValues.put(n, score);
				if (!postedSolutions.contains(path)) {
					logger.error("Found a goal node whose solution has not been posted before!");
					// for (List<CEOCAction> plan : knownSolutions.values()) {
					// int counter = 0;
					// for (List<T> path : knownSolutions.keySet()) {
					// if (knownSolutions.get(path).equals(plan))
					// counter ++;
					// }
					// if (counter > 1) {
					// System.err.println("Plan " + plan + " has " + counter + " paths");
					// for (List<T> path : knownSolutions.keySet()) {
					// if (knownSolutions.get(path).equals(plan))
					// System.err.println("\t" + path);
					// }
					// }
//					logger.error("Partial plan is {}", partialPlan);
//					logger.error("Is an unsuccessful plan? {}. F-Value: {}", unsuccessfulPlans.contains(partialPlan), scoresOfSolutionPaths.get(path));
//					System.exit(0);
					// }
				}
			}
		}
		V f = fValues.get(n);
		logger.info("Returning f-value: {}", f);
		return f;
	}

	private V getFValueOfSolutionPath(List<T> path) throws Throwable {
//		assert isSolutionPath(path) : "Can only compute f-values for completed plans, but it is invoked with a plan that does not yield a goal node!";
//		List<CEOCAction> plan = CEOCSTNUtil.extractPlanFromSolutionPath(path);
//		logger.info("Compute f-value for path {} and its plan {}", path.stream().map(n -> n.getID()).collect(Collectors.toList()), plan.stream().map(a -> a.getEncoding()).collect(Collectors.toList()));
//		assert checkPathPlanBijection(path, plan);
		boolean knownPath = scoresOfSolutionPaths.containsKey(path);
		if (!knownPath) {
			if (unsuccessfulPaths.contains(path)) {
				logger.info("Associated path was evaluated unsuccessfully in a previous run; returning NULL: {}", path);
				return null;
			}
			logger.info("Associated plan is new. Compute f-value for complete path {}", path);

			long start = System.currentTimeMillis();
			V val = null;
			try {
				val = solutionEvaluator.evaluateSolution(path);
			} catch (Throwable e) {
				unsuccessfulPaths.add(path);
				throw e;
			}
			
			long duration = System.currentTimeMillis() - start;
			logger.info("Result: {}, Size: {}", val, scoresOfSolutionPaths.size());
			if (val == null) {
				unsuccessfulPaths.add(path);
				return null;
			}

			scoresOfSolutionPaths.put(path, val);
			timesToComputeEvaluations.put(path, (int) duration);
			postSolution(path);
		} else {
			logger.info("Associated plan is known. Reading score from cache.");
			if (logger.isTraceEnabled()) {
				for (List<T> existingPath : scoresOfSolutionPaths.keySet()) {
					if (existingPath.equals(path)) {
						logger.trace("The following plans appear equal:\n\t{}\n\t{}", existingPath, path);
					}
				}
			}
			if (!postedSolutions.contains(path))
				throw new IllegalStateException("Reading cached score of a plan whose path has not been posted as a solution! Are there several paths to a plan?");
		}
		V score = scoresOfSolutionPaths.get(path);
		logger.info("Determined value {} for path {}.", score, path);
		return score;
	}

	

	protected void postSolution(List<T> solution) {
		if (postedSolutions.contains(solution))
			throw new IllegalArgumentException("Solution " + solution.toString() + " already posted!");
		postedSolutions.add(solution);
//		List<CEOCAction> plan = CEOCSTNUtil.extractPlanFromSolutionPath(solution);
		try {
	
			/* now post the solution to the event bus */
			int numberOfComputedFValues = scoresOfSolutionPaths.size();
	
			/* post solution and then the annotations */
			if (eventBus == null)
				eventBus = new SolutionEventBus<>();
			eventBus.post(new SolutionFoundEvent<>(solution, scoresOfSolutionPaths.get(solution)));
			eventBus.post(new SolutionAnnotationEvent<>(solution, "fTime", timesToComputeEvaluations.get(solution)));
			eventBus.post(new SolutionAnnotationEvent<>(solution, "timeToSolution", (int) (System.currentTimeMillis() - timestampOfFirstEvaluation)));
			eventBus.post(new SolutionAnnotationEvent<>(solution, "nodesExpandedToSolution", numberOfComputedFValues));
		}
		catch (Throwable e) {
			List<Pair<String, Object>> explanations = new ArrayList<>();
			if (logger.isDebugEnabled()) {
				StringBuilder sb = new StringBuilder();
				solution.forEach(n -> sb.append(n.toString() + "\n"));
				explanations.add(new Pair<>("The path that has been tried to convert is as follows:", sb.toString()));
			}
			LoggerUtil.logException("Cannot post solution, because no valid MLPipeline object could be derived from it.", e, logger, explanations);
		}
	}
	
	@Override
	public void setGenerator(GraphGenerator<T, String> generator) {
		this.generator = (SerializableGraphGenerator<T, String>)generator;
	}

	@Override
	public SolutionEventBus<T> getSolutionEventBus() {
		if (this.eventBus == null) {
			this.eventBus = new SolutionEventBus<>();
		}
		return this.eventBus;
	}

	@Override
	public void cancel() {
		logger.info("Receive cancel signal.");
	}
}
