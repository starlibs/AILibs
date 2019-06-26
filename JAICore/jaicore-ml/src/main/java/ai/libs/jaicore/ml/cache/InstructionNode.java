package ai.libs.jaicore.ml.cache;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ai.libs.jaicore.basic.sets.Pair;

public class InstructionNode {

	private static final long serialVersionUID = -347708788723924559L;
	private String name;
	private Instruction instruction;
	public List<Pair<String, Integer>> inputs;

	@JsonIgnore
	private InstructionGraph graph;

	public InstructionNode() {

	}

	protected InstructionNode(final InstructionGraph graph, @JsonProperty final String name, @JsonProperty final Instruction instruction) {
		this (graph, name, instruction, null);
	}

	protected InstructionNode(final InstructionGraph graph, @JsonProperty final String name, @JsonProperty final Instruction instruction, @JsonProperty final List<Pair<String, Integer>> inputs) {
		super();
		this.graph = graph;
		this.name = name;
		this.instruction = instruction;
		this.inputs = inputs;
	}

	public InstructionNode getNodeThatComputesInput(final int i) {
		return this.graph.getNodeByName(this.inputs.get(i).getX());
	}

	public String getName() {
		return this.name;
	}

	public Instruction getInstruction() {
		return this.instruction;
	}

	public List<Pair<String, Integer>> getInputs() {
		return this.inputs;
	}

	public InstructionGraph getGraph() {
		return this.graph;
	}

	//	public D getOutputInstances(final int outputUnit) throws InstructionFailedException, InterruptedException {
	//
	//		/* first get inputs recursively */
	//		List<D> inputs = new ArrayList<>();
	//		for (int src : this.inputConnections.get(outputUnit)) {
	//			inputs.add(this.preceedingLayer.getOutputInstances(src));
	//		}
	//
	//		/* now compute the data of this unit */
	//		return this.get(outputUnit).getOutputInstances(inputs);
	//	}
}
