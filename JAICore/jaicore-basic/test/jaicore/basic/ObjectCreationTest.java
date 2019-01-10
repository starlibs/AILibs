package jaicore.basic;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class ObjectCreationTest {

	private class Node {
		Node parent;
		int nextDecision;
		int defficiency;
		int val;
	}
	
	@Test
	public void test() {
		List<Node> items = new ArrayList<>();
		for (int i = 0; i < 100000000; i++) {
			long start = System.currentTimeMillis();
			Node obj = new Node();
			long constructionTime = System.currentTimeMillis() - start;
			assert constructionTime <= 1000 : "Creation of object " + i + " took " + constructionTime + "ms";
			obj.val = i;
			items.add(obj);
		}
	}

}
