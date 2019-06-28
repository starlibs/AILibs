package ai.libs.jaicore.ml.cache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.clearspring.analytics.util.Lists;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.ml.core.dataset.IDataset;

public class InstructionGraph extends ArrayList<InstructionNode> {

	private static final long serialVersionUID = -2808542161976307180L;

	@JsonIgnore
	private final Map<String, InstructionNode> nodeMap = new HashMap<>();

	public InstructionGraph() {
		super();
	}

	public static InstructionGraph fromJson(final String jsonRepresentation) throws JsonParseException, JsonMappingException, IOException {
		return new ObjectMapper().readValue(jsonRepresentation, InstructionGraph.class);
	}

	public InstructionGraph(final InstructionGraph toClone) {
		for (InstructionNode node : toClone) {
			List<Pair<String, Integer>> inputs = new ArrayList<>();
			for (Pair<String, Integer> pair : node.getInputs()) {
				inputs.add(new Pair<>(pair.getX(), pair.getY()));
			}
			this.addNode(node.getName(), node.getInstruction().clone(), inputs);
		}
	}

	public InstructionNode addNode(final String name, final Instruction instruction) {
		return this.addNode(name, instruction, Lists.newArrayList());
	}

	public InstructionNode addNode(final String name, final Instruction instruction, final List<Pair<String, Integer>> inputs) {
		InstructionNode node = new InstructionNode(this, name, instruction, inputs);
		this.nodeMap.put(name, node);
		this.add(node);
		return node;
	}

	public InstructionNode getNodeByName(final String name) {
		return this.nodeMap.get(name);
	}

	private void synchronizeNodeMap() {

		/* delete obsolete nodes from map*/
		SetUtil.difference(this.nodeMap.keySet(), this.stream().map(InstructionNode::getName).collect(Collectors.toList())).forEach(this.nodeMap::remove);

		/* add new nodes */
		for (InstructionNode node : this) {
			this.nodeMap.computeIfAbsent(node.getName(), k -> node);
		}
	}

	/**
	 * Recursively computes the data for a node in the graph
	 *
	 * @param unit
	 * @return
	 * @throws InstructionFailedException
	 * @throws InterruptedException
	 */
	public IDataset getDataForUnit(final Pair<String, Integer> unit) throws InstructionFailedException, InterruptedException {
		this.synchronizeNodeMap();
		InstructionNode node = this.nodeMap.get(unit.getX());
		List<IDataset> inputs = new ArrayList<>();
		if (node.getInputs() != null) {
			for (Pair<String, Integer> inputProvider : node.getInputs()) {
				IDataset input = this.getDataForUnit(inputProvider);
				inputs.add(input);
			}
		}
		return node.getInstruction().getOutputInstances(inputs).get(unit.getY());
	}
}
