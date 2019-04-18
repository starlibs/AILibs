package jaicore.testproblems.nqueens;

public class NQueensProblem {
	private final int n;

	public NQueensProblem(int n) {
		super();
		this.n = n;
	}

	public int getN() {
		return n;
	}

	@Override
	public String toString() {
		return "NQueensProblem [n=" + n + "]";
	}
}
