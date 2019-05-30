package jaicore.ml.evaluation.evaluators.weka;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.ml.WekaUtil;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.core.Instances;

public class SingleRandomSplitClassifierEvaluator implements IClassifierEvaluator {

	private final static Logger logger = LoggerFactory.getLogger(SingleRandomSplitClassifierEvaluator.class);
	private final Instances data;
	private int seed;
	private float trainingPortion = 0.7f;
	
	public SingleRandomSplitClassifierEvaluator(Instances data) {
		super();
		this.data = data;
	}

	@Override
	public Double evaluate(Classifier c) throws InterruptedException {
		List<Instances> split = WekaUtil.getStratifiedSplit(data, seed >= 0 ? seed : System.currentTimeMillis(), trainingPortion);
		try {
			c.buildClassifier(split.get(0));
			Evaluation eval = new Evaluation(split.get(0));
			eval.evaluateModel(c, split.get(1));
			Double score = eval.pctIncorrect();
			return score;
		} catch (Throwable e) {
			logger.warn("Evaluation of classifier failed due Exception {} with message {}. Returning null.",
					e.getClass().getName(), e.getMessage());
			return null;
		}
	}

	public int getSeed() {
		return seed;
	}

	public void setSeed(int seed) {
		this.seed = seed;
	}

	public float getTrainingPortion() {
		return trainingPortion;
	}

	public void setTrainingPortion(float trainingPortion) {
		this.trainingPortion = trainingPortion;
	}
}
