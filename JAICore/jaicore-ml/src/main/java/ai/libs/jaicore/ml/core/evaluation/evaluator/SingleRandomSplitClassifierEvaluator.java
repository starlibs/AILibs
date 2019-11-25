package ai.libs.jaicore.ml.core.evaluation.evaluator;

import java.util.Random;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;

public class SingleRandomSplitClassifierEvaluator extends MonteCarloCrossValidationEvaluator {

	public SingleRandomSplitClassifierEvaluator(final ILabeledDataset<ILabeledInstance> data, final int repeats, final double trainingPortion, final Random random) {
		super(data, 1, trainingPortion, random);
	}

}
