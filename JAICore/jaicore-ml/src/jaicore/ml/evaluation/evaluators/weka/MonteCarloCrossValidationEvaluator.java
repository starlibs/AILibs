package jaicore.ml.evaluation.evaluators.weka;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.ml.WekaUtil;
import jaicore.ml.core.evaluation.measure.IMeasure;
import jaicore.ml.evaluation.IInstancesClassifier;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

public class MonteCarloCrossValidationEvaluator implements IClassifierEvaluator {

	static final Logger logger = LoggerFactory.getLogger(MonteCarloCrossValidationEvaluator.class);
	private final IMeasure<Double,Double> basicEvaluator;
	private final Instances data;
	private boolean canceled = false;
	private final int repeats;
	private final float trainingPortion;
	private final int seed;
	private final Random rand;
	private final DescriptiveStatistics stats = new DescriptiveStatistics();

	public MonteCarloCrossValidationEvaluator(final IMeasure<Double,Double> basicEvaluator, final int repeats, final Instances data, final float trainingPortion, final int seed) {
		super();
		this.basicEvaluator = basicEvaluator;
		this.repeats = repeats;
		if (data == null) {
			throw new IllegalArgumentException("NULL data given to MCCV!");
		}
		this.data = data;
		this.trainingPortion = trainingPortion;
		this.seed = seed;
		this.rand = new Random(seed);
	}

	public void cancel() {
		logger.info("Received cancel");
		this.canceled = true;
	}

	@Override
	public Double evaluate(final Classifier pl) throws Exception {
		if (pl == null) {
			throw new IllegalArgumentException("Cannot compute score for null pipeline!");
		}

		/* perform random stratified split */
		logger.info("Starting evaluation of {}", pl);
		for (int i = 0; i < this.repeats && !this.canceled && !Thread.currentThread().isInterrupted(); i++) {
			logger.debug("Obtaining predictions of {} for split #{}/{}", pl, i + 1, this.repeats);
			List<Instances> split = WekaUtil.getStratifiedSplit(data, rand, trainingPortion);
			List<Double> actual = WekaUtil.getClassesAsList(split.get(1));
			List<Double> predicted = new ArrayList<>();
			pl.buildClassifier(split.get(0));
			Instances validationData = split.get(1);
			if (pl instanceof IInstancesClassifier) {
				for (double prediction : ((IInstancesClassifier) pl).classifyInstances(validationData)) {
					predicted.add(prediction);
				}
			}
			else {
				for (Instance inst : validationData) {
					predicted.add(pl.classifyInstance(inst));
				}
			}
			double score = this.basicEvaluator.calculateAvgMeasure(actual, predicted);
			logger.info("Score for evaluation of {} with split #{}/{}: {}", pl, i + 1, this.repeats, score);
			stats.addValue(score);
		}
		if (Thread.currentThread().isInterrupted())
			throw new InterruptedException("MCCV has been interrupted");

		Double score = stats.getMean();
		logger.info("Obtained score of {} for classifier {}.", score, pl);
		return score;
	}

	public IMeasure<Double,Double> getMetric() {
		return this.basicEvaluator;
	}

	public DescriptiveStatistics getStats() {
		return stats;
	}

	public int getSeed() {
		return seed;
	}
}
