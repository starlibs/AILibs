package ai.libs.jaicore.ml.core.filter.sampling.inmemory.stratified.sampling;

import java.util.Arrays;
import java.util.Random;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;

public class LabelBasedStratifiedSampling<D extends ILabeledDataset<?>> extends StratifiedSampling<D> {

	public LabelBasedStratifiedSampling(final Random random, final D input) {
		this(new AttributeBasedStratifier(Arrays.asList(input.getNumAttributes())), random, input);
	}

	private LabelBasedStratifiedSampling(final AttributeBasedStratifier assigner, final Random random, final D input) {
		super(assigner, random, input);
	}
}
