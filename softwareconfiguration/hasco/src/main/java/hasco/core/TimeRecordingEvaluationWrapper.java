package hasco.core;

import java.util.HashMap;
import java.util.Map;

import hasco.model.ComponentInstance;
import jaicore.basic.IInformedObjectEvaluatorExtension;
import jaicore.basic.IObjectEvaluator;
import jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;
import jaicore.logging.ToJSONStringUtil;

public class TimeRecordingEvaluationWrapper<V extends Comparable<V>> implements IObjectEvaluator<ComponentInstance, V>, IInformedObjectEvaluatorExtension<V> {

	private final IObjectEvaluator<ComponentInstance, V> baseEvaluator;
	private final Map<ComponentInstance, Integer> consumedTimes = new HashMap<>();

	public TimeRecordingEvaluationWrapper(final IObjectEvaluator<ComponentInstance, V> baseEvaluator) {
		super();
		this.baseEvaluator = baseEvaluator;
	}

	@Override
	public V evaluate(final ComponentInstance object) throws InterruptedException, ObjectEvaluationFailedException {
		long start = System.currentTimeMillis();
		V score = this.baseEvaluator.evaluate(object);
		long end = System.currentTimeMillis();
		this.consumedTimes.put(object, (int) (end - start));
		return score;
	}

	public boolean hasEvaluationForComponentInstance(final ComponentInstance inst) {
		return this.consumedTimes.containsKey(inst);
	}

	public int getEvaluationTimeForComponentInstance(final ComponentInstance inst) {
		return this.consumedTimes.get(inst);
	}

	@Override
	public String toString() {
		Map<String, Object> fields = new HashMap<>();
		fields.put("baseEvaluator", this.baseEvaluator);
		fields.put("consumedTimes", this.consumedTimes);
		return ToJSONStringUtil.toJSONString(this.getClass().getSimpleName(), fields);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void updateBestScore(V bestScore) {
		if(baseEvaluator instanceof IInformedObjectEvaluatorExtension) {
			((IInformedObjectEvaluatorExtension<V>) baseEvaluator).updateBestScore(bestScore);
		}
	}
	
}
