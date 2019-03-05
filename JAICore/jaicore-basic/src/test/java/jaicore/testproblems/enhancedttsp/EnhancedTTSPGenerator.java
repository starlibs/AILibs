package jaicore.testproblems.enhancedttsp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import jaicore.basic.sets.SetUtil.Pair;
import jaicore.graph.LabeledGraph;

public class EnhancedTTSPGenerator {
	public EnhancedTTSP generate(int n, int maxDistance) {
		
		/* create TTSP problem */
		Random r = new Random(0);
		List<Boolean> blockedHours = Arrays.asList(
				new Boolean[] { true, true, true, true, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, true });
		LabeledGraph<Short, Double> travelGraph;
		travelGraph = new LabeledGraph<>();
		List<Pair<Double, Double>> coordinates = new ArrayList<>();
		for (short i = 0; i < n; i++) {
			coordinates.add(new Pair<>(r.nextDouble() * maxDistance, r.nextDouble() * maxDistance));
			travelGraph.addItem(i);
		}
		for (short i = 0; i < n; i++) {
			double x1 = coordinates.get(i).getX();
			double y1 = coordinates.get(i).getY();
			for (short j = 0; j < i; j++) { // we assume a symmetric travel graph
				double x2 = coordinates.get(j).getX();
				double y2 = coordinates.get(j).getY();
				double minTravelTime = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
				// System.out.println("(" + x1 + ", " + y1 + ") to (" +x2 +", " + y2 +") = "
				// +minTravelTime);
				travelGraph.addEdge(i, j, minTravelTime);
				travelGraph.addEdge(j, i, minTravelTime);
			}
		}
		EnhancedTTSP ttsp = new EnhancedTTSP(travelGraph, (short) 0, blockedHours, 8, 4.5, 1, 10);
		return ttsp;
	}
}
