package ai.libs.jaicore.ml.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.fasterxml.jackson.annotation.JsonProperty;

import ai.libs.jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import ai.libs.jaicore.basic.algorithm.exceptions.AlgorithmException;
import ai.libs.jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;
import ai.libs.jaicore.ml.core.dataset.DatasetCreationException;
import ai.libs.jaicore.ml.core.dataset.IDataset;
import ai.libs.jaicore.ml.core.dataset.IOrderedDataset;
import ai.libs.jaicore.ml.core.dataset.sampling.inmemory.stratified.sampling.AttributeBasedStratiAmountSelectorAndAssigner;
import ai.libs.jaicore.ml.core.dataset.sampling.inmemory.stratified.sampling.StratifiedSampling;

/**
 * Computes a two-fold split
 *
 * @author fmohr
 */
public class StratifiedSplitSubsetInstruction extends SplitInstruction {

	private static final String NAME = "Stratified";

	@JsonProperty
	private final long seed;



	public StratifiedSplitSubsetInstruction(@JsonProperty("seed") final long seed, @JsonProperty("ratios") final double ratios) {
		super(NAME, ratios);
		this.seed = seed;
	}

	@Override
	public List<IDataset> getOutputInstances(final List<IDataset> inputs) throws InstructionFailedException, InterruptedException {

		/* there must be exactly one input */
		if (inputs.size() != 1) {
			throw new IllegalArgumentException("StratifiedSplit needs exactly one input.");
		}

		/* compute sub-sample, which constitutes the first fold of a two-fold split (the second is the complement) */
		IOrderedDataset input = (IOrderedDataset)inputs.get(0);
		AttributeBasedStratiAmountSelectorAndAssigner stratiBuilder = new AttributeBasedStratiAmountSelectorAndAssigner();
		StratifiedSampling sampler = new StratifiedSampling(stratiBuilder, stratiBuilder, new Random(this.seed), input);
		sampler.setSampleSize((int)Math.ceil(input.size() * this.getPortionOfFirstFold()));
		List<IDataset> output = new ArrayList<>(2);


		try {
			IOrderedDataset subsample = (IOrderedDataset)sampler.call();
			output.add(subsample);
			output.add(sampler.getComplement());
			return output;
		}
		catch (AlgorithmExecutionCanceledException | AlgorithmException | DatasetCreationException | AlgorithmTimeoutedException e) {
			throw new InstructionFailedException(e);
		}
	}

	public long getSeed() {
		return this.seed;
	}

	@Override
	public Instruction clone() {
		return new StratifiedSplitSubsetInstruction(this.seed, this.getPortionOfFirstFold());
	}
}
