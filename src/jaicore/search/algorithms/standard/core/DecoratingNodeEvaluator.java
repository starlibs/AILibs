package jaicore.search.algorithms.standard.core;

import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.Node;

public abstract class DecoratingNodeEvaluator<T,V extends Comparable<V>> implements INodeEvaluator<T, V> {
	
	private INodeEvaluator<T, V> evaluator;
	
	public DecoratingNodeEvaluator(INodeEvaluator<T, V> evaluator) {
		super();
		this.evaluator = evaluator;
	}

	public INodeEvaluator<T,V> getEvaluator() {
		return this.evaluator;
	}
	
	@Override
	public V f(Node<T,?> node) throws Exception {
		return this.evaluator.f(node);
	}
	
	public boolean isDecoratedEvaluatorCancelable() {
		return this.evaluator instanceof ICancelableNodeEvaluator;
	}
	
	public boolean isCancelable() {
		return this instanceof ICancelableNodeEvaluator && isDecoratedEvaluatorCancelable();
	}
	
	public boolean isDecoratedEvaluatorGraphDependent() {
		if (this.evaluator instanceof DecoratingNodeEvaluator<?,?>)
			return ((DecoratingNodeEvaluator<T,V>) this.evaluator).isGraphDependent();
		return this.evaluator instanceof IGraphDependentNodeEvaluator<?, ?, ?>;
	}
	
	public boolean isGraphDependent() {
		return this instanceof IGraphDependentNodeEvaluator<?,?,?> || isDecoratedEvaluatorGraphDependent();
	}
	
	public boolean isDecoratedEvaluatorSolutionReporter() {
		if (this.evaluator instanceof DecoratingNodeEvaluator<?,?>)
			return ((DecoratingNodeEvaluator<T,V>) this.evaluator).isSolutionReporter();
		return (this.evaluator instanceof ISolutionReportingNodeEvaluator<?,?>); 
	}
	
	public boolean isSolutionReporter() {
		return this instanceof ISolutionReportingNodeEvaluator<?,?> || isDecoratedEvaluatorSolutionReporter();
	}
	
	/* here we have the default implementations for the GraphDependent and SolutionReporter interfaces */
	@SuppressWarnings("unchecked")
	public <A> void setGenerator(GraphGenerator<T, A> generator) {
		if (!this.isGraphDependent())
			throw new UnsupportedOperationException("This node evaluator is not graph dependent");
		if (!this.isDecoratedEvaluatorGraphDependent())
			return;
		((IGraphDependentNodeEvaluator<T, A, V>)this.evaluator).setGenerator(generator);
	}
	
	public SolutionEventBus<T> getSolutionEventBus() {
		if (this.isDecoratedEvaluatorSolutionReporter())
			return ((ISolutionReportingNodeEvaluator<T, V>)this.evaluator).getSolutionEventBus();
		throw new UnsupportedOperationException("This node evaluator is not a solution reporter");
	}
}
