package jaicore.graphvisualizer.gui;

/**
 * An Evaluator which is used to compute the color of the node
 */
public abstract class ObjectEvaluator<V> {

	public ObjectEvaluator() {
		
	};
	
	/**
	 * evaluates the object given as a parameter and returns the coorespoding value
	 * @param object The object to evaluate
	 * @return the evlauation value of the object
	 */
	public abstract double evaluate(V object);
	
}
