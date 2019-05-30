package hasco.gui.civiewplugin;

import java.util.Collection;
import java.util.Map.Entry;

import hasco.core.Util;
import hasco.model.Component;
import hasco.model.ComponentInstance;
import hasco.model.Parameter;
import jaicore.graphvisualizer.plugin.nodeinfo.NodeInfoGenerator;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import jaicore.search.model.travesaltree.Node;

/**
 * This info generator is meant to be used in combination with the node info plug-in.
 *
 * @author wever
 */
public class TFDNodeAsCIViewInfoGenerator implements NodeInfoGenerator<Node<TFDNode, Double>> {

	private Collection<Component> components;

	public TFDNodeAsCIViewInfoGenerator(final Collection<Component> components) {
		this.components = components;
	}

	@Override
	public String generateInfoForNode(final Node<TFDNode, Double> node) {
		ComponentInstance ci = Util.getSolutionCompositionFromState(this.components, node.getPoint().getState(), true);
		if (ci == null) {
			return "<i>No component has been chosen, yet.</i>";
		} else {
			return this.visualizeComponentInstance(ci);
		}
	}

	private String visualizeComponentInstance(final ComponentInstance ci) {
		StringBuilder sb = new StringBuilder();

		sb.append("<div style=\"border: 1px solid #333; padding: 10px; font-family: Arial, non-serif;\">");
		/* add the name of the component */
		sb.append("<div style=\"text-align: center;font-size: 18px; font-weight: bold;\">" + ci.getComponent().getName() + "</div>");

		sb.append("<table style=\"width: 100%;\">");
		sb.append("<tr style=\"background: #e0e0e0;\"><th>Parameter</th><th>Value</th></tr>");

		int i = 0;
		for (Parameter parameter : ci.getComponent().getParameters()) {
			if (i % 2 == 0) {
				sb.append("<tr style=\"background: #f2f2f2;\">");
			} else {
				sb.append("<tr style=\"background: #efefef;\">");
			}

			sb.append("<td>" + parameter.getName() + "</td>");
			sb.append("<td>" + (ci.getParameterValues().containsKey(parameter.getName()) ? ci.getParameterValue(parameter) : "not yet set") + "</td>");
			sb.append("</tr>");
			i++;
		}
		sb.append("</table>");

		for (Entry<String, ComponentInstance> subComponent : ci.getSatisfactionOfRequiredInterfaces().entrySet()) {
			sb.append(subComponent.getKey());
			sb.append(this.visualizeComponentInstance(subComponent.getValue()));
		}
		sb.append("</div>");

		return sb.toString();
	}

}
