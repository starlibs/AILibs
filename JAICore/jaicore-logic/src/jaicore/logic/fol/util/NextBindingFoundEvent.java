package jaicore.logic.fol.util;

import java.util.Map;

import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.logic.fol.structure.LiteralParam;
import jaicore.logic.fol.structure.VariableParam;

public class NextBindingFoundEvent implements AlgorithmEvent {
	private final Map<VariableParam, LiteralParam> grounding;

	public NextBindingFoundEvent(Map<VariableParam, LiteralParam> grounding) {
		super();
		this.grounding = grounding;
	}

	public Map<VariableParam, LiteralParam> getGrounding() {
		return grounding;
	}
}
