package jaicore.ml.cache;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Instruction class that can be converted into json. 
 * 
 * @author jnowack
 *
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "command", visible = true)
@JsonSubTypes({ 
	  @Type(value = ComputeLossInstruction.class, name = "computeLoss"), 
	  @Type(value = LoadDataSetInstruction.class, name = "loadDataset"),
	  @Type(value = SplitInstruction.class, name = "split")
	})
public abstract class Instruction {

	public Instruction() {
		inputs = new HashMap<String, String>();
	}
	
	@JsonProperty
	String command = "noCommand";
	
	@JsonProperty
	Map<String, String> inputs = new HashMap<>();
	
	
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
