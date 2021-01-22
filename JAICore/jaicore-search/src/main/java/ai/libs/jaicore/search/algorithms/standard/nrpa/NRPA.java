package ai.libs.jaicore.search.algorithms.standard.nrpa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;

import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;
import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.IPathGoalTester;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IEvaluatedPath;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.datastructure.graph.ILabeledPath;
import org.api4.java.datastructure.graph.implicit.INewNodeDescription;
import org.api4.java.datastructure.graph.implicit.ISuccessorGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.events.EvaluatedSearchSolutionCandidateFoundEvent;
import ai.libs.jaicore.search.algorithms.standard.nmcs.NMCS;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.model.other.SearchGraphPath;

/**
 *
 * @author Felix Mohr
 *
 * This class implements the Nested rollout policy adaption algorithm for Monte Carlo Search presented in
 *
 * @inproceedings{rosin2011nested,
 *   title={Nested rollout policy adaptation for Monte Carlo tree search},
 *   author={Rosin, Christopher D},
 *   booktitle={Ijcai},
 *   pages={649--654},
 *   year={2011}
 * }
 *
 *
 * @param <I> Exact Problem Type
 * @param <N> Node Type
 * @param <A> Arc Type
 * @param <V> Type for Path Scores
 */
public class NRPA<I extends IPathSearchWithPathEvaluationsInput<N, A, Double>, N, A> extends NMCS<I, N, A, Double> {

	private Logger logger = LoggerFactory.getLogger(NRPA.class);
	private final ISuccessorGenerator<N, A> succ;
	private final boolean maximization;
	private final ILabeledPath<N, A> root;
	private final IPathGoalTester<N, A> goalTester;

	private final Random random;
	private final double alpha;
	private final int iterationsPerLevel;
	private final Queue<EvaluatedSearchSolutionCandidateFoundEvent<N, A, Double>> solutionEventQueue = new LinkedList<>();

	public NRPA(final I problem, final Random random, final int level, final double alpha, final int iterationsPerLevel, final boolean maximization) {
		super(problem, random, level, maximization);
		this.succ = this.getInput().getGraphGenerator().getSuccessorGenerator();
		this.goalTester = this.getInput().getGoalTester();
		this.maximization = true;
		this.root = new SearchGraphPath<>(problem.getGraphGenerator().getRootGenerator().getRoots().iterator().next());
		this.alpha = alpha;
		this.iterationsPerLevel = iterationsPerLevel;
		this.random = random;
	}

	@Override
	public IAlgorithmEvent nextWithException() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {
		switch (this.getState()) {
		case CREATED:
			super.nextWithException();
			this.logger.info("Algorithm activated.");
			return this.activate();
		case ACTIVE:
			this.checkAndConductTermination();
			try {
				this.logger.debug("Next main iteration.");
				if (this.solutionEventQueue.isEmpty()) {
					this.nrpa(this.getLevel(), new HashMap<>());
				}
				return this.solutionEventQueue.poll();
			} catch (PathEvaluationException e) {
				throw new AlgorithmException("An error occurred.", e);
			}
		default:
			throw new IllegalStateException();
		}
	}

	public Map<A, Double> adaptActionScores(final IEvaluatedPath<N, A, Double> path, final Map<A, Double> pol) throws InterruptedException {

		List<N> nodes = path.getNodes();
		List<A> arcs = path.getArcs();
		int n = arcs.size();

		Map<A, Double> newPol = new HashMap<>(pol);
		double modifier = this.maximization ? this.alpha : (-1 * this.alpha);

		for (int i = 0; i < n; i++) {
			N node = nodes.get(i);
			A arc = arcs.get(i);

			List<INewNodeDescription<N, A>> children = this.succ.generateSuccessors(node);

			/* update the action score of the chosen action */
			double currentActionScore = 0;
			if (newPol.containsKey(arc)) {
				currentActionScore = newPol.get(arc);
			}
			currentActionScore += modifier;
			newPol.put(arc, currentActionScore);

			/* compute new scores for all children under this node */
			double z = 0;
			double z2 = 0;
			List<Double> curScores = new ArrayList<>();
			for (INewNodeDescription<N, A> succDesc : children) {
				double curScore = newPol.computeIfAbsent(succDesc.getArcLabel(), a -> 0.0);
				z += Math.exp(curScore);
				z2 += curScore;
				curScores.add(curScore);
			}

			/* if the sum gets too large, rescale */
			//			if (z2 > 100) {
			//				System.err.println("Clearing");
			//				for (double s : curScores) {
			//					System.out.println("\t" + (Math.exp(s) / z));
			//				}
			//				z = 0;
			//				curScores.clear();
			//				for (INewNodeDescription<N, A> succDesc : children) {
			//					double curScore = this.actionScores.get(succDesc.getArcLabel()) - 5;
			//					this.actionScores.put(succDesc.getArcLabel(), currentActionScore);
			//					z += Math.exp(curScore);
			//					curScores.add(curScore);
			//				}
			//				System.out.println("Re-calculated");
			//				for (double s : curScores) {
			//					System.out.println("\t" + (Math.exp(s) / z));
			//				}
			//			}

			if (z == 0) {
				throw new IllegalStateException("Sum must not be 0");
			}

			if (!Double.isInfinite(z)) {
				int m = curScores.size();
				for (int j = 0; j < m; j++) {
					A curAction = children.get(j).getArcLabel();
					if (!curAction.equals(arc)) {
						double curScore = curScores.get(j);
						double newScore = curScore - modifier * Math.exp(curScore) / z;
						newPol.put(curAction, newScore);
					}
				}
			}
			else {
				this.logger.trace("Ignoring update for numerical reasons.");
			}
		}
		return newPol;
	}

