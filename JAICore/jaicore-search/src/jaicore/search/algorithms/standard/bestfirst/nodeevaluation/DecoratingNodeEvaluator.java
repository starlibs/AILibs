package jaicore.search.algorithms.standard.bestfirst.nodeevaluation;

import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.model.travesaltree.Node;

public abstract class DecoratingNodeEvaluator<T, V extends Comparable<V>> implements INodeEvaluator<T, V>, ICancelableNodeEvaluator {

	private boolean canceled = false;
	private INodeEvaluator<T, V> evaluator;

	public DecoratingNodeEvaluator(INodeEvaluator<T, V> evaluator) {
		super();
		this.evaluator = evaluator;
	}

	public INodeEvaluator<T, V> getEvaluator() {
		return this.evaluator;
	}

	@Override
	public V f(Node<T, ?> node) throws Exception {
		return this.evaluator.f(node);
	}

	public boolean isDecoratedEvaluatorCancelable() {
		return this.evaluator instanceof ICancelableNodeEvaluator;
	}

	public boolean isDecoratedEvaluatorGraphDependent() {
		if (this.evaluator instanceof DecoratingNodeEvaluator<?, ?>)
			return ((DecoratingNodeEvaluator<T, V>) this.evaluator).isGraphDependent();
		return this.evaluator instanceof IGraphDependentNodeEvaluator<?, ?, ?>;
	}

	public boolean isGraphDependent() {
		return this instanceof IGraphDependentNodeEvaluator<?, ?, ?> || isDecoratedEvaluatorGraphDependent();
	}

	public boolean isDecoratedEvaluatorSolutionReporter() {
		if (this.evaluator instanceof DecoratingNodeEvaluator<?, ?>)
			return ((DecoratingNodeEvaluator<T, V>) this.evaluator).isSolutionReporter();
		return (this.evaluator instanceof ISolutionReportingNodeEvaluator<?, ?>);
	}

	public boolean isSolutionReporter() {
		return this instanceof ISolutionReportingNodeEvaluator<?, ?> || isDecoratedEvaluatorSolutionReporter();
	}

	/* here we have the default implementations for the GraphDependent and SolutionReporter interfaces */
	@SuppressWarnings("unchecked")
	public <A> void setGenerator(GraphGenerator<T, A> generator) {
		if (!this.isGraphDependent())
			throw new UnsupportedOperationException("This node evaluator is not graph dependent");
		if (!this.isDecoratedEvaluatorGraphDependent())
			return;
		((IGraphDependentNodeEvaluator<T, A, V>) this.evaluator).setGenerator(generator);
	}

	public void registerSolutionListener(Object listener) {
		if (!this.isDecoratedEvaluatorSolutionReporter())
			throw new UnsupportedOperationException(this.getClass().getName() + " is not a solution reporting node evaluator");
		((ISolutionReportingNodeEvaluator<T, V>) this.evaluator).registerSolutionListener(listener);
	}

	@Override
	public void cancel() {
		if (canceled)
			return;
		canceled = true;
		if (isDecoratedEvaluatorCancelable()) {
			((ICancelableNodeEvaluator)evaluator).cancel();
		}
		if (this instanceof ICancelableNodeEvaluator) {
			((ICancelableNodeEvaluator)this).cancel();
		}
	}
}
