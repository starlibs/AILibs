package ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import ai.libs.jaicore.graphvisualizer.plugin.nodeinfo.NodeInfoGenerator;
import ai.libs.jaicore.logic.fol.structure.Literal;
import ai.libs.jaicore.planning.core.Action;

public class TFDNodeInfoGenerator implements NodeInfoGenerator<List<TFDNode>> {

	private static final String HTML_LI_OPEN = "<li>";
	private static final String HTML_LI_CLOSE = "</li>";
	private static final String HTML_UL_OPEN = "<ul>";
	private static final String HTML_UL_CLOSE = "</ul>";

	@Override
	public String generateInfoForNode(final List<TFDNode> path) {

		TFDNode head = path.get(path.size() - 1);

		StringBuilder sb = new StringBuilder();
		if (head.getAppliedMethodInstance() != null || head.getAppliedAction() != null) {
			sb.append("<h2>Applied Instance</h2>");
			sb.append(head.getAppliedMethodInstance() != null ? head.getAppliedMethodInstance().getEncoding() : head.getAppliedAction().getEncoding());
		}
		sb.append("<h2>Remaining Tasks</h2>");
		sb.append(HTML_UL_OPEN);
		for (Literal l : head.getRemainingTasks()) {
			sb.append(HTML_LI_OPEN);
			sb.append(l);
			sb.append(HTML_LI_CLOSE);
		}
		sb.append(HTML_UL_CLOSE);
		sb.append("<h2>Current State</h2>");
		List<String> monomStrings = head.getProblem().getState().stream().sorted((l1, l2) -> l1.getPropertyName().compareTo(l2.getPropertyName())).map(Literal::toString).collect(Collectors.toList());
		sb.append(HTML_UL_OPEN);
		for (String literal : monomStrings) {
			sb.append(HTML_LI_OPEN);
			sb.append(literal);
			sb.append(HTML_LI_CLOSE);
		}
		sb.append(HTML_UL_CLOSE);
		sb.append("<h2>Current Plan</h2>");
		sb.append(HTML_UL_OPEN);
		for (Action a : path.stream().map(TFDNode::getAppliedAction).filter(Objects::nonNull).collect(Collectors.toList())) {
			sb.append(HTML_LI_OPEN);
			sb.append(a.getEncoding());
			sb.append(HTML_LI_CLOSE);
		}
		sb.append(HTML_UL_CLOSE);
		return sb.toString();
	}

	@Override
	public String getName() {
		return TFDNodeInfoGenerator.class.getName();
	}

}
