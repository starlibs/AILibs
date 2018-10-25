package jaicore.search.algorithms.standard.uncertainty;

import org.junit.Before;
import org.junit.Test;

import jaicore.search.algorithms.standard.uncertainty.paretosearch.CosinusDistanceComparator;
import jaicore.search.algorithms.standard.uncertainty.paretosearch.ParetoNode;
import jaicore.search.algorithms.standard.uncertainty.paretosearch.ParetoSelection;
import jaicore.search.model.travesaltree.Node;

import java.util.ArrayList;
import java.util.PriorityQueue;

public class TestParetoSelection {

    private Node<String, Double> p, q, r, s, t, u, p2, q2, r2, s2, t2, u2;

    @Before
    public void setUp() {
        p = new Node<>(null, "1");
        p.setAnnotation("f", 2.0);
        p.setAnnotation("uncertainty", 0.9d);

        q = new Node<>(null, "2");
        q.setAnnotation("f", 3.0);
        q.setAnnotation("uncertainty", 0.6d);

        r = new Node<>(null, "3");
        r.setAnnotation("f", 5.0);
        r.setAnnotation("uncertainty", 0.4);

        s = new Node<>(null, "4");
        s.setAnnotation("f", 2.5d);
        s.setAnnotation("uncertainty", 0.3);

        t = new Node<>(null, "5");
        t.setAnnotation("f", 8.0);
        t.setAnnotation("uncertainty", 0.3);

        u = new Node<>(null, "6");
        u.setAnnotation("f", 7.0);
        u.setAnnotation("uncertainty", 0.1);

        p2 = new Node<>(null, "1");
        p2.setAnnotation("f", -2.0);
        p2.setAnnotation("uncertainty", 0.9d);

        q2 = new Node<>(null, "2");
        q2.setAnnotation("f", -3.0);
        q2.setAnnotation("uncertainty", 0.6d);

        r2 = new Node<>(null, "3");
        r2.setAnnotation("f", -5.0);
        r2.setAnnotation("uncertainty", 0.4);

        s2 = new Node<>(null, "4");
        s2.setAnnotation("f", -2.5d);
        s2.setAnnotation("uncertainty", 0.3);

        t2 = new Node<>(null, "5");
        t2.setAnnotation("f", -8.0);
        t2.setAnnotation("uncertainty", 0.3);

        u2 = new Node<>(null, "6");
        u2.setAnnotation("f", -7.0);
        u2.setAnnotation("uncertainty", 0.1);
    }

    @Test
    public void testCosinusDistanceComparatorWithFNegativeValues() {
        CosinusDistanceComparator c = new CosinusDistanceComparator(-1.0, 1.0);

        double d1 = 1 - c.cosineSimilarity(2.0, 0.3d);
        double d4 = 1 - c.cosineSimilarity(-2.0d, 0.3d);

        System.out.println(d1);
        System.out.println(d4);
        //System.out.println(d6);

    }

    @Test
    public void testCosinusDistanceComparator() {
        CosinusDistanceComparator c = new CosinusDistanceComparator(10.0, 1.0);

        ParetoNode pp = new ParetoNode(p, 1);
        ParetoNode ss = new ParetoNode(s, 1);
        ParetoNode uu = new ParetoNode(u, 1);

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
        CosinusDistanceComparator c = new CosinusDistanceComparator(-10.0, 1);
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

    @Test
    public void testParetoFrontWithNegativeFValues() {
        CosinusDistanceComparator c = new CosinusDistanceComparator(-10.0, 1);
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
