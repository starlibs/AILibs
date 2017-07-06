package jaicore.planning.graphgenerators.task.tfd;

import java.util.stream.Collectors;

import jaicore.graphvisualizer.TooltipGenerator;
import jaicore.logic.Literal;
import jaicore.planning.model.core.Action;
import jaicore.search.structure.core.Node;

public class TFDTooltipGenerator implements TooltipGenerator<Node<TFDNode, Integer>> {

	@Override
	public String getTooltip(Node<TFDNode,Integer> node) {
		StringBuilder sb = new StringBuilder();
		TFDNode nodeRepresentation = node.getPoint();
		sb.append("<h2>Node: " + nodeRepresentation.getID() + "</h2>");
		sb.append("<h2>F-Value</h2>");
		sb.append(node.getInternalLabel());
		if (nodeRepresentation.getAppliedMethodInstance() != null || nodeRepresentation.getAppliedAction() != null) {
			sb.append("<h2>Applied Instance</h2>");
			sb.append(nodeRepresentation.getAppliedMethodInstance() != null ? nodeRepresentation.getAppliedMethodInstance().getEncoding() : nodeRepresentation.getAppliedAction().getEncoding());
		}
		sb.append("<h2>Remaining Tasks</h2>");
		sb.append("<ul>");
		for (Literal l : nodeRepresentation.getRemainingTasks()) {
			sb.append("<li>");
			sb.append(l);
			sb.append("</li>");
		}
		sb.append("</ul>");
		sb.append("<h2>Current State</h2>");
		String[] monomStrings = nodeRepresentation.getProblem().getState().toString().split("&");
		sb.append("<ul>");
		for (int i=0 ; i < monomStrings.length ; i++) {
			sb.append("<li>");
			sb.append(monomStrings[i].trim());
			sb.append("</li>");
		}
		sb.append("</ul>");
		sb.append("<h2>Current Plan</h2>");
		sb.append("<ul>");
		for (Action a : node.externalPath().stream().map(np -> np.getAppliedAction()).filter(a -> a != null).collect(Collectors.toList())) {
			sb.append("<li>");
			sb.append(a.getEncoding());
			sb.append("</li>");
		}
		sb.append("</ul>");
		return sb.toString();
	}

}
