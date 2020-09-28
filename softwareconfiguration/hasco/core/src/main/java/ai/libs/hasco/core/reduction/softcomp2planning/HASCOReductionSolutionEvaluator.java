package ai.libs.hasco.core.reduction.softcomp2planning;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.api4.java.common.attributedobjects.IObjectEvaluator;
import org.api4.java.common.attributedobjects.ObjectEvaluationFailedException;
import org.api4.java.common.control.ILoggingCustomizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.jaicore.components.model.RefinementConfiguredSoftwareConfigurationProblem;
import ai.libs.jaicore.logging.ToJSONStringUtil;
import ai.libs.jaicore.planning.core.Action;
import ai.libs.jaicore.planning.core.interfaces.IPlan;
import ai.libs.jaicore.timing.TimeRecordingObjectEvaluator;

public class HASCOReductionSolutionEvaluator<V extends Comparable<V>> implements IObjectEvaluator<IPlan, V>, ILoggingCustomizable {

	private Logger logger = LoggerFactory.getLogger(HASCOReductionSolutionEvaluator.class);
	private final RefinementConfiguredSoftwareConfigurationProblem<V> configurationProblem;
	private final HASCOReduction<V> reduction;
	private final IObjectEvaluator<IComponentInstance, V> evaluator;
	private final TimeRecordingObjectEvaluator<IComponentInstance, V> timedEvaluator;

	public HASCOReductionSolutionEvaluator(final RefinementConfiguredSoftwareConfigurationProblem<V> configurationProblem, final HASCOReduction<V> reduction) {
		super();
		this.configurationProblem = configurationProblem;
		this.reduction = reduction;
		this.evaluator = this.configurationProblem.getCompositionEvaluator();
		this.timedEvaluator = new TimeRecordingObjectEvaluator<>(this.evaluator);
	}

	public HASCOReduction<V> getReduction() {
		return this.reduction;
	}

	@Override
	public V evaluate(final IPlan plan) throws InterruptedException, ObjectEvaluationFailedException {
		ComponentInstance solution = this.reduction.decodeSolution(plan);
		if (solution == null) {
			throw new IllegalArgumentException("The following plan yields a null solution: \n\t" + plan.getActions().stream().map(Action::getEncoding).collect(Collectors.joining("\n\t")));
		}
		this.logger.info("Forwarding evaluation request for CI {} to evaluator {}", solution, this.evaluator.getClass().getName());
		return this.timedEvaluator.evaluate(solution);
	}

	@Override
	public String toString() {
		Map<String, Object> fields = new HashMap<>();
		fields.put("problem", this.configurationProblem);
		return ToJSONStringUtil.toJSONString(this.getClass().getSimpleName(), fields);
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
		if (this.evaluator instanceof ILoggingCustomizable) {
			this.logger.info("Setting logger of evaluator {} to {}.be", this.evaluator.getClass().getName(), name);
			((ILoggingCustomizable) this.evaluator).setLoggerName(name + ".be");
		} else {
			this.logger.info("Evaluator {} cannot be customized for logging, so not configuring its logger.", this.evaluator.getClass().getName());
		}
	}

	public IObjectEvaluator<IComponentInstance, V> getEvaluator() {
		return this.evaluator;
	}

	public TimeRecordingObjectEvaluator<IComponentInstance, V> getTimedEvaluator() {
		return this.timedEvaluator;
	}
}
