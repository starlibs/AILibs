package jaicore.search.algorithms.standard.bestfirst.nodeevaluation;

import jaicore.concurrent.TimeoutTimer;
import jaicore.concurrent.TimeoutTimer.TimeoutSubmitter;
import jaicore.search.model.travesaltree.Node;

public class TimedNodeEvaluator<T, V extends Comparable<V>> extends DecoratingNodeEvaluator<T, V> {

	private final int timeoutInMS;
	private final INodeEvaluator<T, V> fallbackNodeEvaluator;

	public TimedNodeEvaluator(INodeEvaluator<T, V> evaluator, final int pTimeoutInMS, final INodeEvaluator<T, V> pFallbackNodeEvaluator) {
		super(evaluator);
		this.timeoutInMS = pTimeoutInMS;
		this.fallbackNodeEvaluator = pFallbackNodeEvaluator;
	}

	@Override
	public V f(Node<T, ?> node) throws Exception {
		TimeoutSubmitter ts = TimeoutTimer.getInstance().getSubmitter();
		int taskId = ts.interruptMeAfterMS(timeoutInMS);
		try {
			V result = super.f(node);
			ts.cancelTimeout(taskId);
			ts.close();
			return result;
		} catch (InterruptedException e) {
			return fallbackNodeEvaluator.f(node);
		}
	}
}
