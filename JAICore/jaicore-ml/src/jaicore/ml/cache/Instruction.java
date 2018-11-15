package jaicore.ml.cache;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Instruction class that can be converted into json. 
 * 
 * @author jnowack
 *
 */
public abstract class Instruction {

	public Instruction() {
	}
	
	@JsonProperty
	String command;
	
	@JsonProperty
	Map<String, String> inputs;
	
	
	public String getCommand() {
		return command;
	}
	
	public void setCommand(String command) {
		this.command = command;
	}
	
	public Map<String, String> getInputs() {
		return inputs;
	}
	
	public void setInputs(Map<String, String> inputs) {
		this.inputs = inputs;
	}
	
	
}
