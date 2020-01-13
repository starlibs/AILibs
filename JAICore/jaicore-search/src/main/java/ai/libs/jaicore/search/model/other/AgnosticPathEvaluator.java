package ai.libs.jaicore.search.model.other;

import org.api4.java.common.attributedobjects.IObjectEvaluator;
import org.api4.java.datastructure.graph.ILabeledPath;

public class AgnosticPathEvaluator<N, A> implements IObjectEvaluator<ILabeledPath<N, A>, Double> {

	@Override
	public Double evaluate(final ILabeledPath<N, A> solutionPath) {
		return 0.0;
	}
}
