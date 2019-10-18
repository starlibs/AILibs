package ai.libs.jaicore.ml.core.filter.sampling.inmemory.stratified.sampling;

import java.util.Arrays;
import java.util.Random;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;

public class LabelBasedStratifiedSampling<I extends ILabeledInstance, D extends ILabeledDataset<I>> extends StratifiedSampling<I, D> {

	public LabelBasedStratifiedSampling(final Random random, final D input) {
		this(new AttributeBasedStratiAmountSelectorAndAssigner<>(Arrays.asList(input.getNumAttributes())), random, input);
	}

	private LabelBasedStratifiedSampling(final AttributeBasedStratiAmountSelectorAndAssigner<I, D> assigner, final Random random, final D input) {
		super(assigner, assigner, random, input);
	}
}
