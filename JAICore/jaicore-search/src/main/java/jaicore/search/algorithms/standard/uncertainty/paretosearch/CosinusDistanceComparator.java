package jaicore.search.algorithms.standard.uncertainty.paretosearch;

import java.util.Comparator;

public class CosinusDistanceComparator<T, V extends Comparable<V>> implements Comparator<ParetoNode<T,V>> {

    public final double x1;
    public final double x2;

    public CosinusDistanceComparator(double x1, double x2) {
        this.x1 = x1;
        this.x2 = x2;
    }

    /**
     * Compares the cosine distance of two nodes to x.
     * @param first
     * @param second
     * @return negative iff first < second, 0 iff first == second, positive iff first > second
     */
    public int compare(ParetoNode<T,V> first, ParetoNode<T,V> second) {

        Double firstF = (Double) first.node.getAnnotation("f");
        Double firstU = (Double) first.node.getAnnotation("uncertainty");

        Double secondF = (Double) second.node.getAnnotation("f");
        Double secondU = (Double) second.node.getAnnotation("uncertainty");

        double cosDistanceFirst = 1 - this.cosineSimilarity(firstF, firstU);
        double cosDistanceSecond = 1 - this.cosineSimilarity(secondF, secondU);

        return (int)((cosDistanceFirst - cosDistanceSecond) * 10000);
    }

    /**
     * Cosine similarity to x.
     * @param f
     * @param u
     * @return
     */
    public double cosineSimilarity(double f, double u) {
        return (this.x1*f + this.x2*u)/(Math.sqrt(f*f + u*u)*Math.sqrt(this.x1*this.x1 + this.x2*this.x2));
    }

}


