package jaicore.logic.fol.util;

import java.util.Map;

import jaicore.basic.algorithm.events.AAlgorithmEvent;
import jaicore.logic.fol.structure.LiteralParam;
import jaicore.logic.fol.structure.VariableParam;

public class NextBindingFoundEvent extends AAlgorithmEvent {
	private final Map<VariableParam, LiteralParam> grounding;

	public NextBindingFoundEvent(String algorithmId, Map<VariableParam, LiteralParam> grounding) {
		super(algorithmId);
		this.grounding = grounding;
	}

	public Map<VariableParam, LiteralParam> getGrounding() {
		return grounding;
	}
}
