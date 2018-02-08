package de.upb.crc901.mlplan.search.evaluators;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.mlplan.core.SolutionEvaluator;
import jaicore.ml.WekaUtil;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.core.Instances;

@SuppressWarnings("serial")
public class SimpleSolutionEvaluator implements SolutionEvaluator {

	private final static Logger logger = LoggerFactory.getLogger(SimpleSolutionEvaluator.class);
	private Thread worker;
	private Instances train, validation;

	@Override
	public Integer getSolutionScore(Classifier c) throws InterruptedException {

		/* check whether we have data available */
		if (train == null)
			throw new IllegalStateException("Cannot compute f-values since training data have not been set!");
		if (validation == null)
			throw new IllegalStateException("Cannot compute f-values since validation data have not been set!");

		/* serialize pipeline */
		FutureTask<Integer> performance = new FutureTask<>(new Callable<Integer>() {
			public Integer call() {
				try {
					c.buildClassifier(train);
					Evaluation eval = new Evaluation(train);
					eval.evaluateModel(c, validation);
					int score = (int) Math.round(eval.pctIncorrect() * 100);
					return score;
				} catch (Throwable e) {
					logger.warn("Evaluation of pipeline failed due Exception {} with message {}. Returning null.", e.getClass().getName(), e.getMessage());
					return null;
				}
			}
		});

		/* run the job and wait for the result of the computation */
		worker = new Thread(performance);
		worker.start();
		try {
			return performance.get();
		} catch (InterruptedException e) {
			logger.info("Received timeout, canceling computation of f. Awaiting termination of worker ...");
			worker.join();
			logger.info("worker thread closed ...");
			throw e;
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		throw new IllegalStateException("Could not compute f-value!");
	}

	@Override
	public void setData(Instances data) {
		if (this.train != null)
			throw new IllegalStateException("Cannot reset the data instances");
		List<Instances> split = WekaUtil.getStratifiedSplit(data, new Random(System.currentTimeMillis()), .8f);
		this.train = split.get(0);
		this.validation = split.get(1);
	}

	@Override
	public void cancel() {
		worker.interrupt();
	}
}
