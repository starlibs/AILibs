package ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation;

import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.IPathGoalTester;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.ICancelablePathEvaluator;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPotentiallyGraphDependentPathEvaluator;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPotentiallySolutionReportingPathEvaluator;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.datastructure.graph.ILabeledPath;
import org.api4.java.datastructure.graph.implicit.IGraphGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DecoratingNodeEvaluator<N, A, V extends Comparable<V>>
implements IPathEvaluator<N, A, V>, ICancelablePathEvaluator, ILoggingCustomizable, IPotentiallyGraphDependentPathEvaluator<N, A, V>, IPotentiallySolutionReportingPathEvaluator<N, A, V> {

	private boolean canceled = false;
	private Logger logger = LoggerFactory.getLogger(DecoratingNodeEvaluator.class);
	private final IPathEvaluator<N, A, V> decoratedEvaluator;

	public DecoratingNodeEvaluator(final IPathEvaluator<N, A, V> evaluator) {
		super();
		if (evaluator == null) {
			throw new IllegalArgumentException("The decorated evaluator must not be null!");
		}
		this.decoratedEvaluator = evaluator;
	}

	public IPathEvaluator<N, A, V> getEvaluator() {
		return this.decoratedEvaluator;
	}

	@Override
	public V evaluate(final ILabeledPath<N, A> node) throws PathEvaluationException, InterruptedException {
		return this.decoratedEvaluator.evaluate(node);
	}

	public boolean isDecoratedEvaluatorCancelable() {
		return this.decoratedEvaluator instanceof ICancelablePathEvaluator;
	}

	public boolean isDecoratedEvaluatorGraphDependent() {
		return this.decoratedEvaluator instanceof IPotentiallyGraphDependentPathEvaluator && ((IPotentiallyGraphDependentPathEvaluator<?, ?, ?>) this.decoratedEvaluator).requiresGraphGenerator();
	}

	public boolean doesDecoratedEvaluatorReportSolutions() {
		return this.decoratedEvaluator instanceof IPotentiallySolutionReportingPathEvaluator && ((IPotentiallySolutionReportingPathEvaluator<?, ?, ?>) this.decoratedEvaluator).reportsSolutions();
	}

	/**
	 * default implementation that is just correct with respect to the decorated node evaluator.
	 * If the node evaluator that inherits from DecoratingNodeEvaluator itself may require the graph, this method should be overwritten.
	 *
	 */
	@Override
	public boolean requiresGraphGenerator() {
		return this.isDecoratedEvaluatorGraphDependent();
	}

	/**
	 * default implementation that is just correct with respect to the decorated node evaluator.
	 * If the node evaluator that inherits from DecoratingNodeEvaluator itself may be solution reporting, this method should be overwritten.
	 *
	 */
	@Override
	public boolean reportsSolutions() {
		return this.doesDecoratedEvaluatorReportSolutions();
	}

	@Override
	public void setGenerator(final IGraphGenerator<N, A> generator, final IPathGoalTester<N, A> goalTester) {
		this.logger.info("Setting graph generator of {} to {}", this, generator);
		if (!this.requiresGraphGenerator()) {
			throw new UnsupportedOperationException("This node evaluator is not graph dependent");
		}
		if (!this.isDecoratedEvaluatorGraphDependent()) {
			return;
		}
		((IPotentiallyGraphDependentPathEvaluator<N, A, V>) this.decoratedEvaluator).setGenerator(generator, goalTester);
	}

	@Override
	public void registerSolutionListener(final Object listener) {
		if (!this.doesDecoratedEvaluatorReportSolutions()) {
			throw new UnsupportedOperationException(this.getClass().getName() + " is not a solution reporting node evaluator");
		}
		((IPotentiallySolutionReportingPathEvaluator<N, A, V>) this.decoratedEvaluator).registerSolutionListener(listener);
	}

	@Override
	public void cancelActiveTasks() {
		if (this.canceled) {
			return;
		}
		this.canceled = true;
		if (this.isDecoratedEvaluatorCancelable()) {
			((ICancelablePathEvaluator) this.decoratedEvaluator).cancelActiveTasks();
		}
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
	}
}
