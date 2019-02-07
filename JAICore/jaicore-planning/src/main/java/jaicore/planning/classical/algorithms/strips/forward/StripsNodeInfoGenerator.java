package jaicore.planning.classical.algorithms.strips.forward;

import java.util.List;
import java.util.stream.Collectors;

import jaicore.graphvisualizer.plugin.nodeinfo.NodeInfoGenerator;

public class StripsNodeInfoGenerator<V extends Comparable<V>> implements NodeInfoGenerator<StripsForwardPlanningNode> {

	@Override
	public String generateInfoForNode(StripsForwardPlanningNode node) {
		StringBuilder sb = new StringBuilder();
		if (node.getActionToReachState() != null) {
			sb.append("<h2>Applied Action</h2>");
			sb.append(node.getActionToReachState().getEncoding());
		}
		sb.append("<h2>Current Add List</h2>");
		List<String> addMonomStrings = node.getAdd().stream().sorted((l1, l2) -> l1.getPropertyName().compareTo(l2.getPropertyName())).map(l -> l.toString(false)).collect(Collectors.toList());
		sb.append("<ul>");
		for (String literal : addMonomStrings) {
			sb.append("<li>");
			sb.append(literal);
			sb.append("</li>");
		}
		sb.append("</ul>");
		sb.append("<h2>Current Delete List</h2>");
		List<String> delMonomStrings = node.getDel().stream().sorted((l1, l2) -> l1.getPropertyName().compareTo(l2.getPropertyName())).map(l -> l.toString(false)).collect(Collectors.toList());
		sb.append("<ul>");
		for (String literal : delMonomStrings) {
			sb.append("<li>");
			sb.append(literal);
			sb.append("</li>");
		}
		sb.append("</ul>");
		return sb.toString();
	}

}
