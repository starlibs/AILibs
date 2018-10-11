package jaicore.graph;

import jaicore.basic.Score;

public class ScoreNode<T> extends TreeNode<T> implements Score{

	public ScoreNode(T rootData) {
		super(rootData);
		// TODO Auto-generated constructor stub
	}
	
	public ScoreNode(T value, TreeNode<T> parent) {
        super(value, parent);
    }

	@Override
	public double getScore() {
		if(this.getValue() instanceof Number) {
			return ((Number) this.getValue()).doubleValue();
		}
		else
			return -1;
	}

}
