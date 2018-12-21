package jaicore.basic.sets;

import java.util.List;

import jaicore.basic.algorithm.AAlgorithmFactory;
import jaicore.basic.algorithm.IAlgorithm;

public class LDSRelationComputerFactory<T> extends AAlgorithmFactory<RelationComputationProblem<T>, List<Object[]>> {

	@Override
	public IAlgorithm<RelationComputationProblem<T>, List<Object[]>> getAlgorithm() {
		return new LDSRelationComputer<>(getInput());
	}
}
