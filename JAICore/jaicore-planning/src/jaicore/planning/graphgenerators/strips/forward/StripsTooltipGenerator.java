package jaicore.planning.graphgenerators.strips.forward;

import java.util.List;
import java.util.stream.Collectors;

import jaicore.graphvisualizer.TooltipGenerator;
import jaicore.logic.fol.structure.Literal;

public class StripsTooltipGenerator<V extends Comparable<V>> implements TooltipGenerator<StripsForwardPlanningNode> {

	@Override
	public String getTooltip(StripsForwardPlanningNode node) {
		StringBuilder sb = new StringBuilder();
		if (node.getActionToReachState() != null) {
			sb.append("<h2>Applied Action</h2>");
			sb.append(node.getActionToReachState().getEncoding());
		}
		sb.append("<h2>Current State</h2>");
		List<String> monomStrings = node.getState().stream().sorted((l1,l2) -> l1.getPropertyName().compareTo(l2.getPropertyName())).map(l -> l.toString(false)).collect(Collectors.toList());
		sb.append("<ul>");
		for (String literal : monomStrings) {
			sb.append("<li>");
			sb.append(literal);
			sb.append("</li>");
		}
		sb.append("</ul>");
		return sb.toString();
	}

}
