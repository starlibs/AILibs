package autofe.algorithm.hasco;

import java.util.Random;

import autofe.util.DataSet;
import jaicore.basic.IObjectEvaluator;
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
	public Double evaluate(final AutoFEWekaPipeline object) throws Exception {
		Instances wekaData = object.transformData(this.data);
		MonteCarloCrossValidationEvaluator evaluator = new MonteCarloCrossValidationEvaluator(new MulticlassEvaluator(this.rand), 5, wekaData, this.trainingPortion);
		return evaluator.evaluate(object);
	}

}