	public IEvaluatedPath<N, A, Double> nrpa(final int localLevel, final Map<A, Double> pol) throws PathEvaluationException, AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException {
		this.checkAndConductTermination();
		if (localLevel == 0) {
			return this.sample(this.root, pol);
		}
		IEvaluatedPath<N, A, Double> bestSeq = null;
		Map<A, Double> localPol = new HashMap<>(pol);
		for (int i = 0; i < this.iterationsPerLevel; i++) {
			IEvaluatedPath<N, A, Double> bestPathReachableFromHere = this.nrpa(localLevel - 1, localPol); // our policy should not be modified
			if (this.isBetter(bestSeq, bestPathReachableFromHere)) {
				bestSeq = bestPathReachableFromHere;
			}
			localPol = this.adaptActionScores(bestPathReachableFromHere, localPol);
		}
		return bestSeq;
	}

	/**
	 * This method is different than the sample method in NMCS since it weights the candidates according to the action scores (variable "pol" in the paper)
	 */
	public IEvaluatedPath<N, A, Double> sample(final ILabeledPath<N, A> pathToNode,  final Map<A, Double> pol) throws InterruptedException, PathEvaluationException, AlgorithmTimeoutedException, AlgorithmExecutionCanceledException {

		/* if this is a leaf, return the result */
		if (this.goalTester.isGoal(pathToNode)) {
			Double score = this.getInput().getPathEvaluator().evaluate(pathToNode);
			this.logger.debug("Found a leaf with score {}", score);
			EvaluatedSearchGraphPath<N, A, Double> solutionPath = new EvaluatedSearchGraphPath<>(pathToNode, score);
			EvaluatedSearchSolutionCandidateFoundEvent<N, A, Double> solutionEvent = new EvaluatedSearchSolutionCandidateFoundEvent<>(this, solutionPath);
			this.logger.debug("Obtained solution path with score {}, now updating.", solutionPath.getScore());
			this.post(solutionEvent);
			if (this.updateBestSeenSolution(solutionPath)) {
				this.logger.info("New best score is {}", this.getBestSeenSolution().getScore());
			}
			return solutionPath;
		}

		/* compute a probability vector over the child nodes */
		List<Double> probVector = new ArrayList<>();
		List<INewNodeDescription<N, A>> childNodes = this.succ.generateSuccessors(pathToNode.getHead());
		if (childNodes.isEmpty()) {
			return new EvaluatedSearchGraphPath<>(pathToNode, this.maximization ? (-1  * Double.MAX_VALUE) : Double.MAX_VALUE);
		}
		double sum = 0;
		double maxScore = 0;
		INewNodeDescription<N, A> maxScoreChoice = null;
		for (INewNodeDescription<N, A> successorDescription : childNodes) {
			double curScore = pol.computeIfAbsent(successorDescription.getArcLabel(), a -> 0.0);
			double nodeScore = Math.exp(curScore);
			if (curScore > maxScore) {
				maxScore = curScore;
				maxScoreChoice = successorDescription;
			}
			probVector.add(nodeScore);
			sum += nodeScore;
		}
		if (sum == 0) {
			throw new IllegalStateException("Sum of node scores is 0");
		}
		int n = probVector.size();
		for (int i = 0; i < n; i++) {
			probVector.set(i, probVector.get(i) / sum);
		}

		/* now choose a random successor according to the probability vector and recurse */
		INewNodeDescription<N, A> choice;
		if (maxScore > 500) {
			choice = maxScoreChoice; // deterministically choose this one, because the probs cannot be updated properly anymore
		}
		else {
			choice = SetUtil.getRandomElement(childNodes, this.random, probVector);
		}
		ILabeledPath<N, A> extendedPath = new SearchGraphPath<>(pathToNode, choice.getTo(), choice.getArcLabel());
		return this.sample(extendedPath, pol);
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}
}
