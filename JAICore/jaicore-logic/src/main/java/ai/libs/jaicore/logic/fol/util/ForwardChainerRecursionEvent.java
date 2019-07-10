package ai.libs.jaicore.logic.fol.util;

import org.api4.java.algorithm.events.AAlgorithmEvent;

import ai.libs.jaicore.logic.fol.structure.Literal;
import ai.libs.jaicore.logic.fol.structure.Monom;

public class ForwardChainerRecursionEvent extends AAlgorithmEvent {
	private Literal local;
	private Monom remainingConclusion;

	public ForwardChainerRecursionEvent(final String algorithmId, final Literal local, final Monom remainingConclusion) {
		super(algorithmId);
		this.local = local;
		this.remainingConclusion = remainingConclusion;
	}

	public Literal getLocal() {
		return this.local;
	}

	public void setLocal(final Literal local) {
		this.local = local;
	}

	public Monom getRemainingConclusion() {
		return this.remainingConclusion;
	}

	public void setRemainingConclusion(final Monom remainingConclusion) {
		this.remainingConclusion = remainingConclusion;
	}
}
