package jaicore.logic.fol.util;

import jaicore.logic.fol.structure.Monom;

public class ForwardChainingProblem {
	private final Monom factbase;
	private final Monom conclusion;
	private final boolean cwa;

	public ForwardChainingProblem(Monom factbase, Monom conclusion, boolean cwa) {
		super();
		this.factbase = factbase;
		this.conclusion = conclusion;
		this.cwa = cwa;
	}

	public Monom getFactbase() {
		return factbase;
	}

	public Monom getConclusion() {
		return conclusion;
	}

	public boolean isCwa() {
		return cwa;
	}
}
