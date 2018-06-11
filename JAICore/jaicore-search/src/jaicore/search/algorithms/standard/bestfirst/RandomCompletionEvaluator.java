package jaicore.search.algorithms.standard.bestfirst;

import jaicore.basic.sets.SetUtil.Pair;
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

@SuppressWarnings("serial")
public class RandomCompletionEvaluator<T, V extends Comparable<V>>
    implements IGraphDependentNodeEvaluator<T, String, V>, SerializableNodeEvaluator<T, V>, ISolutionReportingNodeEvaluator<T, V>, ICancelableNodeEvaluator {

  private final static Logger logger = LoggerFactory.getLogger(RandomCompletionEvaluator.class);
  protected Map<List<T>, List<T>> completions = new ConcurrentHashMap<>();
  protected Set<List<T>> unsuccessfulPaths = Collections.synchronizedSet(new HashSet<>());
  protected Set<List<T>> postedSolutions = new HashSet<>();
  protected Map<List<T>, Integer> timesToComputeEvaluations = new HashMap<>();

  protected Map<List<T>, V> scoresOfSolutionPaths = new ConcurrentHashMap<>();
  protected Map<Node<T, ?>, V> fValues = new ConcurrentHashMap<>();
  protected Map<String, Integer> ppFails = new ConcurrentHashMap<>();
  protected Map<String, Integer> plFails = new ConcurrentHashMap<>();
  protected Map<String, Integer> plSuccesses = new ConcurrentHashMap<>();

  protected final IPathUnification<T> pathUnifier;
  protected SerializableGraphGenerator<T, String> generator;
  protected long timestampOfFirstEvaluation;
  protected final Random random;
  protected int samples;
  protected final ISolutionEvaluator<T, V> solutionEvaluator;
  protected transient SolutionEventBus<T> eventBus = new SolutionEventBus<>();

  public RandomCompletionEvaluator(final Random random, final int samples, final IPathUnification<T> pathUnifier, final ISolutionEvaluator<T, V> solutionEvaluator) {
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

  protected V computeEvaluationPriorToCompletion(final Node<T, ?> n, final List<T> path) throws Throwable {
    return null;
  }

  protected V convertErrorRateToNodeEvaluation(final Integer errorRate) {
    return null;
  }

  protected double getExpectedUpperBoundForRelativeDistanceToOptimalSolution(final Node<T, ?> n, final List<T> path) {
    return 0.0;
  }

  @Override
  public V f(final Node<T, ?> n) throws Throwable {
    if (this.timestampOfFirstEvaluation == 0) {
      this.timestampOfFirstEvaluation = System.currentTimeMillis();
    }
    logger.info("Received request for f-value of node {}", n);

    if (!this.fValues.containsKey(n)) {

      /* if we already have a value for this path, do not continue */
      if (this.generator == null) {
        throw new IllegalStateException("Cannot compute f-values before the generator is set!");
      }

      /* compute path and partial plan belonging to the node */
      List<T> path = n.externalPath();

      /* annotate node with estimated relative distance to optimal solution */
      if (this.eventBus == null) {
        this.eventBus = new SolutionEventBus<>();
      }
      this.eventBus.post(new NodeAnnotationEvent<>(n.getPoint(), "EUBRD2OS", this.getExpectedUpperBoundForRelativeDistanceToOptimalSolution(n, path)));
      if (!n.isGoal()) {
        V evaluationPriorToCompletion = this.computeEvaluationPriorToCompletion(n, path);
        if (evaluationPriorToCompletion != null) {
          this.fValues.put(n, evaluationPriorToCompletion);
          return evaluationPriorToCompletion;
        }

        /* if there was no relevant change in comparison to parent, apply parent's f */
        if (path.size() > 1 && !this.solutionEvaluator.doesLastActionAffectScoreOfAnySubsequentSolution(path)) {
          assert this.fValues.containsKey(n
              .getParent()) : "The solution evaluator tells that the solution on the path has not significantly changed, but no f-value has been stored before for the parent. The path is: "
                  + path;
          V score = this.fValues.get(n.getParent());
          this.fValues.put(n, score);
          return score;
        }
        
        /* check if we have an f-value for exactly this node */
        if (!this.completions.containsKey(path)) {

          /* determine whether we have a solution path (found by the oracle) that goes over this node */
          /* only if we have no path to a solution over this node, we compute a new one */
          if (this.pathUnifier == null) {
            throw new IllegalStateException("Trying to check path unification, but no path unifier has been set. Path: " + path);
          }
          List<T> pathWhoseCompletionSubsumesCurrentPath = this.pathUnifier.getSubsumingKnownPathCompletion(this.completions, path);
          boolean interrupted = false;
          if (pathWhoseCompletionSubsumesCurrentPath == null) {
            V best = null;
            List<T> bestCompletion = null;
            int i = 0;
            int j = 0;
            final int maxSamples = this.samples * 20;

            for (; i < this.samples; i++) {
              if (Thread.currentThread().isInterrupted()) {
                interrupted = true;
                break;
              }

              /* create randomized dfs searcher */
              BestFirst<T, String> completer = new RandomizedDepthFirstSearch<>(new GraphGenerator<T, String>() {
                @Override
                public SingleRootGenerator<T> getRootGenerator() {
                  return () -> n.getPoint();
                }

                @Override
                public SuccessorGenerator<T, String> getSuccessorGenerator() {
                  return RandomCompletionEvaluator.this.generator.getSuccessorGenerator();
                }

                @Override
                public GoalTester<T> getGoalTester() {
                  return RandomCompletionEvaluator.this.generator.getGoalTester();
                }

                @Override
                public boolean isSelfContained() {
                  return false;
                }

                @Override
                public void setNodeNumbering(final boolean nodenumbering) {
                	throw new UnsupportedOperationException();
                }
              }, this.random);

              /* now complete the current path by the dfs-solution */
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
                V val = this.getFValueOfSolutionPath(completedPath);
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
                if (j == maxSamples) {
                  logger.warn("Too many retry attempts, giving up.");
                  throw ex;
                } else {
                  logger.error("Could not evaluate solution candidate ... retry another completion. {}", LoggerUtil.getExceptionInfo(ex));
                  i--;
                }
              }
            }

            /* add number of samples to node */
            n.setAnnotation("fRPSamples", i);

            if (bestCompletion == null) {
              // countPLFail(plName);
              // logger.info("Did not find any successful completion for classifier {}. Interrupted: {}",
              // classifierName, interrupted);
              if (interrupted) {
                throw new InterruptedException();
              }
              logger.warn("Did not find any completion");
              return null;
            }

            /* we have been interrupted, but there are intermediate results. We accept these */
            if (interrupted) {
              logger.info("Estimate {} is only based on {} instead of {} samples, because we received an interrupt.", best, i, this.samples);
            }

            // countPLSuccess(plName);
            // assert isSolutionPath(bestCompletion) : "Identified a completion that is no solution path!";
            // assert scoresOfSolutionPaths.containsKey(CEOCSTNUtil.extractPlanFromSolutionPath(bestCompletion))
            // : "Solution was detected but its score was not saved";
            this.completions.put(path, bestCompletion);
          } else {
            // assert isSolutionPath(completions.get(pathWhoseCompletionSubsumesCurrentPath)) : "Identified a
            // subsuming completion "
            // + pathWhoseCompletionSubsumesCurrentPath.stream().map(l -> l.toString() +
            // "\n").collect(Collectors.toList()) + " that is no solution path!";
            this.completions.put(path, this.completions.get(pathWhoseCompletionSubsumesCurrentPath));
          }
        }
        this.fValues.put(n, this.getFValueOfSolutionPath(this.completions.get(path)));
      }

      /* the node is a goal node */
      else {

        /* record that we found a new solution for this technique */
        // String preprocessorName =
        // CodePlanningUtil.getPreprocessorEvaluatorFromPipelineGenerationCode(currentProgram);
        // String classifierName = CodePlanningUtil.getClassifierFromPipelineGenerationCode(currentProgram);
        // if (!solutionsPerTechnique.containsKey(preprocessorName))
        // solutionsPerTechnique.put(preprocessorName, new HashMap<>());
        // if (!solutionsPerTechnique.get(preprocessorName).containsKey(classifierName))
        // solutionsPerTechnique.get(preprocessorName).put(classifierName, 0);
        // int currentlyExploredVariants = solutionsPerTechnique.get(preprocessorName).get(classifierName);
        // solutionsPerTechnique.get(preprocessorName).put(classifierName, currentlyExploredVariants + 1);

        V score = this.getFValueOfSolutionPath(path);
        if (score == null) {
          logger.warn("No score was computed");
          return null;
        }
        this.fValues.put(n, score);
        if (!this.postedSolutions.contains(path)) {
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
          // logger.error("Partial plan is {}", partialPlan);
          // logger.error("Is an unsuccessful plan? {}. F-Value: {}", unsuccessfulPlans.contains(partialPlan),
          // scoresOfSolutionPaths.get(path));
          // System.exit(0);
          // }
        }
      }
    }
    V f = this.fValues.get(n);
    logger.info("Returning f-value: {}", f);
    return f;
  }

  protected V getFValueOfSolutionPath(final List<T> path) throws Throwable {
    // assert isSolutionPath(path) : "Can only compute f-values for completed plans, but it is invoked
    // with a plan that does not yield a goal node!";
    // List<CEOCAction> plan = CEOCSTNUtil.extractPlanFromSolutionPath(path);
    // logger.info("Compute f-value for path {} and its plan {}", path.stream().map(n ->
    // n.getID()).collect(Collectors.toList()), plan.stream().map(a ->
    // a.getEncoding()).collect(Collectors.toList()));
    // assert checkPathPlanBijection(path, plan);
    boolean knownPath = this.scoresOfSolutionPaths.containsKey(path);
    if (!knownPath) {
      if (this.unsuccessfulPaths.contains(path)) {
        logger.info("Associated path was evaluated unsuccessfully in a previous run; returning NULL: {}", path);
        return null;
      }
      logger.info("Associated plan is new. Compute f-value for complete path {}", path);

      long start = System.currentTimeMillis();
      V val = null;
      try {
        val = this.solutionEvaluator.evaluateSolution(path);
      } catch (Throwable e) {
        this.unsuccessfulPaths.add(path);
        throw e;
      }

      long duration = System.currentTimeMillis() - start;
      logger.info("Result: {}, Size: {}", val, this.scoresOfSolutionPaths.size());
      if (val == null) {
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
      this.eventBus.post(new SolutionFoundEvent<>(solution, this.scoresOfSolutionPaths.get(solution)));
      this.eventBus.post(new SolutionAnnotationEvent<>(solution, "fTime", this.timesToComputeEvaluations.get(solution)));
      this.eventBus.post(new SolutionAnnotationEvent<>(solution, "timeToSolution", (int) (System.currentTimeMillis() - this.timestampOfFirstEvaluation)));
      this.eventBus.post(new SolutionAnnotationEvent<>(solution, "nodesExpandedToSolution", numberOfComputedFValues));
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
}
