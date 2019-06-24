package ai.libs.jaicore.ml.cache;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import ai.libs.jaicore.ml.core.dataset.IDataset;

/**
 * Instruction for loss computation.
 *
 * @author jnowack
 *
 */
public class ComputeLossInstruction<I, D extends IDataset<I>> extends Instruction<I, D> {

	/**
	 * @param algorithm
	 *            String representation of the pipeline used
	 * @param trainingPortion
	 *            portion of the data that should be used for training
	 * @param seed
	 *            random seed
	 */
	public ComputeLossInstruction(@JsonProperty("algorithm") final String algorithm,
			@JsonProperty("trainingPortion") final double trainingPortion, @JsonProperty("seed") final long seed) {
		this.command = "computeLoss";
		this.parameters.put("algorithm", algorithm);
		this.parameters.put("trainingPortion", "" + trainingPortion);
		this.parameters.put("seed", "" + seed);
	}

	@Override
	public D getOutputInstances(final List<D> inputs) throws InstructionFailedException, InterruptedException {
		throw new UnsupportedOperationException();
	}
}
