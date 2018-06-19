package jaicore.search.algorithms.standard.uncertainty.paretosearch;

import jaicore.search.structure.core.Node;
import org.junit.Test;

import java.util.PriorityQueue;

public class TestParetoSelection {

    @Test
    public void testParetoFront() {
        Node<String, Double> p = new Node<>(null, "p");
        p.setAnnotation("f", 0.2d);
        p.setAnnotation("uncertainty", 0.6);

        Node<String, Double> q = new Node<>(null, "q");
        q.setAnnotation("f", 0.2d);
        q.setAnnotation("uncertainty", 0.8);

        Node<String, Double> r = new Node<>(null, "r");
        r.setAnnotation("f", 0.2d);
        r.setAnnotation("uncertainty", 0.4);

        Node<String, Double> s = new Node<>(null, "s");
        s.setAnnotation("f", 0.2d);
        s.setAnnotation("uncertainty", 0.5);

        PriorityQueue<Node<String, Double>> pareto = new PriorityQueue<>(new CosinusDistanceComparator());
        ParetoSelection<String, Double> paretoSelection = new ParetoSelection<>(pareto);

        paretoSelection.add(p);
        paretoSelection.add(q);
        paretoSelection.add(r);
        paretoSelection.add(s);

        Node<String, Double> n = paretoSelection.peek();
        paretoSelection.remove(n);
        System.out.println(n);

        Node<String, Double> m = paretoSelection.peek();
        System.out.println(m);
        paretoSelection.remove(m);

        Node<String, Double> o = paretoSelection.peek();
        System.out.println(o);
        paretoSelection.remove(o);

        Node<String, Double> x = paretoSelection.peek();
        System.out.println(n);

    }

}
