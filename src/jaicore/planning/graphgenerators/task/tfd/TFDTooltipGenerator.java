package jaicore.planning.graphgenerators.task.tfd;

import java.util.Map;
import java.util.stream.Collectors;

import jaicore.graphvisualizer.TooltipGenerator;
import jaicore.logic.fol.structure.Literal;
import jaicore.planning.model.core.Action;
import jaicore.search.structure.core.Node;

public class TFDTooltipGenerator<V extends Comparable<V>> implements TooltipGenerator<Node<TFDNode, V>> {

	@Override
	public String getTooltip(Node<TFDNode,V> node) {
		StringBuilder sb = new StringBuilder();
		TFDNode nodeRepresentation = node.getPoint();
		Map<String,Object> annotations = node.getAnnotations();
		sb.append("<h2>Node: " + nodeRepresentation.getID() + "</h2>");
		sb.append(annotations);
		sb.append("<h2>F-Value</h2>");
		sb.append(node.getInternalLabel());
		if (annotations.containsKey("fRPSamples")) {
			sb.append(" (based on " + annotations.get("fRPSamples") + " samples)");
		}
		if (annotations.containsKey("fError")) {
			sb.append("<pre style=\"color: red;\">" + annotations.get("fError") + "</pre>");
		}
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
