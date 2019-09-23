package ai.libs.jaicore.ml.core.dataset.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.api4.java.ai.ml.core.dataset.IDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;
import org.api4.java.ai.ml.core.exception.DatasetTraceInstructionFailedException;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;

import com.fasterxml.jackson.annotation.JsonProperty;

import ai.libs.jaicore.ml.core.filter.sampling.inmemory.stratified.sampling.AttributeBasedStratiAmountSelectorAndAssigner;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.stratified.sampling.StratifiedSampling;

/**
 * Computes a two-fold split
 *
 * @author fmohr
 */
public class StratifiedSplitSubsetInstruction extends SplitInstruction {

	/**
	 *
	 */
	private static final long serialVersionUID = 8983732598462588900L;

	private static final String NAME = "Stratified";

	@JsonProperty
	private final long seed;

	public StratifiedSplitSubsetInstruction(@JsonProperty("seed") final long seed, @JsonProperty("ratios") final double ratios) {
		super(NAME, ratios);
		this.seed = seed;
	}

	@Override
	public List<IDataset<?>> getOutputInstances(final List<IDataset<?>> inputs) throws DatasetTraceInstructionFailedException, InterruptedException {

		/* there must be exactly one input */
		if (inputs.size() != 1) {
			throw new IllegalArgumentException("StratifiedSplit needs exactly one input.");
		}

		/* compute sub-sample, which constitutes the first fold of a two-fold split (the second is the complement) */
		ILabeledDataset<ILabeledInstance> input = (ILabeledDataset<ILabeledInstance>) inputs.get(0);
		AttributeBasedStratiAmountSelectorAndAssigner<ILabeledInstance, ILabeledDataset<ILabeledInstance>> stratiBuilder = new AttributeBasedStratiAmountSelectorAndAssigner<>();
		StratifiedSampling<ILabeledInstance, ILabeledDataset<ILabeledInstance>> sampler = new StratifiedSampling<>(stratiBuilder, stratiBuilder, new Random(this.seed), input);
		sampler.setSampleSize((int) Math.ceil(input.size() * this.getPortionOfFirstFold()));
		List<IDataset<?>> output = new ArrayList<>(2);
		try {
			IDataset<?> subsample = sampler.call();
			output.add(subsample);
			output.add(sampler.getComplement());
			return output;
		} catch (AlgorithmExecutionCanceledException | AlgorithmException | DatasetCreationException | AlgorithmTimeoutedException e) {
			throw new DatasetTraceInstructionFailedException(e);
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
