package jaicore.search.algorithms.standard.uncertainty.paretosearch;

import jaicore.search.structure.core.Node;
import java.util.Comparator;
import java.lang.Double;

public class CosinusDistanceComparator implements Comparator<Node<?,Double>> {

    /**
     * Compares the cosine distance of two nodes to (1,1).
     *
     * @param first
     * @param second
     * @return
     *  -1 if cosine distance to (1,1) of first < cosine distance to (1,1) of second,
     *  0 if they are same and
     *  +1 if cosine distance to (1,1) of first > cosine distance to (1,1) of second
     */
    public int compare(Node<?,Double> first, Node<?,Double> second) {

        Double firstF = (Double) first.getAnnotation("f");
        Double firstC = 1 - (Double)first.getAnnotation("uncertainty");

        Double secondF = (Double) second.getAnnotation("f");
        Double secondC = 1 - (Double)second.getAnnotation("uncertainty");

        double cosDistanceFirst = (firstF + firstC)/(Math.sqrt(firstF+firstC));
        double cosDistanceSecond = (secondF + secondC)/(Math.sqrt(secondF+secondC));

        return (int)((cosDistanceSecond - cosDistanceFirst) * 10000);
    }

}


