package ai.libs.jaicore.search.algorithms.standard.nmcs;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;
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
import ai.libs.jaicore.search.core.interfaces.AOptimalPathInORGraphSearch;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.model.other.SearchGraphPath;

/**
 *
 * @author Felix Mohr
 *
 * This class implements the Nested Monte Carlo Search algorithm presented in
 *
 * @inproceedings{nmcs,
 *    title={Nested monte-carlo search},
 *    author={Cazenave, Tristan},
 *    booktitle={Twenty-First International Joint Conference on Artificial Intelligence},
 *    year={2009}
 *  }
 *
 *
 * @param <I> Exact Problem Type
 * @param <N> Node Type
 * @param <A> Arc Type
 * @param <V> Type for Path Scores
 */
public class NMCS<I extends IPathSearchWithPathEvaluationsInput<N, A, V>, N, A, V extends Comparable<V>> extends AOptimalPathInORGraphSearch<I, N, A, V> {

	private Logger logger = LoggerFactory.getLogger(NMCS.class);
	private final int level;
	private final ISuccessorGenerator<N, A> succ;
	private final boolean maximization;
	//	private final RandomSearch<N, A> rs;
	private final Random random;
	private final ILabeledPath<N, A> root;

	private final Queue<EvaluatedSearchSolutionCandidateFoundEvent<N, A, V>> solutionEventQueue = new LinkedList<>();

	public NMCS(final I problem, final Random random, final int level, final boolean maximization) {
		super(problem);
		this.succ = this.getInput().getGraphGenerator().getSuccessorGenerator();
		//		this.rs = new RandomSearch<>(problem, random);
		this.random = random;
		this.level = level;
		this.maximization = maximization;
		this.root = new SearchGraphPath<>(problem.getGraphGenerator().getRootGenerator().getRoots().iterator().next());
	}

	@Override
	public IAlgorithmEvent nextWithException() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {
		switch (this.getState()) {
		case CREATED:
			this.logger.info("Algorithm activated.");
			return this.activate();
		case ACTIVE:
			this.checkAndConductTermination();
			try {
				this.logger.debug("Next main iteration.");
				if (this.solutionEventQueue.isEmpty()) {
					this.nested(this.root, this.level);
				}
				return this.solutionEventQueue.poll();
			} catch (PathEvaluationException e) {
				throw new AlgorithmException("An error occurred.", e);
			}
		default:
			throw new IllegalStateException();
		}
	}

	public boolean isBetter(final IEvaluatedPath<N, A, V> currentPath, final IEvaluatedPath<N, A, V> newPath) {
		if (currentPath == null) {
			return true;
		}
		int compScore = newPath.getScore().compareTo(currentPath.getScore());

		return this.maximization ? compScore > 0 : compScore < 0;
	}

	public IEvaluatedPath<N, A, V> nested(final ILabeledPath<N, A> pathToNode, final int localLevel) throws PathEvaluationException, AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException {
		this.checkAndConductTermination();
		IEvaluatedPath<N, A, V> bestPathReachableFromHere = null;
		for (INewNodeDescription<N, A> s : this.succ.generateSuccessors(pathToNode.getHead())) {
			ILabeledPath<N, A> pathToChild = new SearchGraphPath<>(pathToNode, s.getTo(), s.getArcLabel());
			this.checkAndConductTermination();
			IEvaluatedPath<N, A, V> bestPathOfThisMove;
			if (localLevel == 1) {
				bestPathOfThisMove = this.sample(pathToChild);
			} else {
				bestPathOfThisMove = this.nested(pathToChild, localLevel - 1);
			}
			if (this.isBetter(bestPathReachableFromHere, bestPathOfThisMove)) {
				bestPathReachableFromHere = bestPathOfThisMove;
			}
		}
		return bestPathReachableFromHere;
	}

	public IEvaluatedPath<N, A, V> sample(final ILabeledPath<N, A> pathToNode) throws InterruptedException, PathEvaluationException, AlgorithmTimeoutedException, AlgorithmExecutionCanceledException {
		this.checkAndConductTermination();
		ILabeledPath<N, A> solutionPath = new SearchGraphPath<>(pathToNode);
		while (!this.getGoalTester().isGoal(solutionPath)) {
			INewNodeDescription<N, A> randomExpansion = SetUtil.getRandomElement(this.succ.generateSuccessors(solutionPath.getHead()), this.random);
			solutionPath.extend(randomExpansion.getTo(), randomExpansion.getArcLabel());
		}
		this.checkAndConductTermination();
		V score = this.getInput().getPathEvaluator().evaluate(solutionPath);
		this.logger.debug("Found a leaf with score {}", score);
		EvaluatedSearchGraphPath<N, A, V> evaluatedSolutionPath = new EvaluatedSearchGraphPath<>(solutionPath, score);
		EvaluatedSearchSolutionCandidateFoundEvent<N, A, V> solutionEvent = new EvaluatedSearchSolutionCandidateFoundEvent<>(this, evaluatedSolutionPath);
		this.post(solutionEvent);
		if (this.updateBestSeenSolution(evaluatedSolutionPath)) {
			this.logger.info("New best score is {}", this.getBestSeenSolution().getScore());
		}
		this.solutionEventQueue.add(solutionEvent);
		return evaluatedSolutionPath;
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	public int getLevel() {
		return this.level;
	}
}
