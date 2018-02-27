package de.upb.crc901.mlplan.classifiers;

import java.util.List;
import java.util.Random;

import de.upb.crc901.mlplan.search.evaluators.multilabel.MultilabelEvaluator;
import jaicore.concurrent.TimeoutTimer;
import jaicore.concurrent.TimeoutTimer.TimeoutSubmitter;
import jaicore.ml.WekaUtil;
import meka.classifiers.multilabel.BR;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.core.Instances;

public class BRBase extends BR {

	private final int seed;
	private final int timeout;
	private static final String[] classifiers = { "weka.classifiers.trees.RandomTree", "weka.classifiers.functions.SMO" };

	private Classifier selectedClassifier;
	private double bestPerformance = Double.MAX_VALUE;
	private MultilabelEvaluator evaluator;

	public BRBase(int seed, int timeout, MultilabelEvaluator evaluator) {
		super();
		this.seed = seed;
		this.timeout = timeout;
		this.evaluator = evaluator;
	}

	@Override
	public void buildClassifier(Instances data) throws Exception {

		List<Instances> split = WekaUtil.realizeSplit(data, WekaUtil.getArbitrarySplit(data, new Random(seed), .7f));
		int taskId = -1;
		TimeoutSubmitter submitter = null;
		if (timeout > 0) {
			submitter = TimeoutTimer.getInstance().getSubmitter();
			taskId = submitter.interruptMeAfterMS(timeout);
		}

		try {
			for (String classifier : classifiers) {

				System.out.println("Testing " + classifier);

				/* build br */
				Classifier c = AbstractClassifier.forName(classifier, null);
				this.setClassifier(c);
				super.buildClassifier(split.get(0));

				/* evaluated candidate */
				double loss = evaluator.loss(this, split.get(1));
				if (loss < bestPerformance) {
					System.out.println("Choosing " + classifier);
					selectedClassifier = c;
					bestPerformance = loss;
				}
			}
		} catch (InterruptedException e) {
			System.out.println("Interrupted!");
		}
		finally {

			if (taskId >= 0)
				submitter.cancelTimeout(taskId);
			
			System.out.println("Setting classifier to " + selectedClassifier.getClass().getName());
			if (selectedClassifier != null)
				super.setClassifier(selectedClassifier);
			super.buildClassifier(data);
		}
	}

	public Classifier getSelectedClassifier() {
		return selectedClassifier;
	}
}
