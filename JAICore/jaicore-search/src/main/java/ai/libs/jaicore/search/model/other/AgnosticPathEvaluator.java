package ai.libs.jaicore.search.model.other;

import org.api4.java.common.attributedobjects.IObjectEvaluator;

public class AgnosticPathEvaluator<N, A> implements IObjectEvaluator<SearchGraphPath<N, A>, Double> {

	@Override
	public Double evaluate(final SearchGraphPath<N, A> solutionPath) {
		return 0.0;
	}
}
