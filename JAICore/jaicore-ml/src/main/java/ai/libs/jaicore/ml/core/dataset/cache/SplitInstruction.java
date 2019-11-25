package ai.libs.jaicore.ml.core.dataset.cache;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Instruction to track a split for a {@link ReproducibleInstances} object. Performns a stratified split from {@link WekaTimeseriesUtil} based on the given ratios and seed. The index gives the split to be used by the {@link ReproducibleInstances}.
 *
 * @author fmohr
 *
 */
public abstract class SplitInstruction extends FoldBasedSubsetInstruction {
	private static final long serialVersionUID = 995533570402743259L;

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