package jaicore.ml.evaluation.evaluators.weka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.core.Instances;

public class FixedSplitClassifierEvaluator implements IClassifierEvaluator {

	private final static Logger logger = LoggerFactory.getLogger(FixedSplitClassifierEvaluator.class);
	private final Instances train, validate;
	
	public FixedSplitClassifierEvaluator(Instances train, Instances validate) {
		super();
		this.train = train;
		this.validate = validate;
	}

	@Override
	public Double evaluate(Classifier c) throws InterruptedException {
		try {
			c.buildClassifier(train);
			Evaluation eval = new Evaluation(train);
			eval.evaluateModel(c, validate);
			Double score = eval.pctIncorrect();
			return score;
		} catch (Throwable e) {
			logger.warn("Evaluation of classifier failed due Exception {} with message {}. Returning null.",
					e.getClass().getName(), e.getMessage());
			return null;
		}
	}

	public Instances getTrain() {
		return train;
	}

	public Instances getValidate() {
		return validate;
	}
}
