package jaicore.ml.evaluation.evaluators.weka;

import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.inference.TTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.IInformedObjectEvaluatorExtension;
import jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;
import jaicore.ml.WekaUtil;
import weka.classifiers.Classifier;
import weka.core.Instances;

/**
 * A classifier evaluator that can perform a (monte-carlo)cross-validation on
 * the given dataset. Thereby, it uses the
 * {@link AbstractEvaluatorMeasureBridge} to evaluate the classifier on a random
 * split of the dataset. 
 * This probabilistic version can be used to speed up the process by early termination based on
 * a threshold value that has to be beaten by the evaluation. If it is unlikely after the first
 * repeats that this is possible the unfinished, not as precise result will be returned.
 * 
 * @author fmohr, jnowack
 *
 */
public class ProbabilisticMonteCarloCrossValidationEvaluator implements IClassifierEvaluator, IInformedObjectEvaluatorExtension<Double>, ILoggingCustomizable {

	private Logger logger = LoggerFactory.getLogger(ProbabilisticMonteCarloCrossValidationEvaluator.class);
	private boolean canceled = false;
	private final int repeats;
	private final Instances data;
	private final double trainingPortion;
	private final long seed;
	
	private double bestScore = 1.0;
	
	/* Can either compute the loss or cache it */
	private final AbstractEvaluatorMeasureBridge<Double, Double> bridge;
	
	@Override
	public void updateBestScore(Double bestScore) {
		this.bestScore = bestScore;
	}
	
	public ProbabilisticMonteCarloCrossValidationEvaluator(AbstractEvaluatorMeasureBridge<Double, Double> bridge, final int repeats, final double bestscore, final Instances data, final double trainingPortion, final long seed) {
		super();
		this.repeats = repeats;
		this.bridge = bridge;
		this.data = data;
		this.trainingPortion = trainingPortion;
		this.seed = seed;
		this.bestScore = bestscore;
	}

	public void cancel() {
		logger.info("Received cancel");
		this.canceled = true;
	}

	@Override
	public Double evaluate(final Classifier pl) throws ObjectEvaluationFailedException, InterruptedException {
		return evaluate(pl, new DescriptiveStatistics());
	}
	
	public Double evaluate(final Classifier pl, DescriptiveStatistics stats) throws ObjectEvaluationFailedException, InterruptedException {
		if (pl == null) {
			throw new IllegalArgumentException("Cannot compute score for null pipeline!");
		}
		
		/* perform random stratified split */
		logger.info("Starting evaluation of {}", pl);
		for (int i = 0; i < this.repeats && !this.canceled && !Thread.currentThread().isInterrupted(); i++) {
			logger.debug("Obtaining predictions of {} for split #{}/{}", pl, i + 1, this.repeats);
			List<Instances> split = WekaUtil.getStratifiedSplit(data, seed + i, trainingPortion);
			try {
				double score = bridge.evaluateSplit(pl, split.get(0), split.get(1));
				logger.info("Score for evaluation of {} with split #{}/{}: {}", pl, i + 1, this.repeats, score);
				stats.addValue(score);
				
				/* t-test */
				if(stats.getMean() > bestScore && stats.getN() >= 2) {
					TTest test = new TTest();
					if(test.tTest(bestScore, stats.getValues(), 0.02)) {
						Double result = stats.getMean();
						logger.info("Obtained score of {} for classifier {}. {}-MCCV was not completed because it would have been to unliky to beat best score.", result, pl, this.repeats);
						return result;
					}
				}
			}

			catch (InterruptedException e) {
				throw e;
			}
			catch (Exception e) {
				throw new ObjectEvaluationFailedException(e, "Could not evaluate classifier!");

			}
		}
		if (Thread.currentThread().isInterrupted())
			throw new InterruptedException("MCCV has been interrupted");
		Double score = stats.getMean();
		logger.info("Obtained score of {} for classifier {}.", score, pl);
		return score;
	}
	
	public AbstractEvaluatorMeasureBridge<Double, Double> getBridge() {
		return bridge;
	}

	@Override
	public String getLoggerName() {
		return logger.getName();
	}

	@Override
	public void setLoggerName(String name) {
		this.logger.info("Switching logger of {} from {} to {}", this, logger.getName(), name);
		this.logger = LoggerFactory.getLogger(name);
		this.logger.info("Switched logger of {} to {}", this, name);
	}
}
