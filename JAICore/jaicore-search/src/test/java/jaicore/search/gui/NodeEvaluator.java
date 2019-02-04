package jaicore.search.gui;

import jaicore.graphvisualizer.gui.ObjectEvaluator;
import jaicore.search.model.travesaltree.Node;

/**
 * An Objecevaluator for the node class, which returns the f value
 *
 */
public class NodeEvaluator extends ObjectEvaluator<Node> {

	@Override
	public double evaluate(Node object) {
		
		double value = 0;
		if(object.getInternalLabel() instanceof Number) {
			value = ((Number) object.getInternalLabel()).doubleValue();
		}
		return value;
	}
	
}
