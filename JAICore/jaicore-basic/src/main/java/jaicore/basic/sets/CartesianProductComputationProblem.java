package jaicore.basic.sets;

import java.util.Collection;
import java.util.List;

public class CartesianProductComputationProblem<T> extends RelationComputationProblem<T> {

	public CartesianProductComputationProblem(List<Collection<T>> sets) {
		super(sets, n -> true);
	}
}
