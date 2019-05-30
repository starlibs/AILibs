package jaicore.planning.hierarchical.problems.stn;

import java.util.List;

import jaicore.basic.StringUtil;
import jaicore.graph.Graph;
import jaicore.logic.fol.structure.Literal;

@SuppressWarnings("serial")
public class TaskNetwork extends Graph<Literal> {

	public TaskNetwork() {
		super();
	}
	
	public TaskNetwork(List<Literal> chain) {
		int n = chain.size();
		Literal prev = null;
		for (int i = 0; i < n; i++) {
			Literal cur = chain.get(i);
			this.addItem(cur);
			if (prev != null)
				this.addEdge(prev, cur);
			prev = cur;
		}
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
