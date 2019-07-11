package ai.libs.jaicore.basic.sets;

import java.util.List;

import org.api4.java.algorithm.IAlgorithm;

import ai.libs.jaicore.basic.algorithm.AAlgorithmFactory;

public class LDSRelationComputerFactory<T> extends AAlgorithmFactory<RelationComputationProblem<T>, List<List<T>>, IAlgorithm<RelationComputationProblem<T>, List<List<T>>>> {

	@Override
	public IAlgorithm<RelationComputationProblem<T>, List<List<T>>> getAlgorithm() {
		return new LDSRelationComputer<>(this.getInput());
	}

	@Override
	public IAlgorithm<RelationComputationProblem<T>, List<List<T>>> getAlgorithm(final RelationComputationProblem<T> input) {
		return new LDSRelationComputer<>(input);
	}
}
