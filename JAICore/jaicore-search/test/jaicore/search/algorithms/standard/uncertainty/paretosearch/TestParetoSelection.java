package jaicore.search.algorithms.standard.uncertainty.paretosearch;

import jaicore.search.structure.core.Node;
import org.junit.Test;

import java.util.PriorityQueue;

public class TestParetoSelection {

    @Test
    public void testCosinusDistanceComparator() {
        CosinusDistanceComparator c = new CosinusDistanceComparator(10.0, 1.0);

        Node<String, Double> p = new Node<>(null, "1");
        p.setAnnotation("f", 2.0);
        p.setAnnotation("uncertainty", 0.9d);
        ParetoNode pp = new ParetoNode(p, 1);

        Node<String, Double> s = new Node<>(null, "4");
        s.setAnnotation("f", 2.5d);
        s.setAnnotation("uncertainty", 0.3);
        ParetoNode ss = new ParetoNode(p, 1);

        Node<String, Double> u = new Node<>(null, "6");
        u.setAnnotation("f", 7.0);
        u.setAnnotation("uncertainty", 0.1);
        ParetoNode uu = new ParetoNode(p, 1);

        double d1 = 1 - c.cosineSimilarity(2.0, 0.9);
        double d4 = 1 - c.cosineSimilarity(2.5d, 0.3d);
        double d6 = 1 - c.cosineSimilarity(7, 0.1);

        System.out.println("d1 = " + d1);
        System.out.println("d4 = " + d4);
        System.out.println("d6 = " + d6);

        System.out.println("Compare 1=(2, 0.9) to 4=(2.5, 0.3)");
        System.out.println(c.compare(pp, ss));
        System.out.println("==============================");

        System.out.println("Compare 6=(7, 0.1) to 4=(2.5, 0.3)");
        System.out.println(c.compare(uu, ss));
        System.out.println("==============================");

        System.out.println("Compare 6=(7, 0.1) to 1=(2, 0.9)");
        System.out.println(c.compare(uu, pp));
        System.out.println("==============================");

    }

    @Test
    public void testParetoFront() {
        Node<String, Double> p = new Node<>(null, "1");
        p.setAnnotation("f", 2.0);
        p.setAnnotation("uncertainty", 0.9d);
        // ParetoNode pp = new ParetoNode(p, 1);

        Node<String, Double> q = new Node<>(null, "2");
        q.setAnnotation("f", 3.0);
        q.setAnnotation("uncertainty", 0.6d);
        // ParetoNode qq = new ParetoNode(q, 2);

        Node<String, Double> r = new Node<>(null, "3");
        r.setAnnotation("f", 5.0);
        r.setAnnotation("uncertainty", 0.4);
        // ParetoNode rr = new ParetoNode(r, 3);

        Node<String, Double> s = new Node<>(null, "4");
        s.setAnnotation("f", 2.5d);
        s.setAnnotation("uncertainty", 0.3);
        // ParetoNode ss = new ParetoNode(s, 4);

        Node<String, Double> t = new Node<>(null, "5");
        t.setAnnotation("f", 8.0);
        t.setAnnotation("uncertainty", 0.3);
        // ParetoNode tt = new ParetoNode(t, 5);

        Node<String, Double> u = new Node<>(null, "6");
        u.setAnnotation("f", 7.0);
        u.setAnnotation("uncertainty", 0.1);
        // ParetoNode uu = new ParetoNode(p, 6);

        CosinusDistanceComparator c = new CosinusDistanceComparator(10.0, 1);
        PriorityQueue pareto = new PriorityQueue<ParetoNode<String, Double>>(c);
        ParetoSelection<String, Double> paretoSelection = new ParetoSelection<String, Double>(pareto);

        paretoSelection.add(p);
        System.out.println(paretoSelection);
        System.out.println("===================");

        paretoSelection.add(q);
        System.out.println(paretoSelection);
        System.out.println("===================");

        paretoSelection.add(r);
        System.out.println(paretoSelection);
        System.out.println("===================");

        paretoSelection.add(s);
        System.out.println(paretoSelection);
        System.out.println("===================");

        paretoSelection.add(t);
        System.out.println(paretoSelection);
        System.out.println("===================");

        paretoSelection.add(u);
        System.out.println(paretoSelection);
        System.out.println("===================");

        for (int i=0; i<=5; i++) {
            Node<String, Double> n = paretoSelection.peek();
            paretoSelection.remove(n);
            System.out.println(n);
            System.out.println(paretoSelection);
            System.out.println("===================");
        }

    }

}
