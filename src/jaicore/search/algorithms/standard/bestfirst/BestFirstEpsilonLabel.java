package jaicore.search.algorithms.standard.bestfirst;

public class BestFirstEpsilonLabel implements Comparable<BestFirstEpsilonLabel> {
	private final int f1, f2;

	public BestFirstEpsilonLabel(int f1, int f2) {
		super();
		this.f1 = f1;
		this.f2 = f2;
	}

	public int getF1() {
		return f1;
	}

	public int getF2() {
		return f2;
	}

	@Override
	public int compareTo(BestFirstEpsilonLabel o) {
		return f1 - o.f1;
	}
}
