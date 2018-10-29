package jaicore.search.gui;

import jaicore.graphvisualizer.gui.ObjectEvaluator;
import jaicore.search.model.travesaltree.Node;

public class NodeEvaluator extends ObjectEvaluator<Node> {

	@Override
	public double evaluate(Node object) {
		
		double value = 0;
		if(object.getInternalLabel() instanceof Number) {
			value = ((Number) object.getInternalLabel()).doubleValue();
		}
//		value = Math.random()*10000000;
		return value;
	}
	
}
