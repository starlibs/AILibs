package jaicore.ml.evaluation.evaluators.weka;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.ml.WekaUtil;
import jaicore.ml.core.evaluation.measure.IMeasure;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class CVEvaluator implements IClassifierEvaluator {

	static final Logger logger = LoggerFactory.getLogger(CVEvaluator.class);
	private final IMeasure<Double,Double> evaluator;
	private boolean canceled = false;
	private final int folds;
	private Instances data;

	public CVEvaluator(IMeasure<Double, Double> evaluator, int folds) throws IOException {
		super();
		this.evaluator = evaluator;
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
			List<Double> actual = WekaUtil.getClassesAsList(test);
			List<Double> predicted = new ArrayList<>();
			c.buildClassifier(train);
			double score = evaluator.calculateAvgMeasure(actual, predicted);
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
