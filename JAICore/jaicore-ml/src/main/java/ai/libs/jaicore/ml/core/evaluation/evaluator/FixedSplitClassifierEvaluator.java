package ai.libs.jaicore.ml.core.evaluation.evaluator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.core.Instances;

public class FixedSplitClassifierEvaluator implements IClassifierEvaluator {

	private final static Logger logger = LoggerFactory.getLogger(FixedSplitClassifierEvaluator.class);
	private final Instances train, validate;

	public FixedSplitClassifierEvaluator(final Instances train, final Instances validate) {
		super();
		this.train = train;
		this.validate = validate;
	}

	@Override
	public Double evaluate(final Classifier c) throws InterruptedException {
		try {
			c.buildClassifier(this.train);
			Evaluation eval = new Evaluation(this.train);
			eval.evaluateModel(c, this.validate);
			return eval.errorRate();
		} catch (InterruptedException e) {
			throw e;
		}
		catch (Exception e) {
			logger.warn("Evaluation of classifier failed due Exception {} with message {}. Returning null.",
					e.getClass().getName(), e.getMessage());
			return null;
		}
	}

	public Instances getTrain() {
		return this.train;
	}

	public Instances getValidate() {
		return this.validate;
	}
}
