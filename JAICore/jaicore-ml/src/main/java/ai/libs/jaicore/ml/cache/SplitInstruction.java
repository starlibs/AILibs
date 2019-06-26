package ai.libs.jaicore.ml.cache;

import com.fasterxml.jackson.annotation.JsonProperty;

import ai.libs.jaicore.ml.WekaUtil;

/**
 * Instruction to track a split for a {@link ReproducibleInstances} object. Performns a stratified split from {@link WekaUtil} based on the given ratios and seed. The index gives the split to be used by the {@link ReproducibleInstances}.
 *
 * @author fmohr
 *
 */
public abstract class SplitInstruction extends FoldBasedSubsetInstruction {

	@JsonProperty
	private final double portionOfFirstFold;

	public SplitInstruction(final String name, @JsonProperty("portionOfFirstFold") final double portionOfFirstFold) {
		super(name);
		this.portionOfFirstFold = portionOfFirstFold;
	}

	public double getPortionOfFirstFold() {
		return this.portionOfFirstFold;
	}
}