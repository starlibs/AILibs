package de.upb.crc901.mlplan.metamining;

import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.model.MLPipeline;
import jaicore.basic.algorithm.events.AAlgorithmEvent;
import weka.classifiers.Classifier;

public class IntermediateSolutionEvent extends AAlgorithmEvent {

	private String classifier;
	private String searcher;
	private String evaluator;

	private double score;
	public IntermediateSolutionEvent(final String algorithmId, final Classifier classifier, final double score) {
		super (algorithmId);
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
	}

	public String getClassifier() {
		return this.classifier;
	}

	public String getSearcher() {
		return this.searcher;
	}

	public String getEvaluator() {
		return this.evaluator;
	}

	public double getScore() {
		return this.score;
	}
}
