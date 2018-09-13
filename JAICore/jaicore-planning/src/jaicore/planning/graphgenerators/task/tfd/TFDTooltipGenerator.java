package jaicore.planning.graphgenerators.task.tfd;

import java.util.List;
import java.util.stream.Collectors;

import jaicore.graphvisualizer.TooltipGenerator;
import jaicore.logic.fol.structure.Literal;

public class TFDTooltipGenerator<V extends Comparable<V>> implements TooltipGenerator<TFDNode> {

	@Override
	public String getTooltip(TFDNode node) {
		StringBuilder sb = new StringBuilder();
		if (node.getAppliedMethodInstance() != null || node.getAppliedAction() != null) {
			sb.append("<h2>Applied Instance</h2>");
			sb.append(node.getAppliedMethodInstance() != null ? node.getAppliedMethodInstance().getEncoding() : node.getAppliedAction().getEncoding());
		}
		sb.append("<h2>Remaining Tasks</h2>");
		sb.append("<ul>");
		for (Literal l : node.getRemainingTasks()) {
			sb.append("<li>");
			sb.append(l);
			sb.append("</li>");
		}
		sb.append("</ul>");
		sb.append("<h2>Current State</h2>");
		List<String> monomStrings = node.getProblem().getState().stream().sorted((l1,l2) -> l1.getPropertyName().compareTo(l2.getPropertyName())).map(l -> l.toString()).collect(Collectors.toList());
		sb.append("<ul>");
		for (String literal : monomStrings) {
			sb.append("<li>");
			sb.append(literal);
			sb.append("</li>");
		}
		sb.append("</ul>");
		sb.append("<h2>Current Plan</h2>");
		sb.append("<ul>");
//		for (Action a : node.externalPath().stream().map(np -> np.getAppliedAction()).filter(a -> a != null).collect(Collectors.toList())) {
//			sb.append("<li>");
//			sb.append(a.getEncoding());
//			sb.append("</li>");
//		}
//		sb.append("</ul>");
		return sb.toString();
	}

}
