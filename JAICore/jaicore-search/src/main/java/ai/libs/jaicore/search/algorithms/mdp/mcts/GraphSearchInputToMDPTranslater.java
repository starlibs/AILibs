package ai.libs.jaicore.search.algorithms.mdp.mcts;

import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IEvaluatedPath;
import org.api4.java.algorithm.IAlgorithmProblemReduction;

import ai.libs.jaicore.search.probleminputs.IMDP;

public class GraphSearchInputToMDPTranslater<N, A> implements IAlgorithmProblemReduction<IPathSearchWithPathEvaluationsInput<N, A, Double>, IEvaluatedPath<N, A, Double>, IMDP<N, A, Double>, IPolicy<N, A>> {

	@Override
	public IMDP<N, A, Double> encodeProblem(final IPathSearchWithPathEvaluationsInput<N, A, Double> problem) {
		return null;
	}

	@Override
	public IEvaluatedPath<N, A, Double> decodeSolution(final IPolicy<N, A> solution) {
		// TODO Auto-generated method stub
		return null;
	}

}
