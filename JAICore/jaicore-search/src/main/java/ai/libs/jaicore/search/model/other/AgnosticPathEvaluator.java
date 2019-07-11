package ai.libs.jaicore.search.model.other;

import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.IPath;
import org.api4.java.common.attributedobjects.IObjectEvaluator;

public class AgnosticPathEvaluator<N, A> implements IObjectEvaluator<IPath<N, A>, Double> {

	@Override
	public Double evaluate(final IPath<N, A> solutionPath) {
		return 0.0;
	}
}
