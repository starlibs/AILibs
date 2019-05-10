package ai.libs.jaicore.basic.sets;

import java.util.List;

import ai.libs.jaicore.basic.algorithm.AAlgorithmFactory;
import ai.libs.jaicore.basic.algorithm.IAlgorithm;

public class LDSRelationComputerFactory<T> extends AAlgorithmFactory<RelationComputationProblem<T>, List<List<T>>> {

	@Override
	public IAlgorithm<RelationComputationProblem<T>, List<List<T>>> getAlgorithm() {
		return new LDSRelationComputer<>(this.getInput());
	}

	@Override
	public IAlgorithm<RelationComputationProblem<T>, List<List<T>>> getAlgorithm(final RelationComputationProblem<T> input) {
		return new LDSRelationComputer<>(input);
	}
}
