package ai.libs.jaicore.timing;

import java.util.HashMap;
import java.util.Map;

import org.api4.java.common.attributedobjects.IObjectEvaluator;
import org.api4.java.common.attributedobjects.ObjectEvaluationFailedException;
import org.api4.java.common.control.ILoggingCustomizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.logging.ToJSONStringUtil;

public class TimeRecordingObjectEvaluator<T, V extends Comparable<V>> implements IObjectEvaluator<T, V>, ILoggingCustomizable{
	private Logger logger = LoggerFactory.getLogger(TimeRecordingObjectEvaluator.class);
	private final IObjectEvaluator<T, V> baseEvaluator;
	private final Map<T, Integer> consumedTimes = new HashMap<>();

	public TimeRecordingObjectEvaluator(final IObjectEvaluator<T, V> baseEvaluator) {
		super();
		this.baseEvaluator = baseEvaluator;
	}

	@Override
	public V evaluate(final T object) throws InterruptedException, ObjectEvaluationFailedException {
		long start = System.currentTimeMillis();
		this.logger.info("Starting timed evaluation.");
		V score = this.baseEvaluator.evaluate(object);
		long end = System.currentTimeMillis();
		int runtime = (int) (end - start);
		this.logger.info("Finished evaluation in {}ms. Score is {}", runtime, score);
		this.consumedTimes.put(object, runtime);
		return score;
	}

	public boolean hasEvaluationForComponentInstance(final T inst) {
		return this.consumedTimes.containsKey(inst);
	}

	public int getEvaluationTimeForComponentInstance(final T inst) {
		return this.consumedTimes.get(inst);
	}

	@Override
	public String toString() {
		Map<String, Object> fields = new HashMap<>();
		fields.put("baseEvaluator", this.baseEvaluator);
		fields.put("consumedTimes", this.consumedTimes);
		return ToJSONStringUtil.toJSONString(this.getClass().getSimpleName(), fields);
	}
	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
		if (this.baseEvaluator instanceof ILoggingCustomizable) {
			this.logger.info("Setting logger of evaluator {} to {}.be", this.baseEvaluator.getClass().getName(), name);
			((ILoggingCustomizable) this.baseEvaluator).setLoggerName(name + ".be");
		}
		else {
			this.logger.info("Evaluator {} cannot be customized for logging, so not configuring its logger.", this.baseEvaluator.getClass().getName());
		}
	}
}
