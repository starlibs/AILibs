package jaicore.search.structure.core;

import jaicore.basic.Score;

public class ScoreNode<T> extends Node<T, Double> implements Score {
    public ScoreNode(Node<T, Double> parent, T point) {
        super(parent, point);
    }

    @Override
    public double getScore() {
       return  this.getInternalLabel();
    }
}
