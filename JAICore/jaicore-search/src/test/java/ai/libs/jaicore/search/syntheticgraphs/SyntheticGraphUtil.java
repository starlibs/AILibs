package ai.libs.jaicore.search.syntheticgraphs;

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;

import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.search.algorithms.standard.dfs.DepthFirstSearch;
import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.syntheticgraphs.graphmodels.ITransparentTreeNode;

public class SyntheticGraphUtil {

	public static List<Pair<Integer, Double>> getScoresWithDepths(final ISyntheticTreasureIslandProblem problem) {
		DepthFirstSearch<ITransparentTreeNode, Integer> dfs = new DepthFirstSearch<>(problem);


		List<Pair<Integer,Double>> scores = new LinkedList<>();
		while (dfs.hasNext()) {
			try {
				SearchGraphPath<ITransparentTreeNode, Integer> path = dfs.nextSolutionCandidate();
				scores.add(new Pair<>(path.getArcs().size(), problem.getPathEvaluator().evaluate(path)));
			} catch (AlgorithmTimeoutedException | InterruptedException | AlgorithmExecutionCanceledException | AlgorithmException | NoSuchElementException | PathEvaluationException e) {
				e.printStackTrace();
			}
		}
		return scores;
	}
}
