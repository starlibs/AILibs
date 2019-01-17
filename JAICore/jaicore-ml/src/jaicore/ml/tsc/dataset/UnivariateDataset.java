package jaicore.ml.tsc.dataset;

/**
 * UnivariateDataset
 */
public class UnivariateDataset {

    private double[][] values;

    public final int[] targets;

    private final boolean train;

    public UnivariateDataset(double[][] values) {
        this.values = values;
        this.targets = new int[values.length];
        this.train = false;
    }

    public UnivariateDataset(double[][] values, int[] targets) {
        this.values = values;
        this.targets = targets;
        this.train = true;
    }

    public void setAttributes(double[][] values) {
        this.values = values;
    }

    public double[][] getValues() {
        return values;
    }

    public boolean isTrain() {
        return train;
    }

    public boolean isTest() {
        return !train;
    }
}