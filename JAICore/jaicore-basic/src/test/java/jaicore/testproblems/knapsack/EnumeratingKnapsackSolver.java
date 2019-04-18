package jaicore.testproblems.knapsack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.sound.midi.Synthesizer;

import jaicore.basic.sets.SetUtil;

public class EnumeratingKnapsackSolver {
	public Collection<Set<String>> getSolutions(KnapsackProblem kp) throws InterruptedException {
		Collection<Set<String>> solutions = new ArrayList<>();
		Set<String> objectsAsSet = kp.getObjects() ;
		for (Collection<String> selection : SetUtil.powerset(objectsAsSet)) {
			double weight = 0;
			for (String item : selection) {
				weight += kp.getWeights().get(item);
			}
			if (weight > kp.getKnapsackCapacity())
				continue;
			
			double remainingWeight = kp.getKnapsackCapacity() - weight;
			Collection<String> missingObjects = SetUtil.difference(objectsAsSet, selection);
			boolean oneMoreFits = false;
			for (String missingObject : missingObjects) {
				if (kp.getWeights().get(missingObject) < remainingWeight) {
					oneMoreFits = true;
					break;
				}
			}
			if (!oneMoreFits && !selection.isEmpty())
				solutions.add(new HashSet<>(selection));
		}
		if (solutions.isEmpty()) {
			solutions.addAll(new HashSet<>());
		}
		return solutions;
	}
}
