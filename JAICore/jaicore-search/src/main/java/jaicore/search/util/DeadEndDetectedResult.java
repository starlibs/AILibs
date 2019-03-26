package jaicore.search.util;

public class DeadEndDetectedResult<N> extends SanityCheckResult {
	private final N deadEnd;

	public DeadEndDetectedResult(N deadEnd) {
		super();
		this.deadEnd = deadEnd;
	}

	public N getDeadEnd() {
		return deadEnd;
	}

	@Override
	public String toString() {
		return "DeadEndDetectedResult [deadEnd=" + deadEnd + "]";
	}
}
