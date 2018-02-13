package jaicore.planning.graphgenerators.task.tfd;

import java.util.Arrays;
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
		sb.append("<h2>Annotation</h2><table><tr><th>Key</th><th>Value</th></tr>");
		for (String key : annotations.keySet()) {
			sb.append("<tr><td>" + key + "</td><td>" + annotations.get(key) + "</td></tr>");
		}
		sb.append("</table>");
		sb.append("<h2>F-Value</h2>");
		sb.append(node.getInternalLabel());
		if (annotations.containsKey("fRPSamples")) {
			sb.append(" (based on " + annotations.get("fRPSamples") + " samples)");
		}
		if (annotations.containsKey("fError") && (annotations.get("fError") instanceof Throwable)) {
			sb.append("<h2>Error Details:</h2><pre style=\"color: red;\">");
			Throwable e = (Throwable)annotations.get("fError");
			sb.append("Error Type " + e.getClass().getName() + "\nMessage: " + e.getMessage() +"\nStack Trace:\n");
			for (StackTraceElement ste : e.getStackTrace()) {
				sb.append("  " + ste.toString() + "\n");
			}
			sb.append("</pre>");
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
