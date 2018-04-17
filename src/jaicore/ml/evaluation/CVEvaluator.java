package jaicore.ml.evaluation;

import java.io.IOException;
import java.util.Random;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.classifiers.Classifier;
import weka.core.Instances;

public class CVEvaluator implements ClassifierEvaluator {

	static final Logger logger = LoggerFactory.getLogger(CVEvaluator.class);
	private final MulticlassEvaluator evaluator = new MulticlassEvaluator(new Random(System.currentTimeMillis()));
	private boolean canceled = false;
	private final int folds;
	private Instances data;

	public CVEvaluator(int folds) throws IOException {
		super();
		this.folds = folds;
	}

	@Override
	public Double evaluate(Classifier c) throws Exception {
		
		/* perform random stratified split */
		DescriptiveStatistics stats = new DescriptiveStatistics();
		logger.info("Starting evaluation of {}", c);
		data.stratify(folds);
		for (int i = 0; i < folds && !canceled; i++) {
			logger.info("Evaluating {} with split #{}/{}", c, i + 1, folds);
			Instances train = data.trainCV(folds, i);
			Instances test = data.testCV(folds, i);
			int score = (int) Math.round(evaluator.getErrorRateForSplit(c, train, test) * 100);
			logger.info("Score for evaluation of {} with split #{}/{}: {}", c, i + 1, folds, score);
			stats.addValue(score);
		}

		Double score = stats.getMean();
		logger.info("Obtained score of {} for classifier {}.", score, c);
		return score;
	}

	public Instances getData() {
		return data;
	}

	public void setData(Instances data) {
		this.data = data;
	}
}
