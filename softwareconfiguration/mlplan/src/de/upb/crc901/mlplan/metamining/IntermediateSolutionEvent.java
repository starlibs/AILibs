package de.upb.crc901.mlplan.metamining;

import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.model.MLPipeline;
import jaicore.basic.algorithm.AlgorithmEvent;

public class IntermediateSolutionEvent implements AlgorithmEvent {
	
	String classifier;
	String searcher;
	String evaluator;
	
	double score;
	
	long foundAt;
	
	public IntermediateSolutionEvent(MLPipeline foundPipeline, double score, long foundAt) {
		this.classifier=foundPipeline.getBaseClassifier().getClass().getName();
		if (foundPipeline.getPreprocessors() != null && foundPipeline.getPreprocessors().size() > 0) {
			this.searcher = foundPipeline.getPreprocessors().get(0).getSearcher().getClass().getName();
			this.evaluator = foundPipeline.getPreprocessors().get(0).getEvaluator().getClass().getName();
		}
		this.score=score;
		this.foundAt=foundAt;
	}

	public String getClassifier() {
		return classifier;
	}

	public String getSearcher() {
		return searcher;
	}

	public String getEvaluator() {
		return evaluator;
	}

	public double getScore() {
		return score;
	}

	public long getFoundAt() {
		return foundAt;
	}
}
