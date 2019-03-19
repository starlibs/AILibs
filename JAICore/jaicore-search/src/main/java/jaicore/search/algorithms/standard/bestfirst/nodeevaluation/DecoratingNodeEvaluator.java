package jaicore.search.algorithms.standard.bestfirst.nodeevaluation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.ILoggingCustomizable;
import jaicore.search.algorithms.standard.bestfirst.exceptions.NodeEvaluationException;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.model.travesaltree.Node;

public abstract class DecoratingNodeEvaluator<T, V extends Comparable<V>> implements INodeEvaluator<T, V>, ICancelableNodeEvaluator, ILoggingCustomizable, IPotentiallyGraphDependentNodeEvaluator<T, V>, IPotentiallySolutionReportingNodeEvaluator<T, V> {

	private boolean canceled = false;
	private Logger logger = LoggerFactory.getLogger(DecoratingNodeEvaluator.class);
	private final INodeEvaluator<T, V> decoratedEvaluator;

	public DecoratingNodeEvaluator(final INodeEvaluator<T, V> evaluator) {
		super();
		if (evaluator == null)
			throw new IllegalArgumentException("The decorated evaluator must not be null!");
		this.decoratedEvaluator = evaluator;
	}

	public INodeEvaluator<T, V> getEvaluator() {
		return this.decoratedEvaluator;
	}

	@Override
	public V f(final Node<T, ?> node) throws NodeEvaluationException, InterruptedException {
		return this.decoratedEvaluator.f(node);
	}

	public boolean isDecoratedEvaluatorCancelable() {
		return this.decoratedEvaluator instanceof ICancelableNodeEvaluator;
	}

	public boolean isDecoratedEvaluatorGraphDependent() {
		return this.decoratedEvaluator instanceof IPotentiallyGraphDependentNodeEvaluator && ((IPotentiallyGraphDependentNodeEvaluator<?,?>)this.decoratedEvaluator).requiresGraphGenerator();
	}
	
	public boolean doesDecoratedEvaluatorReportSolutions() {
		return this.decoratedEvaluator instanceof IPotentiallySolutionReportingNodeEvaluator && ((IPotentiallySolutionReportingNodeEvaluator<?, ?>)this.decoratedEvaluator).reportsSolutions();
	}
	
	/**
	 * default implementation that is just correct with respect to the decorated node evaluator.
	 * If the node evaluator that inherits from DecoratingNodeEvaluator itself may require the graph, this method should be overwritten.
	 * 
	 */
	@Override
	public boolean requiresGraphGenerator() {
		return isDecoratedEvaluatorGraphDependent();
	}
	
	/**
	 * default implementation that is just correct with respect to the decorated node evaluator.
	 * If the node evaluator that inherits from DecoratingNodeEvaluator itself may be solution reporting, this method should be overwritten.
	 * 
	 */
	@Override
	public boolean reportsSolutions() {
		return doesDecoratedEvaluatorReportSolutions();
	}

	@Override
	public void setGenerator(final GraphGenerator<T, ?> generator) {
		logger.info("Setting graph generator of {} to {}", this, generator);
		if (!this.requiresGraphGenerator()) {
			throw new UnsupportedOperationException("This node evaluator is not graph dependent");
		}
		if (!this.isDecoratedEvaluatorGraphDependent()) {
			return;
		}
		((IPotentiallyGraphDependentNodeEvaluator<T, V>) this.decoratedEvaluator).setGenerator(generator);
	}

	public void registerSolutionListener(final Object listener) {
		if (!this.doesDecoratedEvaluatorReportSolutions()) {
			throw new UnsupportedOperationException(this.getClass().getName() + " is not a solution reporting node evaluator");
		}
		((IPotentiallySolutionReportingNodeEvaluator<T, V>) this.decoratedEvaluator).registerSolutionListener(listener);
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
		return logger.getName();
	}
	
	@Override
	public void setLoggerName(String name) {
		logger = LoggerFactory.getLogger(name);
	}	
}
