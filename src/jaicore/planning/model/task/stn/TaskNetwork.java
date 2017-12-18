package jaicore.planning.model.task.stn;

import jaicore.basic.StringUtil;
import jaicore.graph.Graph;
import jaicore.logic.fol.structure.Literal;

@SuppressWarnings("serial")
public class TaskNetwork extends Graph<Literal> {

	public TaskNetwork() {
		super();
	}

	public TaskNetwork(Graph<Literal> graph) {
		super(graph);
	}

	public TaskNetwork(String chain) {
		super();
		Literal current = null;
		int id = 1;
		for (String taskDescription : StringUtil.explode(chain, "->")) {
			if (!taskDescription.trim().isEmpty()) {
				Literal task = new Literal("tn" + "_" + id + "-" + taskDescription.trim());
				this.addItem(task);
				if (current != null)
					this.addEdge(current, task);
				current = task;
				id++;
			}
		}
	}
}
