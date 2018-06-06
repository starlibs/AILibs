package jaicore.search.algorithms.standard.uncertainty;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.search.algorithms.interfaces.IPathUnification;
import jaicore.search.algorithms.interfaces.ISolutionEvaluator;
import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.algorithms.standard.bestfirst.RandomCompletionEvaluator;
import jaicore.search.algorithms.standard.core.NodeAnnotationEvent;
import jaicore.search.algorithms.standard.core.SolutionEventBus;
import jaicore.search.algorithms.standard.rdfs.RandomizedDepthFirstSearch;
import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.Node;
import jaicore.search.structure.graphgenerator.GoalTester;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

@SuppressWarnings("serial")
public class UncertaintyRandomCompletionEvaluator<T> extends RandomCompletionEvaluator<T, UncertaintyFMeasure> {

	private static final Logger logger = LoggerFactory.getLogger(UncertaintyRandomCompletionEvaluator.class);
	
	private IUncertaintySource<T> uncertaintyCalculation;

	public UncertaintyRandomCompletionEvaluator(Random random, int samples, IPathUnification<T> pathUnifier, ISolutionEvaluator<T, UncertaintyFMeasure> solutionEvaluator, IUncertaintySource<T> uncertaintySource) {
		super(random, samples,pathUnifier, solutionEvaluator);
		this.uncertaintyCalculation = uncertaintySource;
	}

	@Override
	public UncertaintyFMeasure f(Node<T, ?> n) throws Throwable {
		if (timestampOfFirstEvaluation == 0)
			timestampOfFirstEvaluation = System.currentTimeMillis();
		logger.info("Received request for f-value of node {}", n);

		if (!fValues.containsKey(n)) {

			/* if we already have a value for this path, do not continue */
			if (generator == null)
				throw new IllegalStateException("Cannot compute f-values before the generator is set!");

			/* compute path and partial plan belonging to the node */
			List<T> path = n.externalPath();

			/* annotate node with estimated relative distance to optimal solution */
			if (eventBus == null)
				eventBus = new SolutionEventBus<>();
			eventBus.post(new NodeAnnotationEvent<>(n.getPoint(), "EUBRD2OS", getExpectedUpperBoundForRelativeDistanceToOptimalSolution(n, path)));

			if (!n.isGoal()) {
				
				UncertaintyFMeasure evaluationPriorToCompletion = computeEvaluationPriorToCompletion(n, path);
				if (evaluationPriorToCompletion != null) {
					fValues.put(n, evaluationPriorToCompletion);
					return evaluationPriorToCompletion;
				}
				
				/* if there was no relevant change in comparison to parent, apply parent's f */
				if (path.size() > 1 && !solutionEvaluator.doesLastActionAffectScoreOfAnySubsequentSolution(path)) {
					assert fValues.containsKey(n.getParent()) : "The solution evaluator tells that the solution on the path has not significantly changed, but no f-value has been stored before for the parent. The path is: " + path;
					UncertaintyFMeasure score = fValues.get(n.getParent());
					fValues.put(n, score);
					return score;
				}

				/* check if we have an f-value for exactly this node */
				if (!completions.containsKey(path)) {

					/* determine whether we have a solution path (found by the oracle) that goes over this node */
					/* only if we have no path to a solution over this node, we compute a new one */
					if (pathUnifier == null)
						throw new IllegalStateException("Trying to check path unification, but no path unifier has been set. Path: " + path);
					List<T> pathWhoseCompletionSubsumesCurrentPath = pathUnifier.getSubsumingKnownPathCompletion(completions, path);

					boolean interrupted = false;
					if (pathWhoseCompletionSubsumesCurrentPath == null) {
						UncertaintyFMeasure best = null;
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
									return n::getPoint;
								}

								public SuccessorGenerator<T, String> getSuccessorGenerator() {
									return generator.getSuccessorGenerator();
								}

								public GoalTester<T> getGoalTester() {
									return generator.getGoalTester();
								}

								@Override
								public boolean isSelfContained() {
									return false;
								}

								@Override
								public void setNodeNumbering(boolean nodenumbering) {
								}
							}, random);

							/* now complete the current path by the dfs-solution */
							List<T> completedPath = new ArrayList<>(n.externalPath());
							logger.info("Starting search for next solution ...");
							List<T> pathCompletion = completer.nextSolution();
							if (pathCompletion == null) {
								return null;
							}
							logger.info("Found solution {}", pathCompletion);
							pathCompletion.remove(0);
							completedPath.addAll(pathCompletion);

							/* now evaluate this solution */
							j++;
							try {
								UncertaintyFMeasure val = getUncertainFValueOfSolutionPath(n, completedPath, false);
								if (val != null && (best == null || val.compareTo(best) < 0)) {
									best = val;
									bestCompletion = completedPath;
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
									logger.error(ex.getMessage());
									i--;
								}
							}
						}
						
						/* add number of samples to node  */
						n.setAnnotation("fRPSamples", i);
						
						if (bestCompletion == null) {
							if (interrupted)
								throw new InterruptedException();
							logger.warn("Did not find any completion");
							return null;
						}
						
						/* we have been interrupted, but there are intermediate results. We accept these */
						if (interrupted) {
							logger.info("Estimate {} is only based on {} instead of {} samples, because we received an interrupt.", best, i, samples);
						}
						completions.put(path, bestCompletion);
					} else {
						completions.put(path, completions.get(pathWhoseCompletionSubsumesCurrentPath));
					}
				}
				fValues.put(n, getUncertainFValueOfSolutionPath(n, completions.get(path), false));
			}

			/* the node is a goal node */
			else {
				UncertaintyFMeasure score = getUncertainFValueOfSolutionPath(n, path, true);
				if (score == null) {
					logger.warn("No score was computed");
					return null;
				}
				fValues.put(n, score);
				if (!postedSolutions.contains(path)) {
					logger.error("Found a goal node whose solution has not been posted before!");
				}
			}
		}
		UncertaintyFMeasure f = fValues.get(n);
		logger.info("Returning f-value: {}", f);
		return f;
	}

	@SuppressWarnings("unchecked")
	private UncertaintyFMeasure getUncertainFValueOfSolutionPath(Node<T, ?> n, List<T> path, boolean readchedGoal) throws Exception {
		boolean knownPath = scoresOfSolutionPaths.containsKey(path);
		if (!knownPath) {
			if (unsuccessfulPaths.contains(path)) {
				logger.info("Associated path was evaluated unsuccessfully in a previous run; returning NULL: {}", path);
				return null;
			}
			logger.info("Associated plan is new. Compute f-value for complete path {}", path);

			long start = System.currentTimeMillis();
			UncertaintyFMeasure val = null;
			try {
				UncertaintyFMeasure score = solutionEvaluator.evaluateSolution(path);
				if (readchedGoal) {
					val = new UncertaintyFMeasure(score.getfValue(), 0.0d);
				} else {
					val = new UncertaintyFMeasure(score.getfValue(), this.uncertaintyCalculation.calculateUncertainty((Node<T, UncertaintyFMeasure>) n, path));
				}
			} catch (Exception e) {
				unsuccessfulPaths.add(path);
				throw e;
			}
			
			long duration = System.currentTimeMillis() - start;
			logger.info("Result: {}, Size: {}", val, scoresOfSolutionPaths.size());

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
		UncertaintyFMeasure score = scoresOfSolutionPaths.get(path);
		logger.info("Determined value {} for path {}.", score, path);
		return score;
	}

}
