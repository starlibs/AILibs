package jaicore.search.algorithms.standard.uncertainty;

import java.util.PriorityQueue;

import org.junit.Before;
import org.junit.Test;

import jaicore.search.algorithms.standard.uncertainty.paretosearch.CosinusDistanceComparator;
import jaicore.search.algorithms.standard.uncertainty.paretosearch.ParetoNode;
import jaicore.search.algorithms.standard.uncertainty.paretosearch.ParetoSelection;
import jaicore.search.model.travesaltree.Node;

public class TestParetoSelection {

	private Node<String, Double> p, q, r, s, t, u, p2, q2, r2, s2, t2, u2;

	@Before
	public void setUp() {
		this.p = new Node<>(null, "1");
		this.p.setAnnotation("f", 2.0);
		this.p.setAnnotation("uncertainty", 0.9d);

		this.q = new Node<>(null, "2");
		this.q.setAnnotation("f", 3.0);
		this.q.setAnnotation("uncertainty", 0.6d);

		this.r = new Node<>(null, "3");
		this.r.setAnnotation("f", 5.0);
		this.r.setAnnotation("uncertainty", 0.4);

		this.s = new Node<>(null, "4");
		this.s.setAnnotation("f", 2.5d);
		this.s.setAnnotation("uncertainty", 0.3);

		this.t = new Node<>(null, "5");
		this.t.setAnnotation("f", 8.0);
		this.t.setAnnotation("uncertainty", 0.3);

		this.u = new Node<>(null, "6");
		this.u.setAnnotation("f", 7.0);
		this.u.setAnnotation("uncertainty", 0.1);

		this.p2 = new Node<>(null, "1");
		this.p2.setAnnotation("f", -2.0);
		this.p2.setAnnotation("uncertainty", 0.9d);

		this.q2 = new Node<>(null, "2");
		this.q2.setAnnotation("f", -3.0);
		this.q2.setAnnotation("uncertainty", 0.6d);

		this.r2 = new Node<>(null, "3");
		this.r2.setAnnotation("f", -5.0);
		this.r2.setAnnotation("uncertainty", 0.4);

		this.s2 = new Node<>(null, "4");
		this.s2.setAnnotation("f", -2.5d);
		this.s2.setAnnotation("uncertainty", 0.3);

		this.t2 = new Node<>(null, "5");
		this.t2.setAnnotation("f", -8.0);
		this.t2.setAnnotation("uncertainty", 0.3);

		this.u2 = new Node<>(null, "6");
		this.u2.setAnnotation("f", -7.0);
		this.u2.setAnnotation("uncertainty", 0.1);
	}

	@Test
	public void testCosinusDistanceComparatorWithFNegativeValues() {
		CosinusDistanceComparator<String, Double> c = new CosinusDistanceComparator<>(-1.0, 1.0);

		double d1 = 1 - c.cosineSimilarity(2.0, 0.3d);
		double d4 = 1 - c.cosineSimilarity(-2.0d, 0.3d);

		System.out.println(d1);
		System.out.println(d4);
	}

	@Test
	public void testCosinusDistanceComparator() {
		CosinusDistanceComparator<String, Double> c = new CosinusDistanceComparator<>(10.0, 1.0);

		ParetoNode<String, Double> pp = new ParetoNode<>(this.p, 1);
		ParetoNode<String, Double> ss = new ParetoNode<>(this.s, 1);
		ParetoNode<String, Double> uu = new ParetoNode<>(this.u, 1);

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
		CosinusDistanceComparator<String, Double> c = new CosinusDistanceComparator<>(-10.0, 1);
		PriorityQueue<ParetoNode<String, Double>> pareto = new PriorityQueue<>(c);
		ParetoSelection<String, Double> paretoSelection = new ParetoSelection<String, Double>(pareto);

		paretoSelection.add(this.p);
		System.out.println(paretoSelection);
		System.out.println("===================");

		paretoSelection.add(this.q);
		System.out.println(paretoSelection);
		System.out.println("===================");

		paretoSelection.add(this.r);
		System.out.println(paretoSelection);
		System.out.println("===================");

		paretoSelection.add(this.s);
		System.out.println(paretoSelection);
		System.out.println("===================");

		paretoSelection.add(this.t);
		System.out.println(paretoSelection);
		System.out.println("===================");

		paretoSelection.add(this.u);
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
		CosinusDistanceComparator<String, Double> c = new CosinusDistanceComparator<>(-10.0, 1);
		PriorityQueue<ParetoNode<String, Double>> pareto = new PriorityQueue<>(c);
		ParetoSelection<String, Double> paretoSelection = new ParetoSelection<>(pareto);

		paretoSelection.add(this.p);
		System.out.println(paretoSelection);
		System.out.println("===================");

		paretoSelection.add(this.q);
		System.out.println(paretoSelection);
		System.out.println("===================");

		paretoSelection.add(this.r);
		System.out.println(paretoSelection);
		System.out.println("===================");

		paretoSelection.add(this.s);
		System.out.println(paretoSelection);
		System.out.println("===================");

		paretoSelection.add(this.t);
		System.out.println(paretoSelection);
		System.out.println("===================");

		paretoSelection.add(this.u);
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
