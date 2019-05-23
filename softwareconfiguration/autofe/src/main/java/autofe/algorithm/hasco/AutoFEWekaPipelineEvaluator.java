package autofe.algorithm.hasco;

import java.util.Random;

import autofe.util.DataSet;
import jaicore.basic.IObjectEvaluator;
import jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;
import jaicore.ml.evaluation.MonteCarloCrossValidationEvaluator;
import jaicore.ml.evaluation.MulticlassEvaluator;
import weka.core.Instances;

public class AutoFEWekaPipelineEvaluator implements IObjectEvaluator<AutoFEWekaPipeline, Double> {

	private final DataSet data;
	private final Random rand;
	private final float trainingPortion;

	public AutoFEWekaPipelineEvaluator(final DataSet data, final Random rand, final float trainingPortion) {
		this.data = data;
		this.rand = rand;
		this.trainingPortion = trainingPortion;
	}

	@Override
	public Double evaluate(final AutoFEWekaPipeline object) throws ObjectEvaluationFailedException {
		try {
			Instances wekaData = object.transformData(data);
			MonteCarloCrossValidationEvaluator evaluator = new MonteCarloCrossValidationEvaluator(new MulticlassEvaluator(rand), 5, wekaData, trainingPortion);
			return evaluator.evaluate(object);
		} catch (Exception e) {
			throw new ObjectEvaluationFailedException("Could not evaluate pipeline", e);
		}
	}

}
