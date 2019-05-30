package jaicore.logic.fol.util;

import jaicore.basic.algorithm.events.AAlgorithmEvent;
import jaicore.logic.fol.structure.Literal;
import jaicore.logic.fol.structure.Monom;

public class ForwardChainerRecursionEvent extends AAlgorithmEvent {
	private Literal local;
	private Monom remainingConclusion;

	public ForwardChainerRecursionEvent(String algorithmId, Literal local, Monom remainingConclusion) {
		super(algorithmId);
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
