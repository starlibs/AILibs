package jaicore.ml.cache;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Instruction class that can be converted into json. Used by {@link ReproducibleInstances}. The instructions are used to store information about the dataset origin and the splits done.
 * Supported are {@link LoadDataSetInstruction} and {@link FoldBasedSubsetInstruction} at the moment. <br>
 *
 * An instruction is identified by a command name, that specifies the type of instruction and a list if input parameters.
 *
 * @author jnowack
 *
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "command", visible = true)
@JsonSubTypes({
	@Type(value = LoadDataSetInstruction.class, name = "loadDataset"),
	@Type(value = FoldBasedSubsetInstruction.class, name = "split")
})
public abstract class Instruction {

	public Instruction() {
		this.inputs = new HashMap<>();
	}

	@JsonProperty
	protected String command = "noCommand";

	@JsonProperty
	protected Map<String, String> inputs = new HashMap<>();

	/**Sets command name that specifies the type of instruction represented by the object. Every instructions has a unique command name.
	 * @return the name of the command used to identify it.
	 */
	public String getCommand() {
		return this.command;
	}

	/**
	 * Gets command name that specifies the type of instruction represented by the object. Every instructions needs a unique command name.
	 * @param command name of the command
	 */
	public void setCommand(final String command) {
		this.command = command;
	}

	/** Inputs are parameters of the instruction. These inputs are used to reproduce the effects of this instruction.
	 * @return the list of input arguments for the instruction
	 */
	public Map<String, String> getInputs() {
		return this.inputs;
	}

	/**Sets the input parameters that will be used to reproduce the effects done by this instruction.
	 * @param inputs map of inputs as pairs of to Strings.
	 */
	public void setInputs(final Map<String, String> inputs) {
		this.inputs = inputs;
	}


}
