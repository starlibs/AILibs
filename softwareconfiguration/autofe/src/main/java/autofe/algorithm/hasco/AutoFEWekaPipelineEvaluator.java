package autofe.algorithm.hasco;

import org.api4.java.common.attributedobjects.IObjectEvaluator;
import org.api4.java.common.attributedobjects.ObjectEvaluationFailedException;

import ai.libs.jaicore.ml.classification.loss.instance.ZeroOneLoss;
import ai.libs.jaicore.ml.core.evaluation.evaluator.splitevaluation.SimpleSLCSplitBasedClassifierEvaluator;
import ai.libs.jaicore.ml.core.evaluation.splitsetgenerator.MonteCarloCrossValidationSplitSetGenerator;
import autofe.util.DataSet;
import weka.core.Instances;

public class AutoFEWekaPipelineEvaluator implements IObjectEvaluator<AutoFEWekaPipeline, Double> {

	private final DataSet data;
	private final int seed;
	private final float trainingPortion;

	public AutoFEWekaPipelineEvaluator(final DataSet data, final int seed, final float trainingPortion) {
		this.data = data;
		this.seed = seed;
		this.trainingPortion = trainingPortion;
	}

	@Override
	public Double evaluate(final AutoFEWekaPipeline object) throws ObjectEvaluationFailedException {
		try {
			Instances wekaData = object.transformData(this.data);
			MonteCarloCrossValidationSplitSetGenerator evaluator = new MonteCarloCrossValidationSplitSetGenerator(new SimpleSLCSplitBasedClassifierEvaluator(new ZeroOneLoss()), 5, wekaData, this.trainingPortion, this.seed);
			return evaluator.evaluate(object);
		} catch (Exception e) {
			throw new ObjectEvaluationFailedException("Could not evaluate pipeline", e);
		}
	}

}
