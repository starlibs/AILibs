package jaicore.logic.fol.structure;

public class HornRule {
	private Monom premise;
	private Literal conclusion;

	public HornRule(Monom premise, Literal conclusion) {
		super();
		this.premise = premise;
		this.conclusion = conclusion;
	}

	public Monom getPremise() {
		return premise;
	}

	public Literal getConclusion() {
		return conclusion;
	}

	@Override
	public String toString() {
		return premise + " -> " + conclusion;
	}
}
