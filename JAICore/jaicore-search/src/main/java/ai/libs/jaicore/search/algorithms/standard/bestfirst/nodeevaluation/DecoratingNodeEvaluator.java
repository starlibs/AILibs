package ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation;

import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.IGraphGenerator;
import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.IPath;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.ICancelableNodeEvaluator;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPotentiallyGraphDependentPathEvaluator;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPotentiallySolutionReportingPathEvaluator;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.common.control.ILoggingCustomizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DecoratingNodeEvaluator<T, A, V extends Comparable<V>>
implements IPathEvaluator<T, A, V>, ICancelableNodeEvaluator, ILoggingCustomizable, IPotentiallyGraphDependentPathEvaluator<T, A, V>, IPotentiallySolutionReportingPathEvaluator<T, A, V> {

	private boolean canceled = false;
	private Logger logger = LoggerFactory.getLogger(DecoratingNodeEvaluator.class);
	private final IPathEvaluator<T, A, V> decoratedEvaluator;

	public DecoratingNodeEvaluator(final IPathEvaluator<T, A, V> evaluator) {
		super();
		if (evaluator == null) {
			throw new IllegalArgumentException("The decorated evaluator must not be null!");
		}
		this.decoratedEvaluator = evaluator;
	}

	public IPathEvaluator<T, A, V> getEvaluator() {
		return this.decoratedEvaluator;
	}

	@Override
	public V f(final IPath<T, A> node) throws PathEvaluationException, InterruptedException {
		return this.decoratedEvaluator.f(node);
	}

	public boolean isDecoratedEvaluatorCancelable() {
		return this.decoratedEvaluator instanceof ICancelableNodeEvaluator;
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
	public void setGenerator(final IGraphGenerator<T, A> generator) {
		this.logger.info("Setting graph generator of {} to {}", this, generator);
		if (!this.requiresGraphGenerator()) {
			throw new UnsupportedOperationException("This node evaluator is not graph dependent");
		}
		if (!this.isDecoratedEvaluatorGraphDependent()) {
			return;
		}
		((IPotentiallyGraphDependentPathEvaluator<T, A, V>) this.decoratedEvaluator).setGenerator(generator);
	}

	@Override
	public void registerSolutionListener(final Object listener) {
		if (!this.doesDecoratedEvaluatorReportSolutions()) {
			throw new UnsupportedOperationException(this.getClass().getName() + " is not a solution reporting node evaluator");
		}
		((IPotentiallySolutionReportingPathEvaluator<T, A, V>) this.decoratedEvaluator).registerSolutionListener(listener);
	}

	@Override
	public void cancelActiveTasks() {
		if (this.canceled) {
			return;
		}
		this.canceled = true;
		if (this.isDecoratedEvaluatorCancelable()) {
			((ICancelableNodeEvaluator) this.decoratedEvaluator).cancelActiveTasks();
		}
		if (this instanceof ICancelableNodeEvaluator) {
			((ICancelableNodeEvaluator) this).cancelActiveTasks();
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
