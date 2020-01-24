package ai.libs.jaicore.logic.fol.util;

import java.util.Map;

import org.api4.java.algorithm.IAlgorithm;

import ai.libs.jaicore.basic.algorithm.AAlgorithmEvent;
import ai.libs.jaicore.logic.fol.structure.LiteralParam;
import ai.libs.jaicore.logic.fol.structure.VariableParam;

public class NextBindingFoundEvent extends AAlgorithmEvent {
	private final Map<VariableParam, LiteralParam> grounding;

	public NextBindingFoundEvent(final IAlgorithm<?, ?> algorithm, final Map<VariableParam, LiteralParam> grounding) {
		super(algorithm);
		this.grounding = grounding;
	}

	public Map<VariableParam, LiteralParam> getGrounding() {
		return this.grounding;
	}
}
