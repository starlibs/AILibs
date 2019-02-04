package jaicore.search.algorithms.standard.bestfirst.nodeevaluation;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.logging.ToJSONStringUtil;
import jaicore.search.algorithms.standard.bestfirst.exceptions.NodeEvaluationException;
import jaicore.search.model.travesaltree.Node;

/**
 * This node evaluator can be used
 * 	a) if there is a prioritized node evaluator that should be used unless it returns NULL
 *  b) to realize dead-end recognition
 *  c) to use different node evaluators in different regions of the search graph
 *
 * @author fmohr
 *
 * @param <T>
 * @param <V>
 */
public class AlternativeNodeEvaluator<T, V extends Comparable<V>> extends DecoratingNodeEvaluator<T, V> {

	private final INodeEvaluator<T, V> ne1;

	public AlternativeNodeEvaluator(final INodeEvaluator<T, V> ne1, final INodeEvaluator<T, V> ne2) {
		super(ne2);
		this.ne1 = ne1;
	}

	@Override
	public V f(final Node<T, ?> node) throws NodeEvaluationException, TimeoutException, AlgorithmExecutionCanceledException, InterruptedException {
		V f1 = this.ne1.f(node);
		if (f1 != null) {
			return f1;
		}
		return super.f(node);
	}

	@Override
	public String toString() {
		Map<String, Object> fields = new HashMap<>();
		fields.put("primary", this.ne1);
		fields.put("secondary", super.getEvaluator());
		return ToJSONStringUtil.toJSONString(this.getClass().getSimpleName(), fields);
	}
}
