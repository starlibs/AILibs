package ai.libs.jaicore.ml.cache;

import java.util.ArrayList;

public class InstructionLayer extends ArrayList<Instruction> {

	private static final long serialVersionUID = -347708788723924559L;

	private final InstructionGraph graph;

	public InstructionLayer(final InstructionGraph graph) {
		super();
		this.graph = graph;
	}

	public InstructionGraph getGraph() {
		return this.graph;
	}
}
