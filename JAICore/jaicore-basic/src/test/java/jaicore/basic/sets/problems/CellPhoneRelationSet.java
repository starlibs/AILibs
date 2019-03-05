package jaicore.basic.sets.problems;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import jaicore.basic.algorithm.IAlgorithmTestProblemSet;
import jaicore.basic.sets.RelationComputationProblem;

public class CellPhoneRelationSet extends IAlgorithmTestProblemSet<RelationComputationProblem<?>, Collection<List<?>>> {

	public CellPhoneRelationSet() {
		super("Cell phone relation set");
	}

	@Override
	public RelationComputationProblem<?> getSimpleProblemInputForGeneralTestPurposes() throws Exception {
		List<Integer> a = Arrays.asList(new Integer[] { 1, 2, 3 });
		List<Integer> b = Arrays.asList(new Integer[] { 4, 5, 6 });
		List<Integer> c = Arrays.asList(new Integer[] { 7, 8, 9 });
		List<Collection<Integer>> collections = new ArrayList<>();
		collections.add(a);
		collections.add(b);
		collections.add(c);
		return new RelationComputationProblem<>(collections, t -> t.size() < 2 || t.get(0) + 3 == t.get(1));
	}

	@Override
	public RelationComputationProblem<?> getDifficultProblemInputForGeneralTestPurposes() throws Exception {
		List<Collection<Integer>> collections = new ArrayList<>();
		List<Integer> collection = new ArrayList<>();
		for (int i = 0; i < 20; i++) {
			collection.add(i);
			if (collection.size() > 2)
				collections.add(new ArrayList<>(collection));
		}
		return new RelationComputationProblem<>(collections, t -> t.size() < 2|| t.get(0) + 3 == t.get(1));
	}
}
