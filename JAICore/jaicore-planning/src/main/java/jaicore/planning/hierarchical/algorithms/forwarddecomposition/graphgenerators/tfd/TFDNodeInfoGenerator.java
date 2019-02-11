package jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd;

import java.util.List;
import java.util.stream.Collectors;

import jaicore.graphvisualizer.plugin.nodeinfo.NodeInfoGenerator;
import jaicore.logic.fol.structure.Literal;
import jaicore.planning.core.Action;

public class TFDNodeInfoGenerator implements NodeInfoGenerator<List<TFDNode>> {

	@Override
	public String generateInfoForNode(List<TFDNode> path) {
		
		TFDNode head = path.get(path.size() - 1);
		
		StringBuilder sb = new StringBuilder();
		if (head.getAppliedMethodInstance() != null || head.getAppliedAction() != null) {
			sb.append("<h2>Applied Instance</h2>");
			sb.append(head.getAppliedMethodInstance() != null ? head.getAppliedMethodInstance().getEncoding() : head.getAppliedAction().getEncoding());
		}
		sb.append("<h2>Remaining Tasks</h2>");
		sb.append("<ul>");
		for (Literal l : head.getRemainingTasks()) {
			sb.append("<li>");
			sb.append(l);
			sb.append("</li>");
		}
		sb.append("</ul>");
		sb.append("<h2>Current State</h2>");
		List<String> monomStrings = head.getProblem().getState().stream().sorted((l1, l2) -> l1.getPropertyName().compareTo(l2.getPropertyName())).map(l -> l.toString()).collect(Collectors.toList());
		sb.append("<ul>");
		for (String literal : monomStrings) {
			sb.append("<li>");
			sb.append(literal);
			sb.append("</li>");
		}
		sb.append("</ul>");
		sb.append("<h2>Current Plan</h2>");
		sb.append("<ul>");
		for (Action a : path.stream().map(np -> np.getAppliedAction()).filter(a -> a != null)
				.collect(Collectors.toList())) {
			sb.append("<li>");
			sb.append(a.getEncoding());
			sb.append("</li>");
		}
		sb.append("</ul>");
		return sb.toString();
	}

}
