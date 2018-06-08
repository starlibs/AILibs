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
public class UncertaintyRandomCompletionEvaluator<T, N extends Comparable<N>, V extends Comparable<V>> extends RandomCompletionEvaluator<T, V> {

	private static final Logger logger = LoggerFactory.getLogger(UncertaintyRandomCompletionEvaluator.class);
	
	private IUncertaintySource<T, V> uncertaintyCalculation;

	public UncertaintyRandomCompletionEvaluator(Random random, int samples, IPathUnification<T> pathUnifier, ISolutionEvaluator<T, V> solutionEvaluator, IUncertaintySource<T, V> uncertaintySource) {
		super(random, samples,pathUnifier, solutionEvaluator);
		this.uncertaintyCalculation = uncertaintySource;
	}

	@Override
	public V f(Node<T, ?> n) throws Throwable {
		if (timestampOfFirstEvaluation == 0)
			timestampOfFirstEvaluation = System.currentTimeMillis();
		logger.info("Received request for f-value of node {}", n);

		if (!fValues.containsKey(n)) {
			double uncertainty = 1.0d;

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
						V best = null;
						List<T> bestCompletion = null;
						int i = 0;
						int j = 0;
						final int maxSamples = samples * 20;
						List<V> evaluations = new ArrayList<>();
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
								V val = getFValueOfSolutionPath(completedPath);
								uncertainty = this.uncertaintyCalculation.calculateUncertainty(n, completedPath, evaluations);
								if (val != null) {
									evaluations.add(val);
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
				fValues.put(n, getFValueOfSolutionPath(this.completions.get(path)));
			}

			/* the node is a goal node */
			else {
				V score = getFValueOfSolutionPath(path);
				if (score == null) {
					logger.warn("No score was computed");
					return null;
				}
				fValues.put(n, score);
				if (!postedSolutions.contains(path)) {
					logger.error("Found a goal node whose solution has not been posted before!");
				}
				uncertainty = 0.0d;
			}

			n.setAnnotation("uncertainty", uncertainty);
		}
		V f = fValues.get(n);
		logger.info("Returning f-value: {}", f);
		return f;
	}

}
