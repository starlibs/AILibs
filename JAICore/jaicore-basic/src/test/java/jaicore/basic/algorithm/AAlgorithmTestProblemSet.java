package jaicore.basic.algorithm;

public abstract class AAlgorithmTestProblemSet<I> implements IAlgorithmTestProblemSet<I> {
	private final String name;

	public AAlgorithmTestProblemSet(final String name) {
		super();
		this.name = name;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String toString() {
		return this.name;
	}
}
