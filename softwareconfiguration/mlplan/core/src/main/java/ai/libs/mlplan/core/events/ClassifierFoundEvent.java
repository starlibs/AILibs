package ai.libs.mlplan.core.events;

import java.util.HashMap;
import java.util.Map;

import org.api4.java.ai.ml.core.learner.ISupervisedLearner;
import org.api4.java.algorithm.IAlgorithm;
import org.api4.java.algorithm.events.result.IScoredSolutionCandidateFoundEvent;

import ai.libs.hasco.model.ComponentInstance;
import ai.libs.jaicore.basic.algorithm.ASolutionCandidateFoundEvent;
import ai.libs.jaicore.logging.ToJSONStringUtil;

public class ClassifierFoundEvent extends ASolutionCandidateFoundEvent<ISupervisedLearner<?, ?>> implements IScoredSolutionCandidateFoundEvent<ISupervisedLearner<?, ?>, Double> {

	private final double inSampleError;
	private final ComponentInstance componentDescription;

	public ClassifierFoundEvent(final IAlgorithm<?, ?> algorithm, final ComponentInstance componentDescription, final ISupervisedLearner<?, ?> solutionCandidate, final double inSampleError) {
		super(algorithm, solutionCandidate);
		this.inSampleError = inSampleError;
		this.componentDescription = componentDescription;
	}

	public double getInSampleError() {
		return this.inSampleError;
	}

	@Override
	public Double getScore() {
		return this.inSampleError;
	}

	public ComponentInstance getComponentDescription() {
		return this.componentDescription;
	}

	@Override
	public String toString() {
		Map<String, Object> fields = new HashMap<>();
		fields.put("candidate", super.getSolutionCandidate());
		fields.put("componentDescription", this.componentDescription);
		fields.put("inSampleError", this.inSampleError);
		return ToJSONStringUtil.toJSONString(fields);
	}
}
