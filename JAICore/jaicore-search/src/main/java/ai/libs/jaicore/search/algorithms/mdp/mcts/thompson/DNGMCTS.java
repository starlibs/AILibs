package ai.libs.jaicore.search.algorithms.mdp.mcts.thompson;

import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.INodeGoalTester;

import ai.libs.jaicore.search.algorithms.mdp.mcts.MCTS;
import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.probleminputs.IMDP;

public class DNGMCTS<N, A> extends MCTS<N, A> {

	public DNGMCTS(final IMDP<N, A, Double> input, final double varianceFactor, final double initLambda, final double maxIterations, final double gamma, final double epsilon) {
		super(input, new DNGPolicy<>((INodeGoalTester<N, A>) input.getGoalTester(), n -> problem.getPathEvaluator().evaluate(new SearchGraphPath<>(n)), varianceFactor, initLambda), this.defaultPolicy, maxIterations, gamma, epsilon);
		// TODO Auto-generated constructor stub
	}

	//	public DNGMCTS(final I problem, final long seed, final double varianceFactor, final double initLambda) {
	//		super(problem, ,
	//				new UniformRandomPolicy<>(new Random(seed + DNGMCTS.class.hashCode())), 0.0);
	//	}
}
