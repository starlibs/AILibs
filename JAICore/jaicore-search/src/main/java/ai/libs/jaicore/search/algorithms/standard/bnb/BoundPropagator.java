package ai.libs.jaicore.search.algorithms.standard.bnb;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPotentiallySolutionReportingPathEvaluator;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.datastructure.graph.ILabeledPath;

import com.google.common.eventbus.EventBus;

import ai.libs.jaicore.search.algorithms.standard.bestfirst.events.EvaluatedSearchSolutionCandidateFoundEvent;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.model.other.SearchGraphPath;

public class BoundPropagator<N, A> implements IPathEvaluator<N, A, Double>, IPotentiallySolutionReportingPathEvaluator<N, A, Double> {

	private final IPathEvaluator<N, A, Double> lowerBoundComputer;
	private final EventBus bus = new EventBus();

	public BoundPropagator(final IPathEvaluator<N, A, Double> lowerBoundComputer) {
		super();
		this.lowerBoundComputer = lowerBoundComputer;
	}

	@Override
	public void registerSolutionListener(final Object listener) {
		this.bus.register(listener);
	}

	@Override
	public boolean reportsSolutions() {
		return true;
	}

	@Override
	public Double evaluate(final ILabeledPath<N, A> path) throws PathEvaluationException, InterruptedException {
		double bound = this.lowerBoundComputer.evaluate(path);
		this.bus.post(new EvaluatedSearchSolutionCandidateFoundEvent<N, A, Double>(null, new EvaluatedSearchGraphPath<>(new SearchGraphPath<N, A>(path.getRoot()), bound)));
		return bound;
	}

}
