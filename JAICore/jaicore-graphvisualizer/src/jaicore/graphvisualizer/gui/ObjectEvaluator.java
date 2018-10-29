package jaicore.graphvisualizer.gui;

/**
 * An Evaluator which is used to compute the color of the node
 * @author jkoepe
 *
 */
public abstract class ObjectEvaluator<V> {

	public ObjectEvaluator() {
		
	};
	
	
	public abstract double evaluate(V object);
	
}
