package ai.libs.jaicore.search.algorithms.mcts.samegame;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IEvaluatedPath;

import ai.libs.jaicore.basic.algorithm.AAlgorithmTestProblemSet;
import ai.libs.jaicore.basic.algorithm.AlgorithmTestProblemSetCreationException;
import ai.libs.jaicore.basic.algorithm.IAlgorithmTestProblemSetForSolutionIterators;
import ai.libs.jaicore.problems.samegame.SameGameCell;
import ai.libs.jaicore.problems.samegame.SameGameGenerator;
import ai.libs.jaicore.search.exampleproblems.samegame.SameGameGraphSearchProblem;
import ai.libs.jaicore.search.exampleproblems.samegame.SameGameNode;

public class SameGameGraphSearchSet extends AAlgorithmTestProblemSet<IPathSearchWithPathEvaluationsInput<SameGameNode, SameGameCell, Double>> implements IAlgorithmTestProblemSetForSolutionIterators<IPathSearchWithPathEvaluationsInput<SameGameNode, SameGameCell, Double>, IEvaluatedPath<SameGameNode, SameGameCell, Double>> {

	public SameGameGraphSearchSet() {
		super("Same Game");
	}

	@Override
	public IPathSearchWithPathEvaluationsInput<SameGameNode, SameGameCell, Double> getSimpleProblemInputForGeneralTestPurposes() throws AlgorithmTestProblemSetCreationException, InterruptedException {
		return new SameGameGraphSearchProblem(new SameGameGenerator().generate(5, 5, 2, .5, new Random(0)));
	}

	@Override
	public IPathSearchWithPathEvaluationsInput<SameGameNode, SameGameCell, Double> getDifficultProblemInputForGeneralTestPurposes() throws AlgorithmTestProblemSetCreationException, InterruptedException {
		return new SameGameGraphSearchProblem(new SameGameGenerator().generate(0));
	}

	@Override
	public Map<IPathSearchWithPathEvaluationsInput<SameGameNode, SameGameCell, Double>, Collection<IEvaluatedPath<SameGameNode, SameGameCell, Double>>> getProblemsWithSolutions() throws InterruptedException {
		return new HashMap<>();
	}
}
