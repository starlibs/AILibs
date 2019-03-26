package jaicore.basic.sets.problems;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import jaicore.basic.sets.RelationComputationProblem;

public class CellPhoneRelationSet extends RelationalProblemSet {

	public CellPhoneRelationSet() {
		super("Cell phone relation set");
	}

	@Override
	public RelationComputationProblem<Object> getSimpleProblemInputForGeneralTestPurposes() {
		List<Object> a = Arrays.asList(1, 2, 3);
		List<Object> b = Arrays.asList(4, 5, 6);
		List<Object> c = Arrays.asList(7, 8, 9);
		List<Collection<Object>> collections = new ArrayList<>();
		collections.add(a);
		collections.add(b);
		collections.add(c);
		return new RelationComputationProblem<>(collections, t -> t.size() < 2 || (int) t.get(0) + 3 == (int) t.get(1));
	}

	@Override
	public RelationComputationProblem<Object> getDifficultProblemInputForGeneralTestPurposes() {
		List<Collection<Object>> collections = new ArrayList<>();
		List<Object> collection = new ArrayList<>();
		for (int i = 0; i < 20; i++) {
			collection.add(i);
			if (collection.size() > 2) {
				collections.add(new ArrayList<>(collection));
			}
		}
		return new RelationComputationProblem<>(collections, t -> t.size() < 2 || (int) t.get(0) + 3 == (int) t.get(1));
	}
}
