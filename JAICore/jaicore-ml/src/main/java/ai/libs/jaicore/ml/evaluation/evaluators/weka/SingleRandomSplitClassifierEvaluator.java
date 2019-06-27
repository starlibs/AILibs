package ai.libs.jaicore.ml.evaluation.evaluators.weka;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;
import ai.libs.jaicore.ml.WekaUtil;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.core.Instances;

public class SingleRandomSplitClassifierEvaluator implements IClassifierEvaluator {

	private final static Logger logger = LoggerFactory.getLogger(SingleRandomSplitClassifierEvaluator.class);
	private final Instances data;
	private int seed;
	private float trainingPortion = 0.7f;

	public SingleRandomSplitClassifierEvaluator(final Instances data) {
		super();
		this.data = data;
	}

	@Override
	public Double evaluate(final Classifier c) throws InterruptedException, ObjectEvaluationFailedException {
		List<Instances> split;
		try {
			split = WekaUtil.getStratifiedSplit(this.data, this.seed >= 0 ? this.seed : System.currentTimeMillis(), this.trainingPortion);
			c.buildClassifier(split.get(0));
			Evaluation eval = new Evaluation(split.get(0));
			eval.evaluateModel(c, split.get(1));
			return eval.pctIncorrect();
		} catch (InterruptedException e) {
			throw e;
		}catch (Exception e) {
			throw new ObjectEvaluationFailedException("Evaluation failed!", e);
		}

	}

	public int getSeed() {
		return this.seed;
	}

	public void setSeed(final int seed) {
		this.seed = seed;
	}

	public float getTrainingPortion() {
		return this.trainingPortion;
	}

	public void setTrainingPortion(final float trainingPortion) {
		this.trainingPortion = trainingPortion;
	}
}
