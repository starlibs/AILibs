package autofe.algorithm.hasco;

import ai.libs.jaicore.basic.IObjectEvaluator;
import ai.libs.jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;
import ai.libs.jaicore.ml.core.evaluation.measure.singlelabel.ZeroOneLoss;
import ai.libs.jaicore.ml.evaluation.evaluators.weka.MonteCarloCrossValidationEvaluator;
import ai.libs.jaicore.ml.evaluation.evaluators.weka.splitevaluation.SimpleSLCSplitBasedClassifierEvaluator;
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
			Instances wekaData = object.transformData(data);
			MonteCarloCrossValidationEvaluator evaluator = new MonteCarloCrossValidationEvaluator(new SimpleSLCSplitBasedClassifierEvaluator(new ZeroOneLoss()), 5, wekaData, trainingPortion, seed);
			return evaluator.evaluate(object);
		} catch (Exception e) {
			throw new ObjectEvaluationFailedException("Could not evaluate pipeline", e);
		}
	}

}
