package jaicore.ml.core.evaluation.measure.multilabel;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.IObjectEvaluator;
import jaicore.ml.WekaUtil;
import meka.classifiers.multilabel.Evaluation;
import meka.classifiers.multilabel.MultiLabelClassifier;
import meka.core.Result;
import weka.core.Instances;

public class MonteCarloCrossValidationEvaluator implements IObjectEvaluator<MultiLabelClassifier, Double> {

	static final Logger logger = LoggerFactory.getLogger(MonteCarloCrossValidationEvaluator.class);
	private final ADecomposableMultilabelMeasure measure;
	private final Instances data;
	private boolean canceled = false;
	private final int repeats;
	private final float trainingPortion;
	private final int seed;
	private final Random random;

	public MonteCarloCrossValidationEvaluator(ADecomposableMultilabelMeasure basicEvaluator, int repeats, Instances data, float trainingPortion, int seed) {
		super();
		this.measure = basicEvaluator;
		this.repeats = repeats;
		if (data == null)
			throw new IllegalArgumentException("NULL data given to MCCV!");
		this.data = data;
		this.trainingPortion = trainingPortion;
		this.seed = seed;
		this.random = new Random(seed);
	}

	public void cancel() {
		logger.info("Received cancel");
		this.canceled = true;
	}
	
	@Override
	public Double evaluate(MultiLabelClassifier c) throws Exception {
		
		if (c == null)
			throw new IllegalArgumentException("Cannot compute score for null classifier!");

		/* perform random stratified split */
		DescriptiveStatistics stats = new DescriptiveStatistics();
		logger.info("Starting evaluation of {}", c);
		for (int i = 0; i < repeats && !canceled; i++) {
			logger.info("Evaluating {} with split #{}/{}", c, i + 1, repeats);
			List<Instances> split = WekaUtil.realizeSplit(data, WekaUtil.getArbitrarySplit(data, random, trainingPortion));
			Instances train = split.get(0);
			Instances test = split.get(1);
			logger.info("Split data set with {} items into {}/{}", data.size(), train.size(), test.size());
			MultiLabelClassifier cCopy = (MultiLabelClassifier)WekaUtil.cloneClassifier(c);
			cCopy.buildClassifier(train);
			Result result = Evaluation.testClassifier(cCopy, test);
			List<int[]> actuals = Arrays.asList(result.allTrueValues());
			List<int[]> predictions = Arrays.asList(result.allPredictions(.5));
			double score = measure.calculateAvgMeasure(actuals, predictions);
			logger.info("Score for evaluation of {} with split #{}/{}: {}", c, i + 1, repeats, score);
			stats.addValue(score);
		}
		Double score = stats.getMean();
		logger.info("Obtained score of {} for classifier {}.", score, c);
		return score;
	}

	public int getSeed() {
		return seed;
	}
}