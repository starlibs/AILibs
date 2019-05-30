package de.upb.crc901.mlplan.metamining;

import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.model.MLPipeline;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import weka.classifiers.Classifier;

public class IntermediateSolutionEvent implements AlgorithmEvent {
	
	String algorithmId;
	
	String classifier;
	String searcher;
	String evaluator;
	
	double score;
	
	long foundAt;
	
	public IntermediateSolutionEvent(String algorithmId, Classifier classifier, double score, long foundAt) {
		this.algorithmId = algorithmId;
		if (classifier instanceof MLPipeline) {
			MLPipeline pl = (MLPipeline) classifier;
			this.classifier=pl.getBaseClassifier().getClass().getName();
			if (pl.getPreprocessors() != null && !pl.getPreprocessors().isEmpty()) {
				this.searcher = pl.getPreprocessors().get(0).getSearcher().getClass().getName();
				this.evaluator = pl.getPreprocessors().get(0).getEvaluator().getClass().getName();
			}
		} else {
			this.classifier = classifier.getClass().getName();
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

	@Override
	public String getAlgorithmId() {
		return algorithmId;
	}

	@Override
	public long getTimestamp() {
		return foundAt;
	}
}
