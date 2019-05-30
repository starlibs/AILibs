package de.upb.crc901.mlplan.core.events;

import java.util.HashMap;
import java.util.Map;

import hasco.model.ComponentInstance;
import jaicore.basic.algorithm.events.ASolutionCandidateFoundEvent;
import jaicore.basic.algorithm.events.ScoredSolutionCandidateFoundEvent;
import jaicore.logging.ToJSONStringUtil;
import weka.classifiers.Classifier;

public class ClassifierFoundEvent extends ASolutionCandidateFoundEvent<Classifier> implements ScoredSolutionCandidateFoundEvent<Classifier, Double> {

	private final double inSampleError;
	private final ComponentInstance componentDescription;

	public ClassifierFoundEvent(final String algorithmId, final ComponentInstance componentDescription, final Classifier solutionCandidate, final double inSampleError) {
		super(algorithmId, solutionCandidate);
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
