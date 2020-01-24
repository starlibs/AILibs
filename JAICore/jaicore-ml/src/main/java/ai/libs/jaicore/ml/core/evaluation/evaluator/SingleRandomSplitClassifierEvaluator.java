package ai.libs.jaicore.ml.core.evaluation.evaluator;

import java.util.Random;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;

public class SingleRandomSplitClassifierEvaluator extends MonteCarloCrossValidationEvaluator {

	public SingleRandomSplitClassifierEvaluator(final ILabeledDataset<?> data, final double trainingPortion, final Random random) {
		super(data, 1, trainingPortion, random);
	}

}
