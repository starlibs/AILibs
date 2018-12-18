package jaicore.basic.sets;

import java.util.Collection;
import java.util.List;

import jaicore.basic.algorithm.AAlgorithmFactory;
import jaicore.basic.algorithm.IAlgorithm;

public class LDSCartesianProductComputerFactory<T> extends AAlgorithmFactory<List<? extends Collection<T>>, List<List<T>>> {

	@Override
	public IAlgorithm<List<? extends Collection<T>>, List<List<T>>> getAlgorithm() {
		return new LDSBasedCartesianProductComputer<>(getInput());
	}
}
