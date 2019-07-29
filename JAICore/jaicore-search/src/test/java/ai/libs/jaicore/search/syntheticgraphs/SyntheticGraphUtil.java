package ai.libs.jaicore.search.syntheticgraphs;

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;

import ai.libs.jaicore.search.algorithms.standard.dfs.DepthFirstSearch;
import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.syntheticgraphs.BalancedGraphGeneratorGenerator.N;

public class SyntheticGraphUtil {

	public static List<Double> getScores(final int branchingFactor, final int depth, final TreasureIslandPathCostGenerator treasureGenerator) {
		BalancedGraphSearchWithPathEvaluationsProblem prob = new BalancedGraphSearchWithPathEvaluationsProblem(branchingFactor, depth, treasureGenerator);
		DepthFirstSearch<N, Integer> dfs = new DepthFirstSearch<>(prob);
		List<Double> scores = new LinkedList<>();
		while (dfs.hasNext()) {
			try {
				SearchGraphPath<N, Integer> path = dfs.nextSolutionCandidate();
				scores.add(prob.getPathEvaluator().evaluate(path));
			} catch (AlgorithmTimeoutedException | InterruptedException | AlgorithmExecutionCanceledException | AlgorithmException | NoSuchElementException | PathEvaluationException e) {
				e.printStackTrace();
			}
		}
		return scores;
	}
}
