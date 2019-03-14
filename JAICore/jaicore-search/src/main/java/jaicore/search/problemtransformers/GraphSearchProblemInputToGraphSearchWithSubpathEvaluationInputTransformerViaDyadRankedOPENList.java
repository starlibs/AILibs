package jaicore.search.problemtransformers;

import java.util.Random;

import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.RandomCompletionBasedNodeEvaluator;
import jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public class GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformerViaDyadRankedOPENList<N, A>
		extends GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformer<N, A, Double> {

	private final int timeoutForSingleCompletionEvaluationInMS;

	public GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformerViaDyadRankedOPENList(
			final int timeoutForSingleCompletionEvaluationInMS) {
		super();
		this.timeoutForSingleCompletionEvaluationInMS = timeoutForSingleCompletionEvaluationInMS;
	}

	@Override
	public GraphSearchWithSubpathEvaluationsInput<N, A, Double> transform(
			final GraphSearchWithPathEvaluationsInput<N, A, Double> problem) {
		RandomCompletionBasedNodeEvaluator<N, Double> rdfsNodeEvaluator = new RandomCompletionBasedNodeEvaluator<>(
				new Random(0), 1, problem.getPathEvaluator(), this.timeoutForSingleCompletionEvaluationInMS,
				this.timeoutForSingleCompletionEvaluationInMS, null);

		this.setNodeEvaluator(n -> {
			System.err.println("Use node eval");
			if (n.isGoal()) {
				System.err.println("for goal");
				return rdfsNodeEvaluator.f(n);
			} else {
				return 0.0;
			}
		});

		return super.transform(problem);
	}
}
