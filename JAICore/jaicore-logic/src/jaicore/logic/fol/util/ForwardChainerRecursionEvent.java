package jaicore.logic.fol.util;

import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.Monom;

public class ForwardChainerRecursionEvent implements AlgorithmEvent {
	private Literal local;
	private Monom remainingConclusion;

	public ForwardChainerRecursionEvent(Literal local, Monom remainingConclusion) {
		super();
		this.local = local;
		this.remainingConclusion = remainingConclusion;
	}

	public Literal getLocal() {
		return local;
	}

	public void setLocal(Literal local) {
		this.local = local;
	}

	public Monom getRemainingConclusion() {
		return remainingConclusion;
	}

	public void setRemainingConclusion(Monom remainingConclusion) {
		this.remainingConclusion = remainingConclusion;
	}
}
