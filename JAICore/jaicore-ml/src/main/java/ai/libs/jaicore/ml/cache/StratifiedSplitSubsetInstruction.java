package ai.libs.jaicore.ml.cache;

import java.util.List;
import java.util.Random;

import ai.libs.jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import ai.libs.jaicore.basic.algorithm.exceptions.AlgorithmException;
import ai.libs.jaicore.ml.core.dataset.DatasetCreationException;
import ai.libs.jaicore.ml.core.dataset.ILabeledAttributeArrayInstance;
import ai.libs.jaicore.ml.core.dataset.IOrderedLabeledAttributeArrayDataset;
import ai.libs.jaicore.ml.core.dataset.sampling.inmemory.stratified.sampling.AttributeBasedStratiAmountSelectorAndAssigner;
import ai.libs.jaicore.ml.core.dataset.sampling.inmemory.stratified.sampling.StratifiedSampling;

public class StratifiedSplitSubsetInstruction<I extends ILabeledAttributeArrayInstance<L>, L> extends FoldBasedSubsetInstruction<I, IOrderedLabeledAttributeArrayDataset<I,L>> {

	private static final String NAME = "MCCV";
	private final int seed;
	private final double[] ratios;

	public StratifiedSplitSubsetInstruction(final int seed, final double[] ratios, final int[] outIndices) {
		super(NAME, outIndices);
		this.seed = seed;
		this.ratios = ratios;
	}

	@Override
	public IOrderedLabeledAttributeArrayDataset<I,L> getOutputInstances(final List<IOrderedLabeledAttributeArrayDataset<I,L>> inputs) throws InstructionFailedException, InterruptedException {

		/* there must be exactly one input */
		if (inputs.size() != 1) {
			throw new IllegalArgumentException("StratifiedSplit needs exactly one input.");
		}

		/* compute sub-sample, which constitutes the first fold of a two-fold split (the second is the complement) */
		IOrderedLabeledAttributeArrayDataset<I,L> input = inputs.get(0);
		AttributeBasedStratiAmountSelectorAndAssigner<I, IOrderedLabeledAttributeArrayDataset<I, L>> stratiBuilder = new AttributeBasedStratiAmountSelectorAndAssigner<>();
		StratifiedSampling<I, IOrderedLabeledAttributeArrayDataset<I, L>> sampler = new StratifiedSampling<>(stratiBuilder, stratiBuilder, new Random(this.seed), input);
		try {
			IOrderedLabeledAttributeArrayDataset<I,L> subsample = sampler.call();

			if (this.outIndices[0] == 0) {
				return subsample;
			}
			else {
				IOrderedLabeledAttributeArrayDataset<I,L> complement = (IOrderedLabeledAttributeArrayDataset<I,L>)input.createEmpty();
				for (I instance : input) {
					if (!subsample.contains(instance)) {
						complement.add(instance);
					}
				}
				return complement;
			}
		}
		catch (AlgorithmExecutionCanceledException | AlgorithmException | DatasetCreationException e) {
			throw new InstructionFailedException(e);
		}
	}
}
