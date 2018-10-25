package hasco.core;

import java.util.HashMap;
import java.util.Map;

import hasco.model.ComponentInstance;
import jaicore.basic.IObjectEvaluator;

public class TimeRecordingEvaluationWrapper<V extends Comparable<V>> implements IObjectEvaluator<ComponentInstance, V> {

	private final IObjectEvaluator<ComponentInstance, V> baseEvaluator;
	private final Map<ComponentInstance, Integer> consumedTimes = new HashMap<>();

	public TimeRecordingEvaluationWrapper(IObjectEvaluator<ComponentInstance, V> baseEvaluator) {
		super();
		this.baseEvaluator = baseEvaluator;
	}

	@Override
	public V evaluate(ComponentInstance object) throws Exception {
		long start = System.currentTimeMillis();
		V score = baseEvaluator.evaluate(object);
		long end = System.currentTimeMillis();
		consumedTimes.put(object, (int) (end - start));
		return score;
	}
	
	public boolean hasEvaluationForComponentInstance(ComponentInstance inst) {
		return consumedTimes.containsKey(inst);
	}

	public int getEvaluationTimeForComponentInstance(ComponentInstance inst) {
		return consumedTimes.get(inst);
	}
}
