package ai.libs.jaicore.search.landscapeanalysis;

import java.util.ArrayList;
import java.util.List;

import org.api4.java.ai.graphsearch.problem.IGraphSearchWithPathEvaluationsInput;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.datastructure.graph.IPath;
import org.api4.java.datastructure.graph.implicit.NodeExpansionDescription;

import ai.libs.jaicore.search.model.other.SearchGraphPath;

public class LandscapeAnalyzer {



	private final TypeEncapsulator<?, ?> capsula;

	public <N, A> LandscapeAnalyzer(final IGraphSearchWithPathEvaluationsInput<N, A, Double> problem) {
		super();
		this.capsula = new TypeEncapsulator<>(problem, problem.getGraphGenerator().getRootGenerator().getRoots().iterator().next(), problem.getGraphGenerator().getSuccessorGenerator());
	}

	public double[] getValues(final int probeSize) throws InterruptedException, PathEvaluationException {
		return this.getValues(new SearchGraphPath<>(this.capsula.root), probeSize, LandscapeAnalysisCompletionTechnique.FIRST);
	}

	public double[] getValues(final List<Integer> childIndicesOfPath, final int probeSize, final LandscapeAnalysisCompletionTechnique technique) throws InterruptedException, PathEvaluationException {
		return this.capsula.getValues(childIndicesOfPath, probeSize, technique);
	}

	public double[] getValues(final IPath path, final int probeSize) throws InterruptedException, PathEvaluationException {
		return this.getValues(path, probeSize, LandscapeAnalysisCompletionTechnique.FIRST);
	}

	public double[] getValues(final IPath path, final int probeSize, final LandscapeAnalysisCompletionTechnique technique) throws InterruptedException, PathEvaluationException {
		return this.capsula.getValues(path, probeSize, technique);
	}


}
}
