package ai.libs.jaicore.search.problemtransformers;

public class GraphSearchProblemInputToGraphSearchWithSubpathEvaluationViaUninformedness<N, A> extends GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformer<N, A, Double> {

	public GraphSearchProblemInputToGraphSearchWithSubpathEvaluationViaUninformedness() {
		super(n -> 0.0);
	}
}
