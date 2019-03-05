package de.upb.crc901.mlplan.core.events;

import jaicore.basic.algorithm.events.ASolutionCandidateFoundEvent;
import jaicore.basic.algorithm.events.ScoredSolutionCandidateFoundEvent;
import weka.classifiers.Classifier;

public class ClassifierFoundEvent extends ASolutionCandidateFoundEvent<Classifier> implements ScoredSolutionCandidateFoundEvent<Classifier, Double> {

	private final double inSampleError;

	public ClassifierFoundEvent(String algorithmId, Classifier solutionCandidate, double inSampleError) {
		super(algorithmId, solutionCandidate);
		this.inSampleError = inSampleError;
	}

	public double getInSampleError() {
		return inSampleError;
	}

	@Override
	public Double getScore() {
		return inSampleError;
	}	
}
