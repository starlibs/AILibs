package de.upb.crc901.mlplan.search.evaluators;

import java.io.IOException;
import java.util.Random;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.mlplan.core.SolutionEvaluator;
import weka.classifiers.Classifier;
import weka.core.Instances;

@SuppressWarnings("serial")
public class CVEvaluator implements SolutionEvaluator {
	
	static final Logger logger = LoggerFactory.getLogger(CVEvaluator.class);
	private final MulticlassEvaluator evaluator = new MulticlassEvaluator(new Random(System.currentTimeMillis()));
	private Instances data;
	private boolean canceled = false;
	private final int folds;

	public CVEvaluator(int folds) throws IOException {
		super();
		this.folds = folds;
	}

	@Override
	public void cancel() {
		logger.info("Received cancel");
		this.canceled = true;
	}

	@Override
	public void setData(Instances data) {
		if (this.data != null)
			throw new IllegalStateException("Cannot reset the data");
		this.data = data;
	}

	@Override
	public Integer getSolutionScore(Classifier c) throws Exception {
		
		/* perform random stratified split */
		DescriptiveStatistics stats = new DescriptiveStatistics();
		logger.info("Starting evaluation of {}", c);
		try {
			data.stratify(folds);
			for (int i = 0; i < folds && !canceled; i++) {
				logger.info("Evaluating {} with split #{}/{}", c, i + 1, folds);
				Instances train = data.trainCV(folds, i);
				Instances test = data.testCV(folds, i);
				int score = (int) Math.round(evaluator.getErrorRateForSplit(c, train, test)* 100);
				logger.info("Score for evaluation of {} with split #{}/{}: {}", c, i + 1, folds, score);
				stats.addValue(score);
			}
		} catch (Throwable e) {
			logger.warn("Exception or Error thrown by classifier {}. Returning a NULL score", c);
			return null;
		}

		int score = (int)Math.round(stats.getMean());
		logger.info("Obtained score of {} for classifier {}.", score, c);
		return score;
	}

}
